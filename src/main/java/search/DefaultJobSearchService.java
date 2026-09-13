package search;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import classification.JobSeniorityClassifier;
import classification.JobTagClassifier;
import classification.JobTechScopeClassifier;
import classification.JobWorkTypeClassifier;
import deduplication.ExactJobDuplicateDetector;
import deduplication.JobDuplicateDetector;
import domain.JobPosting;
import provider.JobProvider;
import provider.ProviderException;
import ranking.JobRelevanceScorer;
import ranking.WeightedJobRelevanceScorer;

public class DefaultJobSearchService implements JobSearchService {

    private final JobProvider[] providers;
    private final JobSeniorityClassifier seniorityClassifier = new JobSeniorityClassifier();
    private final JobWorkTypeClassifier workTypeClassifier = new JobWorkTypeClassifier();
    private final JobTechScopeClassifier techScopeClassifier = new JobTechScopeClassifier();
    private final JobTagClassifier tagClassifier = new JobTagClassifier();
    private final JobRelevanceScorer relevanceScorer = new WeightedJobRelevanceScorer();
    private final JobDuplicateDetector duplicateDetector = new ExactJobDuplicateDetector();
    private static final Pattern SALARY_NUMBER_PATTERN = Pattern.compile("(\\d[\\d,]*(?:\\.\\d+)?)\\s*([kK])?");

    public DefaultJobSearchService(JobProvider... providers) {
        if (providers == null || providers.length == 0) {
            throw new IllegalArgumentException("At least one job provider is required.");
        }
        this.providers = providers;
    }

    @Override
    public List<JobPosting> searchJobs(String location, String position) {
        return searchJobs(new JobSearchCriteria(location, position, "", "", "", ""));
    }

    @Override
    public List<JobPosting> searchJobs(JobSearchCriteria criteria) {
        return search(criteria).jobs();
    }

    @Override
    public JobSearchOutcome search(JobSearchCriteria criteria) {
        List<JobPosting> jobs = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int failedProviders = 0;

        for(JobProvider provider : providers){
            try{
                for(JobPosting job : provider.getJobPostings(criteria.location(), criteria.position())) {
                    if(hasTitle(job)){
                        String seniority = seniorityClassifier.classify(job);
                        String workType = workTypeClassifier.classify(job);
                        boolean techRelated = techScopeClassifier.isTechRelated(job);
                        
                        JobPosting classifiedJob = job.withClassification(
                                provider.getSourceName(),
                                seniority,
                                workType,
                                techRelated,
                                List.of()
                        );
                    
                        JobPosting enrichedJob = classifiedJob.withClassification(
                                provider.getSourceName(),
                                seniority,
                                workType,
                                techRelated,
                                tagClassifier.classify(classifiedJob)
                        );

                        if(enrichedJob.techRelated() && matchesCriteria(enrichedJob, criteria)) {
                            jobs.add(enrichedJob);
                        }
                    }
                }
            } catch (RuntimeException e) {
                failedProviders++;
                String warning = provider.getSourceName() + " is temporarily unavailable. Showing results from other sources.";
                warnings.add(warning);
                System.out.println("Job provider failed: " + provider.getSourceName() + " - " + e.getMessage());
            }
        }
        
        if (failedProviders == providers.length) {
            throw new ProviderException("All job providers failed.");
        }

        sortJobs(jobs, criteria);
        return new JobSearchOutcome(duplicateDetector.removeDuplicates(jobs), warnings);
    }

    private void sortJobs(List<JobPosting> jobs, JobSearchCriteria criteria) {
        jobs.sort((first, second) -> compareJobs(first, second, criteria));
    }

    private int compareJobs(JobPosting first, JobPosting second, JobSearchCriteria criteria) {
        int result;
        switch (criteria.sortOption()) {
            case NEWEST:
                result = compareDescending(parsePostedDate(first.postedDate()), parsePostedDate(second.postedDate()));
                break;
            case SALARY:
                result = compareDescending(extractSalaryAmount(first.salary()), extractSalaryAmount(second.salary()));
                break;
            case COMPANY:
                result = compareAscending(first.companyName(), second.companyName());
                break;
            case RELEVANCE:
            default:
                result = compareRelevance(first, second, criteria);
                break;
        }

        if (result != 0) {
            return result;
        }
        return compareRelevance(first, second, criteria);
    }

