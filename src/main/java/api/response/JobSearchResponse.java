package api.response;

import java.util.ArrayList;
import java.util.List;

public record JobSearchResponse (
        boolean success,
        int count,
        List<JobSearchResult> jobs,
        List<String> warnings    
){
    
    public JobSearchResponse(List<JobSearchResult> jobs) {
        this(jobs, new ArrayList<>());
    }

    public JobSearchResponse (List<JobSearchResult> jobs, List<String> warnings){
        this(    
            true,
            jobs == null ? 0 : jobs.size(),
            jobs == null ? new ArrayList<>() : jobs,
            warnings == null ? new ArrayList<>() : warnings
        );
    }
}
