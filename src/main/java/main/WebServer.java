package main;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import model.AdzunaStrategy;
import model.Model;
import model.Provider;
import vo.JobPosting;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebServer {
    private static Model model;

    public static void main(String[] args) throws IOException {
        AppConfig config = AppConfig.fromEnvironment();
        int port = config.getServerPort();
        String serverUrl = "http://localhost:" + port;

        // Initialize model
        model = new Model(
                new DummyView(),
                new Provider(new AdzunaStrategy(
                        config.getAdzunaAppId(),
                        config.getAdzunaAppKey(),
                        config.getAdzunaCountry()
                ))
        );
        
        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Routes
        server.createContext("/", new StaticFileHandler());
        server.createContext("/search", new SearchHandler());
        
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
        public void handle(HttpExchange exchange) throws IOException{
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());

            String location = params.getOrDefault("location", "Dallas, TX");
            String position = params.getOrDefault("position", "");

            System.out.println("Search request: location=" + location + ", position=" + position);

            List<JobPosting> jobs = model.getJobPostings(location, position);
            String jsonResponse = convertToJson(jobs);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(jsonResponse.getBytes());
            os.close();
        }
        

        private Map<String, String> parseQuery(String query){
            Map<String, String> result = new HashMap<>();
            if(query == null) return result;
            
            for(String param : query.split("&")){
                String[] pair = param.split("=");
                if(pair.length > 1){
                    try{
                        result.put(
                            URLDecoder.decode(pair[0], "UTF-8"), 
                            URLDecoder.decode(pair[1], "UTF-8")
                        );
                    } catch(UnsupportedEncodingException e){
                        e.printStackTrace();
                    }
                }
            }
            return result;
        }
        
        private String convertToJson(List<JobPosting> jobs) {
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < jobs.size(); i++) {
                    JobPosting job = jobs.get(i);
                    json.append("{");
                    json.append("\"title\":\"").append(escapeJson(job.getTitle())).append("\",");
                    json.append("\"company\":\"").append(escapeJson(job.getCompanyName())).append("\",");
                    json.append("\"location\":\"").append(escapeJson(job.getCity())).append("\",");
                    json.append("\"url\":\"").append(escapeJson(job.getUrl())).append("\",");
                    json.append("\"website\":\"").append(escapeJson(job.getWebsiteName())).append("\"");
                    json.append("}");
                    if (i < jobs.size() - 1) json.append(",");
                }
                json.append("]");
                return json.toString();
        }

        private String escapeJson(String str) {
            if (str == null) return "";
            return str.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n");
        }
    }

    static class DummyView implements view.View {
        @Override
        public void update(List<JobPosting> vacancies) {}
        
        @Override
        public void setController(Controller controller) {}
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
