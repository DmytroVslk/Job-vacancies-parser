package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import model.AdzunaJobProvider;
import model.ProviderException;
import response.ErrorResponse;
import response.JobSearchResponse;
import response.JobSearchResult;
import service.JobSearchCriteria;
import service.JobSearchService;
import service.JobSortOption;
import vo.JobPosting;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebServer {

    public static void main(String[] args) throws IOException {
        AppConfig config = AppConfig.fromEnvironment();
        int port = config.getServerPort();
        String serverUrl = "http://localhost:" + port;

        JobSearchService jobSearchService = new JobSearchService(
                new AdzunaJobProvider(
                        config.getAdzunaAppId(),
                        config.getAdzunaAppKey(),
                        config.getAdzunaCountry()
                )
        );
        
        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Routes
        server.createContext("/", new StaticFileHandler());
        server.createContext("/search", new SearchHandler(jobSearchService));
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("🚀 Server started at " + serverUrl);
        System.out.println("📂 Serving files from: src/main/resources/view/");
        System.out.println("Press Ctrl+C to stop");
        
        // Automatically open browser
        openBrowser(serverUrl);
    }

    static class StaticFileHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            String path = exchange.getRequestURI().getPath();

            if(path.equals("/")){
                path = "/index.html";
            }

            String filePath = "src/main/resources/view" + path;
            File file = new File(filePath);

            if(file.exists()){
                String contentType = getContentType(filePath);
                byte[] content = Files.readAllBytes(file.toPath());

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, content.length);
                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            } else {
                String response = "404 (Not Found)/n";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            return "text/plain";
        }
    }

    public static class SearchHandler implements HttpHandler{
        private final JobSearchService jobSearchService;
        private final ObjectMapper objectMapper = new ObjectMapper();

        public SearchHandler(JobSearchService jobSearchService) {
            this.jobSearchService = jobSearchService;
        }

        public void handle(HttpExchange exchange) throws IOException{
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());

            String location = params.get("location");
            String position = params.getOrDefault("position", "");
            String category = params.getOrDefault("category", "");
            String seniority = params.getOrDefault("seniority", "");
            String workType = params.getOrDefault("workType", "");
            String tag = params.getOrDefault("tag", "");
            String preferredSeniority = params.getOrDefault("preferredSeniority", "");
            String preferredWorkType = params.getOrDefault("preferredWorkType", "");
            String preferredEmploymentType = params.getOrDefault("preferredEmploymentType", "");
            String preferredEmploymentSchedule = params.getOrDefault("preferredEmploymentSchedule", "");
            String sort = params.getOrDefault("sort", "");

            if (location == null || location.isBlank()) {
                sendJson(exchange, 400, new ErrorResponse("Location is required."));
                return;
            }

            location = location.trim();
            position = position.trim();
            JobSortOption sortOption;
            try {
                sortOption = JobSortOption.fromApiValue(sort);
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400, new ErrorResponse("Unsupported sort option. Supported value: relevance."));
                return;
            }
            JobSearchCriteria criteria = new JobSearchCriteria(
                    location,
                    position,
                    category,
                    seniority,
                    workType,
                    tag,
                    preferredSeniority,
                    preferredWorkType,
                    preferredEmploymentType,
                    preferredEmploymentSchedule,
                    sortOption
            );
            System.out.println("Search request: location=" + location
                    + ", position=" + position
                    + ", seniority=" + criteria.getSeniority()
                    + ", preferredSeniority=" + criteria.getPreferredSeniority()
                    + ", workType=" + criteria.getWorkType()
                    + ", preferredWorkType=" + criteria.getPreferredWorkType()
                    + ", preferredEmploymentType=" + criteria.getPreferredEmploymentType()
                    + ", preferredEmploymentSchedule=" + criteria.getPreferredEmploymentSchedule()
                    + ", sort=" + criteria.getSortOption().getApiValue());

            try {
                List<JobPosting> jobs = jobSearchService.searchJobs(criteria);
                List<JobSearchResult> jobResults = toResponse(jobs);
                sendJson(exchange, 200, new JobSearchResponse(jobResults));
            } catch (ProviderException e) {
                System.out.println("Job provider error: " + e.getMessage());
                sendJson(exchange, 500, new ErrorResponse("Unable to fetch jobs right now. Please try again later."));
            } catch (Exception e) {
                System.out.println("Unexpected search error: " + e.getClass().getSimpleName());
                sendJson(exchange, 500, new ErrorResponse("Unable to fetch jobs right now. Please try again later."));
            }
        }
        

        private Map<String, String> parseQuery(String query){
            Map<String, String> result = new HashMap<>();
            if(query == null) return result;
            
            for(String param : query.split("&")){
                String[] pair = param.split("=", 2);
                if(pair.length > 1){
                    try{
                        result.put(
                            URLDecoder.decode(pair[0], "UTF-8"), 
                            URLDecoder.decode(pair[1], "UTF-8")
                        );
                    } catch(UnsupportedEncodingException e){
                        System.out.println("Unable to decode query parameter.");
                    }
                }
            }
            return result;
        }

        private List<JobSearchResult> toResponse(List<JobPosting> jobs) {
            List<JobSearchResult> response = new ArrayList<>();
            for (JobPosting job : jobs) {
                response.add(JobSearchResult.from(job));
            }
            return response;
        }

        private void sendJson(HttpExchange exchange, int statusCode, Object response) throws IOException {
            byte[] jsonResponse = objectMapper.writeValueAsBytes(response);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(statusCode, jsonResponse.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse);
            }
        }
    }
    
    private static void openBrowser(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", url});
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec(new String[]{"xdg-open", url});
            }
        } catch (IOException e) {
            System.out.println("Please open manually: " + url);
        }
    }
}
