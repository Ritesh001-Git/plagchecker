package com.detector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Data Transfer Object for AI Content Detection Results
 */
public class AIDetectionResult {
    
    @JsonProperty("ai_probability")
    private double aiProbability;
    
    @JsonProperty("confidence")
    private String confidence;
    
    @JsonProperty("classification")
    private String classification;
    
    @JsonProperty("features")
    private Map<String, Double> features;
    
    @JsonProperty("ai_percent")
    private double aiPercent;
    
    @JsonProperty("diversity")
    private double diversity;
    
    @JsonProperty("repetition")
    private double repetition;
    
    @JsonProperty("uniformity")
    private double uniformity;
    
    @JsonProperty("burstiness")
    private double burstiness;
    
    @JsonProperty("keywords")
    private double keywords;
    
    @JsonProperty("perplexity")
    private double perplexity;
    
    @JsonProperty("entropy")
    private double entropy;

    public AIDetectionResult() {}

    public AIDetectionResult(double aiProbability, String confidence, 
                           String classification, Map<String, Double> features) {
        this.aiProbability = aiProbability;
        this.aiPercent = aiProbability;
        this.confidence = confidence;
        this.classification = classification;
        this.features = features;
        
        // Extract individual features
        if (features != null) {
            this.diversity = features.getOrDefault("diversity", 0.0);
            this.repetition = features.getOrDefault("repetition", 0.0);
            this.uniformity = features.getOrDefault("uniformity", 0.0);
            this.burstiness = features.getOrDefault("burstiness", 0.0);
            this.keywords = features.getOrDefault("keywords", 0.0);
            this.perplexity = features.getOrDefault("perplexity", 0.0);
            this.entropy = features.getOrDefault("entropy", 0.0);
        }
    }

    // Getters and Setters
    public double getAiProbability() { return aiProbability; }
    public void setAiProbability(double aiProbability) { 
        this.aiProbability = aiProbability;
        this.aiPercent = aiProbability;
    }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public Map<String, Double> getFeatures() { return features; }
    public void setFeatures(Map<String, Double> features) { this.features = features; }

    public double getAiPercent() { return aiPercent; }
    public void setAiPercent(double aiPercent) { this.aiPercent = aiPercent; }

    public double getDiversity() { return diversity; }
    public void setDiversity(double diversity) { this.diversity = diversity; }

    public double getRepetition() { return repetition; }
    public void setRepetition(double repetition) { this.repetition = repetition; }

    public double getUniformity() { return uniformity; }
    public void setUniformity(double uniformity) { this.uniformity = uniformity; }

    public double getBurstiness() { return burstiness; }
    public void setBurstiness(double burstiness) { this.burstiness = burstiness; }

    public double getKeywords() { return keywords; }
    public void setKeywords(double keywords) { this.keywords = keywords; }

    public double getPerplexity() { return perplexity; }
    public void setPerplexity(double perplexity) { this.perplexity = perplexity; }

    public double getEntropy() { return entropy; }
    public void setEntropy(double entropy) { this.entropy = entropy; }

    @Override
    public String toString() {
        return String.format("AIDetectionResult{aiProbability=%.2f%%, confidence='%s', classification='%s'}", 
                           aiProbability, confidence, classification);
    }
}
