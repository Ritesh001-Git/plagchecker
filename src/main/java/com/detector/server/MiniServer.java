package com.detector.server;

import com.sun.net.httpserver.*;
import com.detector.service.AIContentDetectorService;
import com.detector.model.AIDetectionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Map;

/**
 * Lightweight HTTP Server for AI Content Detection and Plagiarism Checking
 */
public class MiniServer {
    
    private static final Logger logger = LoggerFactory.getLogger(MiniServer.class);
    private static final String FRONTEND_DIR = "frontend";
    private static final int PORT = 8080;
    
    private static AIContentDetectorService aiDetectorService;
    private static ObjectMapper objectMapper;

    public static void main(String[] args) throws Exception {
        // Initialize services
        aiDetectorService = new AIContentDetectorService();
        objectMapper = new ObjectMapper();
        
        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        logger.info("🚀 Server starting on http://localhost:{}", PORT);

        // Serve static files
        server.createContext("/", MiniServer::handleStaticFiles);
        
        // API endpoints
        server.createContext("/check", MiniServer::handleCheckRequest);
        server.createContext("/health", MiniServer::handleHealthCheck);
        server.createContext("/stats", MiniServer::handleStats);

        // Start server
        server.start();
        logger.info("✅ Server running on http://localhost:{}", PORT);
        
        // Open browser
        openBrowser();
        
        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server...");
            aiDetectorService.shutdown();
            server.stop(0);
        }));
    }

    /**
     * Handle static file requests (HTML, CSS, JS)
     */
    private static void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";

        File file = new File(FRONTEND_DIR + path).getCanonicalFile();

        // Security check
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
    }

    /**
     * Handle /check API requests (both plagiarism and AI detection)
     */
    private static void handleCheckRequest(HttpExchange exchange) throws IOException {
        // Set CORS headers
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "POST, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");

        // Handle OPTIONS preflight
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
            // Read request body
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
            logger.error("Invalid request: {}", e.getMessage());
            sendJsonResponse(exchange, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing request", e);
            sendJsonResponse(exchange, 500, Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Handle AI detection request
     */
    private static JSONObject handleAIDetection(JSONObject request) {
        String text = request.optString("text1", "");
        
        if (text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        logger.info("Processing AI detection request. Text length: {}", text.length());
        
        // Use the enhanced AI detection service
        AIDetectionResult result = aiDetectorService.detectAIContent(text);
        
        // Build response
        JSONObject response = new JSONObject();
        response.put("ai_percent", round(result.getAiProbability()));
        response.put("confidence", result.getConfidence());
        response.put("classification", result.getClassification());
        
        // Add detailed metrics
        Map<String, Double> features = result.getFeatures();
        response.put("diversity", round(features.getOrDefault("diversity", 0.0)));
        response.put("repetition", round(features.getOrDefault("repetition", 0.0)));
        response.put("uniformity", round(features.getOrDefault("uniformity", 0.0)));
        response.put("burstiness", round(features.getOrDefault("burstiness", 0.0)));
        response.put("keywords", round(features.getOrDefault("keywords", 0.0)));
        response.put("perplexity", round(features.getOrDefault("perplexity", 0.0)));
        response.put("entropy", round(features.getOrDefault("entropy", 0.0)));
        
        logger.info("AI Detection complete. Score: {:.2f}%, Confidence: {}", 
                   result.getAiProbability(), result.getConfidence());
        
        return response;
    }

    /**
     * Handle plagiarism check request
     */
    private static JSONObject handlePlagiarismCheck(JSONObject request) {
        String text1 = request.optString("text1", "");
        String text2 = request.optString("text2", "");
        
        if (text1.trim().isEmpty() || text2.trim().isEmpty()) {
            throw new IllegalArgumentException("Both texts are required for plagiarism check");
        }

        logger.info("Processing plagiarism check. Text1: {} chars, Text2: {} chars", 
                   text1.length(), text2.length());
        
        // Use original plagiarism checker
        Map<String, Double> details = PlagiarismChecker.getDetailedSimilarity(text1, text2);

        JSONObject response = new JSONObject();
        response.put("similarity", round(details.getOrDefault("overall", 0.0)));
        response.put("jaccard", round(details.getOrDefault("jaccard", 0.0)));
        response.put("cosine", round(details.getOrDefault("cosine", 0.0)));
        response.put("lcs", round(details.getOrDefault("lcs", 0.0)));
        response.put("ngram", round(details.getOrDefault("ngram", 0.0)));

        logger.info("Plagiarism check complete. Similarity: {:.2f}%", 
                   details.getOrDefault("overall", 0.0));
        
        return response;
    }

    /**
     * Health check endpoint
     */
    private static void handleHealthCheck(HttpExchange exchange) throws IOException {
        JSONObject health = new JSONObject();
        health.put("status", aiDetectorService.isHealthy() ? "healthy" : "unhealthy");
        health.put("service", "AI Content Detector");
        health.put("version", "1.0.0");
        
        sendJsonResponse(exchange, 200, health.toString());
    }

    /**
     * Statistics endpoint
     */
    private static void handleStats(HttpExchange exchange) throws IOException {
        Map<String, Object> stats = aiDetectorService.getStatistics();
        sendJsonResponse(exchange, 200, new JSONObject(stats).toString());
    }

    /**
     * Helper: Read request body
     */
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), "UTF-8");
        }
    }

    /**
     * Helper: Send JSON response
     */
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Object body) throws IOException {
        String jsonResponse = body instanceof String ? (String) body : new JSONObject(body).toString();
        byte[] bytes = jsonResponse.getBytes("UTF-8");
        
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    /**
     * Helper: Round double to 2 decimal places
     */
    private static double round(Double val) {
        if (val == null) return 0.0;
        return Math.round(val * 100.0) / 100.0;
    }

    /**
     * Helper: Get MIME content type
     */
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

    /**
     * Open browser automatically
     */
    private static void openBrowser() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI("http://localhost:" + PORT + "/index.html"));
                logger.info("Browser opened automatically");
            } catch (Exception e) {
                logger.warn("Failed to open browser: {}", e.getMessage());
            }
        }
    }
}
