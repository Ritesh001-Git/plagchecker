import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

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

            // Handle CORS preflight
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                String response;
                try {
                    if (body.contains("\"mode\":\"ai\"")) {
                        String text = extractValue(body, "text1");
                        double aiPercent = PlagiarismCheckerAi.calculateAIScore(text);
                        response = "{\"ai_percent\": " + aiPercent + "}";
                    } else {
                        String text1 = extractValue(body, "text1");
                        String text2 = extractValue(body, "text2");
                        double sim = PlagiarismChecker.compareTexts(text1, text2);
                        response = "{\"similarity\": " + sim + "}";
                    }
                } catch (Exception e) {
                    response = "{\"error\":\"Invalid input format or server issue.\"}";
                }

                byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, respBytes.length);
                exchange.getResponseBody().write(respBytes);
                exchange.getResponseBody().close();
            }
        });

        server.start();
    }

    // ✅ Regex-based safe extractor that handles multi-line JSON values
    private static String extractValue(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"", Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1).replace("\\n", "\n");
        return "";
    }
}
