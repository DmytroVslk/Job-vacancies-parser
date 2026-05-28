package model;

import vo.JobPosting;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdzunaJobProvider implements JobProvider {

    private static final String API_BASE = "https://api.adzuna.com/v1/api/jobs/";
    private static final int MAX_PAGES = 5;
    private static final int PAGE_SIZE = 50;
    private static final int TIMEOUT_MS = 10000;
    private static final String IT_CATEGORY = "it-jobs";
    private static final String SOURCE_NAME = "Adzuna";

    private final String appId;
    private final String appKey;
    private final String country;

    public AdzunaJobProvider(String appId, String appKey, String country) {
        this.appId = appId;
        this.appKey = appKey;
        this.country = country;
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<JobPosting> getJobPostings(String location, String position) {
        List<JobPosting> allVacancies = new ArrayList<>();

        try {
            String searchQuery = cleanPosition(position);
            String encodedLocation = encode(location);

            for (int page = 1; page <= MAX_PAGES; page++) {
                String urlString = API_BASE + encode(country) + "/search/" + page
                        + "?app_id=" + encode(appId)
                        + "&app_key=" + encode(appKey)
                        + "&where=" + encodedLocation
                        + "&results_per_page=" + PAGE_SIZE
                        + "&content-type=application/json";
                if (!searchQuery.isEmpty()) {
                    urlString += "&what=" + encode(searchQuery);
                } else {
                    urlString += "&category=" + encode(IT_CATEGORY);
                }

                System.out.println("Requesting Adzuna page " + page
                        + " for location=" + location
                        + ", position=" + searchQuery);

                URL url = URI.create(urlString).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);

                int status = conn.getResponseCode();
                if (status != 200) {
                    throw new ProviderException("Adzuna returned HTTP " + status + " on page " + page);
                }

                String response = readResponse(conn);
                JSONObject json = new JSONObject(response);
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
        } catch (SocketTimeoutException e) {
            System.out.println("Adzuna timeout: " + e.getClass().getSimpleName());
            throw new ProviderException("Adzuna request timed out.", e);
        } catch (JSONException e) {
            System.out.println("Adzuna JSON parsing error: " + e.getClass().getSimpleName());
            throw new ProviderException("Unable to parse Adzuna response.", e);
        } catch (IOException e) {
            System.out.println("Adzuna network error: " + e.getClass().getSimpleName());
            throw new ProviderException("Unable to connect to Adzuna.", e);
        } catch (ProviderException e) {
            throw e;
        } catch (RuntimeException e) {
            System.out.println("Adzuna provider error: " + e.getClass().getSimpleName());
            throw e;
        }

        return allVacancies;
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private String cleanPosition(String position) {
        return position == null ? "" : position.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private JobPosting extractJobPosting(JSONObject job) {
        String title = job.optString("title", "");

        JobPosting vacancy = new JobPosting();
        vacancy.setTitle(title);
        vacancy.setWebsiteName("adzuna.com");
        vacancy.setSource(getSourceName());
        vacancy.setUrl(job.optString("redirect_url", ""));
        vacancy.setDescription(job.optString("description", ""));
        vacancy.setEmploymentType(normalizeContractType(job.optString("contract_type", "")));
        vacancy.setEmploymentSchedule(normalizeContractTime(job.optString("contract_time", "")));

        JSONObject company = job.optJSONObject("company");
        vacancy.setCompanyName(company != null ? company.optString("display_name", "Unknown") : "Unknown");

        JSONObject category = job.optJSONObject("category");
        vacancy.setCategory(category != null ? category.optString("label", "") : "");

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

    private String normalizeContractType(String contractType) {
        return contractType == null ? "" : contractType.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private String normalizeContractTime(String contractTime) {
        return contractTime == null ? "" : contractTime.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
