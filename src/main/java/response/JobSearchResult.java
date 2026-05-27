package response;

import vo.JobPosting;

public class JobSearchResult {
    private final String title;
    private final String company;
    private final String location;
    private final String url;
    private final String website;
    private final String source;

    public JobSearchResult(String title, String company, String location, String url, String website, String source) {
        this.title = clean(title);
        this.company = clean(company);
        this.location = clean(location);
        this.url = clean(url);
        this.website = clean(website);
        this.source = clean(source);
    }

    public static JobSearchResult from(JobPosting job) {
        return new JobSearchResult(
                job.getTitle(),
                job.getCompanyName(),
                job.getCity(),
                job.getUrl(),
                job.getWebsiteName(),
                job.getSource()
        );
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getLocation() {
        return location;
    }

    public String getUrl() {
        return url;
    }

    public String getWebsite() {
        return website;
    }

    public String getSource() {
        return source;
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }
}
