package service;

import model.Provider;
import vo.JobPosting;

import java.util.ArrayList;
import java.util.List;

public class JobSearchService {

    private final Provider[] providers;

    public JobSearchService(Provider... providers) {
        if (providers == null || providers.length == 0) {
            throw new IllegalArgumentException("At least one job provider is required.");
        }
        this.providers = providers;
    }

    // Business logic: ask each configured provider for jobs and combine the results.
    public List<JobPosting> searchJobs(String location, String position) {
        String searchQuery = buildSearchQuery(position);
        List<JobPosting> jobs = new ArrayList<>();
        for (Provider provider : providers) {
            for (JobPosting job : provider.getJobPostings(location, position)) {
                if (hasTitle(job)) {
                    jobs.add(job);
                }
            }
        }
        jobs.sort((a, b) -> titleScore(b.getTitle(), searchQuery) - titleScore(a.getTitle(), searchQuery));
        return jobs;
    }

    private String buildSearchQuery(String position) {
        if (position == null || position.isBlank()) {
            return "java developer";
        }
        return position.trim();
    }

    private boolean hasTitle(JobPosting job) {
        return job != null && job.getTitle() != null && !job.getTitle().isBlank();
    }

    private int titleScore(String title, String searchQuery) {
        if (title == null) {
            return 0;
        }

        String t = title.toLowerCase();
        String query = searchQuery.toLowerCase();
        int score = 0;

        if (t.contains(query)) {
            score += 5;
        }

        for (String word : query.split("\\s+")) {
            if (!word.isBlank() && t.contains(word)) {
                score += 2;
            }
        }

        if (t.contains("developer"))        score += 1;
        if (t.contains("engineer"))         score += 1;
        if (t.contains("software"))         score += 1;
        if (t.contains("junior"))           score += 1;
        if (t.contains("entry level"))      score += 1;
        return score;
    }
}
