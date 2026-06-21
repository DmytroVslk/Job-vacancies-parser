package service;

import model.JobProvider;
import model.ProviderException;
import vo.JobPosting;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobSearchService {

    private final JobProvider[] providers;
    private final JobSeniorityClassifier seniorityClassifier = new JobSeniorityClassifier();
    private final JobWorkTypeClassifier workTypeClassifier = new JobWorkTypeClassifier();
    private final JobTechScopeClassifier techScopeClassifier = new JobTechScopeClassifier();
    private final JobTagClassifier tagClassifier = new JobTagClassifier();
    private final JobRelevanceScorer relevanceScorer = new JobRelevanceScorer();
    private final JobDuplicateDetector duplicateDetector = new JobDuplicateDetector();
    private static final Pattern SALARY_NUMBER_PATTERN = Pattern.compile("(\\d[\\d,]*(?:\\.\\d+)?)\\s*([kK])?");

    public JobSearchService(JobProvider... providers) {
        if (providers == null || providers.length == 0) {
            throw new IllegalArgumentException("At least one job provider is required.");
        }
        this.providers = providers;
    }

    // Business logic: ask each configured provider for jobs and combine the results.
    public List<JobPosting> searchJobs(String location, String position) {
        return searchJobs(new JobSearchCriteria(location, position, "", "", "", ""));
    }

    public List<JobPosting> searchJobs(JobSearchCriteria criteria) {
        return search(criteria).getJobs();
    }

    public JobSearchOutcome search(JobSearchCriteria criteria) {
        List<JobPosting> jobs = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int failedProviders = 0;

        for (JobProvider provider : providers) {
            try {
                for (JobPosting job : provider.getJobPostings(criteria.getLocation(), criteria.getPosition())) {
                    if (hasTitle(job)) {
                        job.setSource(provider.getSourceName());
                        job.setSeniority(seniorityClassifier.classify(job));
                        job.setWorkType(workTypeClassifier.classify(job));
                        job.setTechRelated(techScopeClassifier.isTechRelated(job));
                        job.setTags(tagClassifier.classify(job));
                    }
                    if (hasTitle(job) && job.isTechRelated() && matchesCriteria(job, criteria)) {
                        jobs.add(job);
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
        switch (criteria.getSortOption()) {
            case NEWEST:
                result = compareDescending(parsePostedDate(first.getPostedDate()), parsePostedDate(second.getPostedDate()));
                break;
            case SALARY:
                result = compareDescending(extractSalaryAmount(first.getSalary()), extractSalaryAmount(second.getSalary()));
                break;
            case COMPANY:
                result = compareAscending(first.getCompanyName(), second.getCompanyName());
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
        return job != null && job.getTitle() != null && !job.getTitle().isBlank();
    }

    private boolean matchesCriteria(JobPosting job, JobSearchCriteria criteria) {
        return matchesPosition(job, criteria.getPosition())
                && matchesPartialValue(job.getCategory(), criteria.getCategory())
                && matchesValue(job.getSeniority(), criteria.getSeniority())
                && matchesValue(job.getWorkType(), criteria.getWorkType())
                && matchesTag(job, criteria.getTag());
    }

    private boolean matchesPosition(JobPosting job, String searchQuery) {
        if (searchQuery.isEmpty()) {
            return true;
        }

        String searchableText = normalize(job.getTitle())
                + " " + normalize(job.getDescription())
                + " " + normalize(job.getCategory());

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
        for (String jobTag : job.getTags()) {
            if (normalize(jobTag).equals(normalize(tag))) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
