package deduplication;
import java.util.function.Predicate;
import domain.JobPosting;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class DistinctJobPredicate implements Predicate<JobPosting> {
    private final Set<DuplicateKey> seenKeys = new HashSet<>();

    @Override 
    public boolean test(JobPosting job) {
        DuplicateKey key = createKey(job);
        return key == null || seenKeys.add(key);
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
