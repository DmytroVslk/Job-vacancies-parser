package service;

import vo.JobPosting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class JobDuplicateDetector {

    public List<JobPosting> removeDuplicates(List<JobPosting> jobs) {
        List<JobPosting> uniqueJobs = new ArrayList<>();
        Set<DuplicateKey> seenKeys = new HashSet<>();

        for (JobPosting job : jobs) {
            DuplicateKey key = createKey(job);
            if (key == null || seenKeys.add(key)) {
                uniqueJobs.add(job);
            }
        }

        return uniqueJobs;
    }

    private DuplicateKey createKey(JobPosting job) {
        if (job == null
                || isMissing(job.getTitle())
                || isMissing(job.getCompanyName())
                || isMissing(job.getCity())
                || "unknown".equals(normalize(job.getCompanyName()))) {
            return null;
        }

        return new DuplicateKey(
                normalize(job.getTitle()),
                normalize(job.getCompanyName()),
                normalize(job.getCity())
        );
    }

    private boolean isMissing(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private record DuplicateKey(String title, String company, String location) {
    }
}