    private int compareRelevance(JobPosting first, JobPosting second, JobSearchCriteria criteria) {
        return Integer.compare(
                relevanceScorer.score(second, criteria),
                relevanceScorer.score(first, criteria)
        );
    }

    private int compareAscending(String firstValue, String secondValue) {
        return normalize(firstValue).compareTo(normalize(secondValue));
    }

    private <T extends Comparable<T>> int compareDescending(T firstValue, T secondValue) {
        if (firstValue == null && secondValue == null) {
            return 0;
        }
        if (firstValue == null) {
            return 1;
        }
        if (secondValue == null) {
            return -1;
        }
        return secondValue.compareTo(firstValue);
    }

    private Instant parsePostedDate(String value) {
        String cleanedValue = clean(value);
        if (cleanedValue.isEmpty()) {
            return null;
        }

        try {
            return Instant.parse(cleanedValue);
        } catch (RuntimeException ignored) {
            // Try less specific date formats below.
        }

        try {
            return OffsetDateTime.parse(cleanedValue).toInstant();
        } catch (RuntimeException ignored) {
            // Try plain yyyy-MM-dd below.
        }

        try {
            return LocalDate.parse(cleanedValue).atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private Double extractSalaryAmount(String salary) {
        String cleanedSalary = clean(salary);
        if (cleanedSalary.isEmpty()) {
            return null;
        }

        Matcher matcher = SALARY_NUMBER_PATTERN.matcher(cleanedSalary);
        Double maxValue = null;
        while (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1).replace(",", ""));
            if (matcher.group(2) != null) {
                value *= 1000;
            }
            if (maxValue == null || value > maxValue) {
                maxValue = value;
            }
        }
        return maxValue;
    }

    private boolean hasTitle(JobPosting job) {
        return job != null && job.title() != null && !job.title().isBlank();
    }

    private boolean matchesCriteria(JobPosting job, JobSearchCriteria criteria) {
        return matchesPosition(job, criteria.position())
                && matchesPartialValue(job.category(), criteria.category())
                && matchesValue(job.seniority(), criteria.seniority())
                && matchesValue(job.workType(), criteria.workType())
                && matchesTag(job, criteria.tag())
                && matchesMinimumSalary(job, criteria.minimumSalary())
                && matchesPostedWithinDays(job, criteria.postedWithinDays());
    }

    private boolean matchesPosition(JobPosting job, String searchQuery) {
        if (searchQuery.isEmpty()) {
            return true;
        }

        String searchableText = normalize(job.title())
                + " " + normalize(job.description())
                + " " + normalize(job.category());

        for (String keyword : normalize(searchQuery).split("\\s+")) {
            if (!keyword.isBlank() && !searchableText.contains(keyword)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesPartialValue(String value, String criterion) {
        return criterion.isEmpty() || normalize(value).contains(normalize(criterion));
    }

    private boolean matchesValue(String value, String criterion) {
        return criterion.isEmpty() || normalize(value).equals(normalize(criterion));
    }

    private boolean matchesTag(JobPosting job, String tag) {
        if (tag.isEmpty()) {
            return true;
        }
        for (String jobTag : job.tags()) {
            if (normalize(jobTag).equals(normalize(tag))) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesMinimumSalary(JobPosting job, String minimumSalary) {
        if (minimumSalary.isEmpty()) {
            return true;
        }

        Double salaryAmount = extractSalaryAmount(job.salary());
        if (salaryAmount == null) {
            return false;
        }

        if ("available".equals(normalize(minimumSalary))) {
            return true;
        }

        try {
            return salaryAmount >= Double.parseDouble(minimumSalary);
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private boolean matchesPostedWithinDays(JobPosting job, String postedWithinDays) {
        if (postedWithinDays.isEmpty()) {
            return true;
        }

        Instant postedDate = parsePostedDate(job.postedDate());
        if (postedDate == null) {
            return false;
        }

        try {
            int days = Integer.parseInt(postedWithinDays);
            Instant earliestAllowedDate = Instant.now().minus(days, ChronoUnit.DAYS);
            return !postedDate.isBefore(earliestAllowedDate);
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
