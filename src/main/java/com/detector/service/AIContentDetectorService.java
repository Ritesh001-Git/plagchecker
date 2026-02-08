package com.detector.service;

import com.detector.model.AIDetectionResult;
import com.detector.ml.ModelPredictor;
import com.detector.nlp.FeatureExtractor;
import com.detector.nlp.TextPreprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Main AI Content Detection Service
 * Thread-safe, production-ready service for detecting AI-generated content
 */
public class AIContentDetectorService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIContentDetectorService.class);
    
    private final TextPreprocessor preprocessor;
    private final FeatureExtractor featureExtractor;
    private final ModelPredictor modelPredictor;
    private final ExecutorService executorService;
    
    private static final int MIN_TEXT_LENGTH = 100;
    private static final int MAX_TEXT_LENGTH = 50000;
    private static final long TIMEOUT_SECONDS = 5;

    public AIContentDetectorService() {
        this.preprocessor = new TextPreprocessor();
        this.featureExtractor = new FeatureExtractor();
        this.modelPredictor = new ModelPredictor();
        
        // Thread pool for async processing
        this.executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
        );
        
        logger.info("AIContentDetectorService initialized successfully");
    }

    /**
     * Main detection method - synchronous
     */
    public AIDetectionResult detectAIContent(String text) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate input
            validateInput(text);
            
            // Preprocess text
            String cleanedText = preprocessor.normalizeText(text);
            logger.debug("Text preprocessed. Length: {} chars", cleanedText.length());
            
            // Extract features
            Map<String, Double> features = featureExtractor.extractAllFeatures(text);
            logger.debug("Extracted {} features", features.size());
            
            // Predict AI probability
            double aiProbability = modelPredictor.predictAIProbability(features);
            
            // Calculate confidence
            String confidence = modelPredictor.calculateConfidence(features, aiProbability);
            
            // Generate classification
            String classification = modelPredictor.classifyText(aiProbability);
            
            // Create result
            AIDetectionResult result = new AIDetectionResult(
                aiProbability,
                confidence,
                classification,
                features
            );
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Detection completed in {}ms. AI Probability: {:.2f}%, Confidence: {}",
                       duration, aiProbability, confidence);
            
            return result;
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error during AI detection", e);
            throw new RuntimeException("AI detection failed: " + e.getMessage(), e);
        }
    }

    /**
     * Async detection method
     */
    public CompletableFuture<AIDetectionResult> detectAIContentAsync(String text) {
        return CompletableFuture.supplyAsync(() -> detectAIContent(text), executorService)
                .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    logger.error("Async detection failed", throwable);
                    throw new RuntimeException("Async detection failed", throwable);
                });
    }

    /**
     * Batch detection for multiple texts
     */
    public Map<String, AIDetectionResult> detectBatch(Map<String, String> texts) {
        Map<String, CompletableFuture<AIDetectionResult>> futures = new ConcurrentHashMap<>();
        
        for (Map.Entry<String, String> entry : texts.entrySet()) {
            futures.put(entry.getKey(), detectAIContentAsync(entry.getValue()));
        }
        
        // Wait for all to complete
        Map<String, AIDetectionResult> results = new ConcurrentHashMap<>();
        for (Map.Entry<String, CompletableFuture<AIDetectionResult>> entry : futures.entrySet()) {
            try {
                results.put(entry.getKey(), entry.getValue().get());
            } catch (Exception e) {
                logger.error("Batch detection failed for key: {}", entry.getKey(), e);
            }
        }
        
        return results;
    }

    /**
     * Get detailed feature analysis
     */
    public Map<String, Double> analyzeFeatures(String text) {
        validateInput(text);
        return featureExtractor.extractAllFeatures(text);
    }

    /**
     * Quick validation check
     */
    public boolean isTextValid(String text) {
        try {
            validateInput(text);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Input validation
     */
    private void validateInput(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }
        
        String trimmed = text.trim();
        
        if (trimmed.length() < MIN_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Text too short. Minimum %d characters required, got %d", 
                            MIN_TEXT_LENGTH, trimmed.length())
            );
        }
        
        if (trimmed.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Text too long. Maximum %d characters allowed, got %d",
                            MAX_TEXT_LENGTH, trimmed.length())
            );
        }
        
        // Check if text has enough actual content
        if (!preprocessor.isValidText(trimmed, MIN_TEXT_LENGTH)) {
            throw new IllegalArgumentException("Text does not contain enough meaningful content");
        }
    }

    /**
     * Graceful shutdown
     */
    public void shutdown() {
        logger.info("Shutting down AIContentDetectorService");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Health check
     */
    public boolean isHealthy() {
        return !executorService.isShutdown() && 
               preprocessor != null && 
               featureExtractor != null && 
               modelPredictor != null;
    }

    /**
     * Get service statistics
     */
    public Map<String, Object> getStatistics() {
        return Map.of(
            "service", "AIContentDetectorService",
            "version", "1.0.0",
            "status", isHealthy() ? "healthy" : "unhealthy",
            "minTextLength", MIN_TEXT_LENGTH,
            "maxTextLength", MAX_TEXT_LENGTH,
            "timeoutSeconds", TIMEOUT_SECONDS
        );
    }
}
