package model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import vo.JobPosting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JoobleJobProvider implements JobProvider {

    private static final String API_BASE = "https://jooble.org/api/";
    private static final String SOURCE_NAME = "Jooble";
    private static final int MAX_PAGES = 5;
    private static final int PAGE_SIZE = 50;
    private static final int TIMEOUT_MS = 10000;
    private static final String BROAD_TECH_KEYWORDS = "software OR developer OR engineer OR data OR devops OR qa";

    private final String apiKey;

    public JoobleJobProvider(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Jooble API key is required.");
        }
        this.apiKey = apiKey.trim();
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<JobPosting> getJobPostings(String location, String position) {
        List<JobPosting> allVacancies = new ArrayList<>();

        try {
            String keywords = resolveKeywords(position);
            for (int page = 1; page <= MAX_PAGES; page++) {
                System.out.println("Requesting Jooble page " + page
                        + " for location=" + location
                        + ", keywords=" + keywords);

                HttpURLConnection conn = openConnection();
                writeRequest(conn, location, keywords, page);

                int status = conn.getResponseCode();
                if (status != 200) {
                    throw new ProviderException("Jooble returned HTTP " + status + " on page " + page);
                }

                JSONObject response = new JSONObject(readResponse(conn));
                JSONArray results = response.optJSONArray("jobs");
                if (results == null || results.length() == 0) {
                    System.out.println("No Jooble results on page " + page + ", stopping.");
                    break;
                }

                System.out.println("Found " + results.length() + " Jooble results on page " + page);
                for (int i = 0; i < results.length(); i++) {
                    allVacancies.add(extractJobPosting(results.getJSONObject(i)));
                }

                if (results.length() < PAGE_SIZE) {
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Jooble timeout: " + e.getClass().getSimpleName());
            throw new ProviderException("Jooble request timed out.", e);
        } catch (JSONException e) {
            System.out.println("Jooble JSON parsing error: " + e.getClass().getSimpleName());
            throw new ProviderException("Unable to parse Jooble response.", e);
        } catch (IOException e) {
            System.out.println("Jooble network error: " + e.getClass().getSimpleName());
            throw new ProviderException("Unable to connect to Jooble.", e);
        } catch (ProviderException e) {
            throw e;
        } catch (RuntimeException e) {
            System.out.println("Jooble provider error: " + e.getClass().getSimpleName());
            throw e;
        }

        return allVacancies;
    }

    private HttpURLConnection openConnection() throws IOException {
        URL url = URI.create(API_BASE + encode(apiKey)).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setDoOutput(true);
        return conn;
    }

    private void writeRequest(HttpURLConnection conn, String location, String keywords, int page) throws IOException {
        JSONObject request = new JSONObject();
        request.put("keywords", clean(keywords));
        request.put("location", clean(location));
        request.put("page", page);
        request.put("ResultOnPage", PAGE_SIZE);
        request.put("companysearch", false);

        byte[] body = request.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream output = conn.getOutputStream()) {
            output.write(body);
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private String resolveKeywords(String position) {
        String cleanedPosition = clean(position);
        return cleanedPosition.isEmpty() ? BROAD_TECH_KEYWORDS : cleanedPosition;
    }

    private JobPosting extractJobPosting(JSONObject job) {
        JobPosting vacancy = new JobPosting();
        String type = job.optString("type", "");

        vacancy.setTitle(clean(job.optString("title", "")));
        vacancy.setCompanyName(cleanOrDefault(job.optString("company", ""), "Unknown"));
        vacancy.setCity(clean(job.optString("location", "")));
        vacancy.setWebsiteName(cleanOrDefault(job.optString("source", ""), "jooble.org"));
        vacancy.setSource(getSourceName());
        vacancy.setUrl(clean(job.optString("link", "")));
        vacancy.setDescription(cleanDescription(job.optString("snippet", "")));
        vacancy.setSalary(clean(job.optString("salary", "")));
        vacancy.setEmploymentType(normalizeEmploymentType(type));
        vacancy.setEmploymentSchedule(normalizeEmploymentSchedule(type));
        return vacancy;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String cleanOrDefault(String value, String defaultValue) {
        String cleanedValue = clean(value);
        return cleanedValue.isEmpty() ? defaultValue : cleanedValue;
    }

    private String cleanDescription(String value) {
        return clean(value)
                .replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String normalizeEmploymentType(String type) {
        String normalizedType = normalize(type);
        if (normalizedType.contains("contract") || normalizedType.contains("freelance")) {
            return "contract";
        }
        if (normalizedType.contains("permanent")) {
            return "permanent";
        }
        return "";
    }

    private String normalizeEmploymentSchedule(String type) {
        String normalizedType = normalize(type);
        if (normalizedType.contains("full-time")) {
            return "full-time";
        }
        if (normalizedType.contains("part-time")) {
            return "part-time";
        }
        return "";
    }

    private String normalize(String value) {
        return clean(value).toLowerCase(Locale.ROOT).replace('_', '-').replace(' ', '-');
    }
}
