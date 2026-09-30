package api.response;

import java.util.ArrayList;
import java.util.List;

public record JobSearchResponse(
        List<JobSearchResult> jobs,
        List<String> warnings    
){    
    public JobSearchResponse {
        jobs = jobs == null ? new ArrayList<>() : jobs;
        warnings = warnings == null ? new ArrayList<>() : warnings;
    }

    public JobSearchResponse(List<JobSearchResult> jobs) {
        this(jobs, new ArrayList<>());
    }
}
