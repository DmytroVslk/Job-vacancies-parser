package deduplication;

import java.util.List;

import domain.JobPosting;

public class ExactJobDuplicateDetector implements JobDuplicateDetector {

    @Override
    public List<JobPosting> removeDuplicates(List<JobPosting> jobs) {
        return jobs.stream()
                .filter(new DistinctJobPredicate())
                .toList();
    }

}
