import com.sun.net.httpserver.*;
import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Map;

import org.json.JSONObject;

public class MiniServer {

    private static final String FRONTEND_DIR = "../frontend";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("🚀 Server running on http://localhost:8080");

        // Serve static files
        server.createContext("/", (HttpExchange exchange) -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            File file = new File(FRONTEND_DIR + path).getCanonicalFile();

            if (!file.getPath().startsWith(new File(FRONTEND_DIR).getCanonicalPath())) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }

            if (file.exists() && !file.isDirectory()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().set("Content-Type", getContentType(file.getName()));
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            } else {
                String notFound = "404 Not Found";
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(404, notFound.length());
                exchange.getResponseBody().write(notFound.getBytes());
            }
            exchange.getResponseBody().close();
        });

        // /check API
        server.createContext("/check", (HttpExchange exchange) -> {
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", "application/json");
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body;
                try (InputStream is = exchange.getRequestBody()) {
                    body = new String(is.readAllBytes(), "UTF-8");
                }

                String responseJson;
                try {
                    JSONObject req = new JSONObject(body);
                    String mode = req.optString("mode", "plagiarism");

                    if ("ai".equalsIgnoreCase(mode)) {
                        // AI detection with detailed metrics
                        String text = req.optString("text1", "");
                        // JSONObject out = new JSONObject();
                        
                        Map<String, Double> aiMetrics = PlagiarismCheckerAi.calculateAIScoreDetailed(text);
                        JSONObject out = new JSONObject();
                        out.put("ai_percent", round(aiMetrics.get("ai_percent")));
                        out.put("diversity", round(aiMetrics.get("diversity")));
                        out.put("repetition", round(aiMetrics.get("repetition")));
                        out.put("uniformity", round(aiMetrics.get("uniformity")));
                        out.put("burstiness", round(aiMetrics.get("burstiness")));
                        out.put("keywords", round(aiMetrics.get("keywords")));
                        
                        responseJson = out.toString();
                        
                        // Debug logging
                        System.out.println("AI Detection Result: " + round(aiMetrics.get("ai_percent")) + "%");
                        System.out.println("  Diversity: " + round(aiMetrics.get("diversity")));
                        System.out.println("  Burstiness: " + round(aiMetrics.get("burstiness")));
                        
                    } else {
                        // Plagiarism mode
                        String text1 = req.optString("text1", "");
                        String text2 = req.optString("text2", "");
                        Map<String, Double> details = PlagiarismChecker.getDetailedSimilarity(text1, text2);

                        JSONObject out = new JSONObject();
                        out.put("similarity", round(details.getOrDefault("overall", 0.0)));
                        out.put("jaccard", round(details.getOrDefault("jaccard", 0.0)));
                        out.put("cosine", round(details.getOrDefault("cosine", 0.0)));
                        out.put("lcs", round(details.getOrDefault("lcs", 0.0)));
                        out.put("ngram", round(details.getOrDefault("ngram", 0.0)));

                        responseJson = out.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JSONObject err = new JSONObject();
                    err.put("error", "Invalid input: " + e.getMessage());
                    responseJson = err.toString();
                }

                byte[] respBytes = responseJson.getBytes("UTF-8");
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(200, respBytes.length);
                exchange.getResponseBody().write(respBytes);
                exchange.getResponseBody().close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.start();

        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI("http://localhost:8080/index.html"));
            } catch (Exception e) {
                System.err.println("Failed to open browser: " + e.getMessage());
            }
        }
    }

    private static double round(Double val) {
        if (val == null) return 0.0;
        return Math.round(val * 100.0) / 100.0;
    }

    private static String getContentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".json")) return "application/json";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }
}