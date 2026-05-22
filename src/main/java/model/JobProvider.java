package model;

import vo.JobPosting;

import java.util.List;

public interface JobProvider {
    List<JobPosting> getJobPostings(String searchString);

    default List<JobPosting> getJobPostings(String location, String position) {
        return getJobPostings(location);
    }
}
