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

public class AdzunaStrategy implements Strategy {

    private static final String API_BASE = "https://api.adzuna.com/v1/api/jobs/us/search/";
    private static final int MAX_PAGES = 5;
    private static final int PAGE_SIZE = 50;

    private final String appId;
    private final String appKey;

    public AdzunaStrategy(String appId, String appKey) {
        this.appId = appId;
        this.appKey = appKey;
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
                String urlString = API_BASE + page
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

        allVacancies.sort((a, b) -> titleScore(b.getTitle()) - titleScore(a.getTitle()));
        return allVacancies;
    }

    private String buildSearchQuery(String position) {
        if (position == null || position.isBlank()) {
            return "java developer";
        }
        return position.trim() + " java developer";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private JobPosting extractJobPosting(JSONObject job) {
        String title = job.optString("title", "");

        if (!title.toLowerCase().matches(".*\\bjava\\b.*")) {
            return null;
        }

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

    private int titleScore(String title) {
        String t = title.toLowerCase();
        int score = 0;
        if (t.contains("java"))             score += 1;
        if (t.contains("java developer"))   score += 4;
        if (t.contains("java engineer"))    score += 4;
        if (t.contains("java backend"))     score += 3;
        if (t.contains("java software"))    score += 3;
        if (t.contains("java full"))        score += 2;
        if (t.contains("junior"))           score += 1;
        if (t.contains("entry level"))      score += 1;
        return score;
    }
}
