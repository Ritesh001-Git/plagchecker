package com.detector.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.detector.model.AIDetectionResult;
import com.detector.service.AIContentDetectorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class MiniServer {

    private static final Logger logger = LoggerFactory.getLogger(MiniServer.class);

    // IMPORTANT: relative path for Docker/EC2
    private static final String FRONTEND_DIR = "./frontend";

    // Dynamic port (cloud compatible)
    private static final int PORT = Integer.parseInt(
            System.getenv().getOrDefault("PORT", "8080")
    );

    private static AIContentDetectorService aiDetectorService;
    private static ObjectMapper objectMapper;

    public static void main(String[] args) throws Exception {

        aiDetectorService = new AIContentDetectorService();
        objectMapper = new ObjectMapper();

        // CRITICAL: bind to 0.0.0.0 (not localhost)
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

        logger.info("🚀 Server starting on port {}", PORT);

        server.createContext("/", MiniServer::handleStaticFiles);
        server.createContext("/check", MiniServer::handleCheckRequest);
        server.createContext("/health", MiniServer::handleHealthCheck);
        server.createContext("/stats", MiniServer::handleStats);

        server.setExecutor(null);
        server.start();

        logger.info("✅ Server running on port {}", PORT);

        // ❌ Removed browser auto-open (breaks in EC2/Docker)

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server...");
            aiDetectorService.shutdown();
            server.stop(0);
        }));
    }

    private static void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";

        File baseDir = new File(FRONTEND_DIR).getCanonicalFile();
        File file = new File(baseDir, path).getCanonicalFile();

        // Security check
        if (!file.getPath().startsWith(baseDir.getPath())) {
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
            exchange.sendResponseHeaders(404, notFound.length());
            exchange.getResponseBody().write(notFound.getBytes());
        }

        exchange.getResponseBody().close();
    }

    private static void handleCheckRequest(HttpExchange exchange) throws IOException {

        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "POST, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }

        try {
            String requestBody = readRequestBody(exchange);
            JSONObject request = new JSONObject(requestBody);
            String mode = request.optString("mode", "plagiarism");

            JSONObject response;

            if ("ai".equalsIgnoreCase(mode)) {
                response = handleAIDetection(request);
            } else {
                response = handlePlagiarismCheck(request);
            }

            sendJsonResponse(exchange, 200, response.toString());

        } catch (IllegalArgumentException e) {
            sendJsonResponse(exchange, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing request", e);
            sendJsonResponse(exchange, 500, Map.of("error", "Internal server error"));
        }
    }

    private static JSONObject handleAIDetection(JSONObject request) {

        String text = request.optString("text1", "");

        if (text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        AIDetectionResult result = aiDetectorService.detectAIContent(text);

        JSONObject response = new JSONObject();
        response.put("ai_percent", round(result.getAiProbability()));
        response.put("confidence", result.getConfidence());
        response.put("classification", result.getClassification());

        Map<String, Double> features = result.getFeatures();

        response.put("diversity", round(features.getOrDefault("diversity", 0.0)));
        response.put("repetition", round(features.getOrDefault("repetition", 0.0)));
        response.put("uniformity", round(features.getOrDefault("uniformity", 0.0)));
        response.put("burstiness", round(features.getOrDefault("burstiness", 0.0)));
        response.put("keywords", round(features.getOrDefault("keywords", 0.0)));
        response.put("perplexity", round(features.getOrDefault("perplexity", 0.0)));
        response.put("entropy", round(features.getOrDefault("entropy", 0.0)));

        return response;
    }

    private static JSONObject handlePlagiarismCheck(JSONObject request) {

        String text1 = request.optString("text1", "");
        String text2 = request.optString("text2", "");

        if (text1.trim().isEmpty() || text2.trim().isEmpty()) {
            throw new IllegalArgumentException("Both texts are required");
        }

        Map<String, Double> details = PlagiarismChecker.getDetailedSimilarity(text1, text2);

        JSONObject response = new JSONObject();
        response.put("similarity", round(details.getOrDefault("overall", 0.0)));
        response.put("jaccard", round(details.getOrDefault("jaccard", 0.0)));
        response.put("cosine", round(details.getOrDefault("cosine", 0.0)));
        response.put("lcs", round(details.getOrDefault("lcs", 0.0)));
        response.put("ngram", round(details.getOrDefault("ngram", 0.0)));

        return response;
    }

    private static void handleHealthCheck(HttpExchange exchange) throws IOException {
        JSONObject health = new JSONObject();
        health.put("status", aiDetectorService.isHealthy() ? "healthy" : "unhealthy");
        sendJsonResponse(exchange, 200, health.toString());
    }

    private static void handleStats(HttpExchange exchange) throws IOException {
        Map<String, Object> stats = aiDetectorService.getStatistics();
        sendJsonResponse(exchange, 200, new JSONObject(stats).toString());
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), "UTF-8");
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Object body) throws IOException {
        String jsonResponse = body instanceof String ? (String) body : new JSONObject(body).toString();
        byte[] bytes = jsonResponse.getBytes("UTF-8");

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
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
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }
}