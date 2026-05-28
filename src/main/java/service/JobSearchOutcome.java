package service;

import vo.JobPosting;

import java.util.ArrayList;
import java.util.List;

public class JobSearchOutcome {

    private final List<JobPosting> jobs;
    private final List<String> warnings;

    public JobSearchOutcome(List<JobPosting> jobs, List<String> warnings) {
        this.jobs = jobs == null ? new ArrayList<>() : jobs;
        this.warnings = warnings == null ? new ArrayList<>() : warnings;
    }

    public List<JobPosting> getJobs() {
        return jobs;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}
