package search;

import domain.JobPosting;
import java.util.List;

public interface JobSearchService {
    List<JobPosting> searchJobs(String location, String position);

    List<JobPosting> searchJobs(JobSearchCriteria criteria);

    JobSearchOutcome search (JobSearchCriteria criteria);
}
