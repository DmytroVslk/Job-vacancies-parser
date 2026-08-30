package model;

import vo.JobPosting;

import java.util.List;

/**
 * Adapter contract for any external job data source.
 *
 * Provider implementations translate a generic location and optional role
 * query into their API-specific request and return normalized job postings.
 */
public interface JobProvider {
    /**
     * Returns the source label shown for jobs returned by this provider.
     */
    String getSourceName();

    /**
     * Retrieves job postings for the selected location and role query.
     *
     * @param location required search location
     * @param position optional role keywords; blank means a broad search in the current IT/tech scope
     * @return postings mapped to the common {@link JobPosting} model
     */
    List<JobPosting> getJobPostings(String location, String position);
}
