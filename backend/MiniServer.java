import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class MiniServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("🚀 Server running on http://localhost:8080");

        server.createContext("/check", (HttpExchange exchange) -> {
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", "application/json");
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");

            // Handle preflight (OPTIONS) requests
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                String response;
                if (body.contains("\"mode\":\"ai\"")) {
                    String text = extractJsonValue(body, "text1");
                    double aiPercent = PlagiarismCheckerAi.calculateAIScore(text);
                    response = "{\"ai_percent\": " + aiPercent + "}";
                } else {
                    String text1 = extractJsonValue(body, "text1");
                    String text2 = extractJsonValue(body, "text2");
                    double sim = PlagiarismChecker.compareTexts(text1, text2);
                    response = "{\"similarity\": " + sim + "}";
                }

                byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, respBytes.length);
                exchange.getResponseBody().write(respBytes);
                exchange.getResponseBody().close();
            }
        });

        server.start();
    }

    private static String extractJsonValue(String json, String key) {
        int start = json.indexOf("\"" + key + "\":");
        if (start == -1) return "";
        int firstQuote = json.indexOf("\"", start + key.length() + 3);
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        return json.substring(firstQuote + 1, secondQuote);
    }
}
