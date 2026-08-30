package response;

import java.util.ArrayList;
import java.util.List;

public class JobSearchResponse {
    private final boolean success;
    private final int count;
    private final List<JobSearchResult> jobs;
    private final List<String> warnings;

    public JobSearchResponse(List<JobSearchResult> jobs) {
        this(jobs, new ArrayList<>());
    }

    public JobSearchResponse(List<JobSearchResult> jobs, List<String> warnings) {
        this.success = true;
        this.jobs = jobs == null ? new ArrayList<>() : jobs;
        this.count = this.jobs.size();
        this.warnings = warnings == null ? new ArrayList<>() : warnings;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }

    public List<JobSearchResult> getJobs() {
        return jobs;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}
