package search;

import java.util.ArrayList;
import java.util.List;

import domain.JobPosting;

public record JobSearchOutcome(List<JobPosting> jobs, List<String> warnings) {

    public JobSearchOutcome {
        jobs = jobs == null ? new ArrayList<>() : jobs;
        warnings = warnings == null ? new ArrayList<>() : warnings;
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}
