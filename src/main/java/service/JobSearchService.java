package service;

import model.JobProvider;
import vo.JobPosting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JobSearchService {

    private final JobProvider[] providers;
    private final JobSeniorityClassifier seniorityClassifier = new JobSeniorityClassifier();
    private final JobWorkTypeClassifier workTypeClassifier = new JobWorkTypeClassifier();
    private final JobTechScopeClassifier techScopeClassifier = new JobTechScopeClassifier();
    private final JobTagClassifier tagClassifier = new JobTagClassifier();
    private final JobRelevanceScorer relevanceScorer = new JobRelevanceScorer();

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
        List<JobPosting> jobs = new ArrayList<>();
        for (JobProvider provider : providers) {
            for (JobPosting job : provider.getJobPostings(criteria.getLocation(), criteria.getPosition())) {
                if (hasTitle(job)) {
                    job.setSeniority(seniorityClassifier.classify(job));
                    job.setWorkType(workTypeClassifier.classify(job));
                    job.setTechRelated(techScopeClassifier.isTechRelated(job));
                    job.setTags(tagClassifier.classify(job));
                }
                if (hasTitle(job) && job.isTechRelated() && matchesCriteria(job, criteria)) {
                    jobs.add(job);
                }
            }
        }
        sortJobs(jobs, criteria);
        return jobs;
    }

    private void sortJobs(List<JobPosting> jobs, JobSearchCriteria criteria) {
        if (criteria.getSortOption() == JobSortOption.RELEVANCE) {
            jobs.sort((first, second) -> Integer.compare(
                    relevanceScorer.score(second, criteria),
                    relevanceScorer.score(first, criteria)
            ));
        }
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
}
