package deduplication;

import java.util.List;

import domain.JobPosting;

public interface JobDuplicateDetector {
    List<JobPosting> removeDuplicates(List<JobPosting> jobs);
}
