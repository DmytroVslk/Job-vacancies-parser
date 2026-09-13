package deduplication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import domain.JobPosting;

public class ExactJobDuplicateDetector implements JobDuplicateDetector {

    @Override
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
                || isMissing(job.title())
                || isMissing(job.companyName())
                || isMissing(job.city())
                || "unknown".equals(normalize(job.companyName()))) {
            return null;
        }

        return new DuplicateKey(
                normalize(job.title()),
                normalize(job.companyName()),
                normalize(job.city())
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
