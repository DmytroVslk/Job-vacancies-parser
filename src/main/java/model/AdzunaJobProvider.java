package model;

import vo.JobPosting;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AdzunaJobProvider implements JobProvider {

    private static final String API_BASE = "https://api.adzuna.com/v1/api/jobs/";
    private static final int MAX_PAGES = 5;
    private static final int PAGE_SIZE = 50;

    private final String appId;
    private final String appKey;
    private final String country;

    public AdzunaJobProvider(String appId, String appKey, String country) {
        this.appId = appId;
        this.appKey = appKey;
        this.country = country;
    }

    @Override
    public List<JobPosting> getJobPostings(String location) {
        return getJobPostings(location, "");
    }

    @Override
    public List<JobPosting> getJobPostings(String location, String position) {
        List<JobPosting> allVacancies = new ArrayList<>();

        try {
            String searchQuery = buildSearchQuery(position);
            String encodedLocation = encode(location);
            String encodedSearchQuery = encode(searchQuery);

            for (int page = 1; page <= MAX_PAGES; page++) {
                String urlString = API_BASE + encode(country) + "/search/" + page
                        + "?app_id=" + encode(appId)
                        + "&app_key=" + encode(appKey)
                        + "&what=" + encodedSearchQuery
                        + "&where=" + encodedLocation
                        + "&results_per_page=" + PAGE_SIZE
                        + "&content-type=application/json";

                System.out.println("Requesting Adzuna page " + page
                        + " for location=" + location
                        + ", position=" + searchQuery);

                URL url = URI.create(urlString).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                int status = conn.getResponseCode();
                if (status != 200) {
                    System.out.println("HTTP error: " + status);
                    break;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray results = json.optJSONArray("results");

                if (results == null || results.length() == 0) {
                    System.out.println("No results on page " + page + ", stopping.");
                    break;
                }

                System.out.println("Found " + results.length() + " results on page " + page);

                for (int i = 0; i < results.length(); i++) {
                    JobPosting vacancy = extractJobPosting(results.getJSONObject(i));
                    if (vacancy != null) {
                        allVacancies.add(vacancy);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

        return allVacancies;
    }

    private String buildSearchQuery(String position) {
        if (position == null || position.isBlank()) {
            return "java developer";
        }
        return position.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private JobPosting extractJobPosting(JSONObject job) {
        String title = job.optString("title", "");

        JobPosting vacancy = new JobPosting();
        vacancy.setTitle(title);
        vacancy.setWebsiteName("adzuna.com");

        vacancy.setUrl(job.optString("redirect_url", ""));

        JSONObject company = job.optJSONObject("company");
        vacancy.setCompanyName(company != null ? company.optString("display_name", "Unknown") : "Unknown");

        JSONObject locationObj = job.optJSONObject("location");
        String city = "";
        if (locationObj != null) {
            JSONArray displayParts = locationObj.optJSONArray("area");
            if (displayParts != null && displayParts.length() > 0) {
                city = displayParts.getString(displayParts.length() - 1);
            }
            if (city.isEmpty()) {
                city = locationObj.optString("display_name", "");
            }
        }
        vacancy.setCity(city);

        return vacancy;
    }
}
