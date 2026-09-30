package api.response;

import domain.JobPosting;

public record JobSearchResult (
        String title,
        String company,
        String location,
        String url,
        String website,
        String source,
        String salary
) {
    
    public static JobSearchResult from(JobPosting job) {
        return new JobSearchResult(
                job.title(),
                job.companyName(),
                job.city(),
                job.url(),
                job.websiteName(),
                job.source(),
                job.salary()
        );
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }
}
