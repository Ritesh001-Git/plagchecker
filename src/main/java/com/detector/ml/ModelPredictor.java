package com.detector.ml;

import java.util.*;

/**
 * Machine Learning Predictor for AI Content Detection
 * Uses ensemble approach combining multiple detection signals
 */
public class ModelPredictor {
    
    // Calibrated weights based on empirical testing
    private static final double WEIGHT_BURSTINESS = 0.30;
    private static final double WEIGHT_UNIFORMITY = 0.20;
    private static final double WEIGHT_PERPLEXITY = 0.15;
    private static final double WEIGHT_DIVERSITY = 0.12;
    private static final double WEIGHT_REPETITION = 0.10;
    private static final double WEIGHT_KEYWORDS = 0.08;
    private static final double WEIGHT_ENTROPY = 0.05;

    // Thresholds calibrated for Claude-style AI detection
    private static final double BURSTINESS_THRESHOLD_LOW = 25.0;
    private static final double BURSTINESS_THRESHOLD_HIGH = 50.0;
    private static final double UNIFORMITY_THRESHOLD = 50.0;
    private static final double DIVERSITY_THRESHOLD = 0.65;
    private static final double PERPLEXITY_THRESHOLD = 50.0;

    /**
     * Predict AI probability using ensemble approach
     */
    public double predictAIProbability(Map<String, Double> features) {
        // Extract key features
        double burstiness = features.getOrDefault("burstiness", 0.0);
        double uniformity = features.getOrDefault("uniformity", 0.0);
        double diversity = features.getOrDefault("lexical_diversity", 0.0);
        double repetition = features.getOrDefault("repetition", 0.0) / 100.0;
        double perplexity = features.getOrDefault("perplexity", 0.0);
        double keywords = features.getOrDefault("keywords", 0.0) / 100.0;
        double entropy = features.getOrDefault("entropy", 0.0);
        
        // Normalize entropy (typical range 3-8)
        double normalizedEntropy = Math.min(1.0, entropy / 8.0);
        
        // Calculate individual signal scores (0-1 scale)
        double burstiScore = calculateBurstinessScore(burstiness);
        double uniformityScore = calculateUniformityScore(uniformity);
        double diversityScore = calculateDiversityScore(diversity);
        double repetitionScore = Math.min(1.0, repetition);
        double perplexityScore = calculatePerplexityScore(perplexity);
        double keywordScore = Math.min(1.0, keywords);
        double entropyScore = 1.0 - normalizedEntropy; // Lower entropy = more AI-like
        
        // Weighted ensemble
        double rawScore = 
            burstiScore * WEIGHT_BURSTINESS +
            uniformityScore * WEIGHT_UNIFORMITY +
            perplexityScore * WEIGHT_PERPLEXITY +
            diversityScore * WEIGHT_DIVERSITY +
            repetitionScore * WEIGHT_REPETITION +
            keywordScore * WEIGHT_KEYWORDS +
            entropyScore * WEIGHT_ENTROPY;
        
        // Apply non-linear transformation for better discrimination
        double transformedScore = applyNonLinearTransform(rawScore);
        
        // Apply special rules for high-confidence AI detection
        transformedScore = applyHighConfidenceRules(transformedScore, burstiness, uniformity, diversity);
        
        // Clamp and convert to percentage
        double finalScore = Math.max(0.0, Math.min(1.0, transformedScore));
        return finalScore * 100.0;
    }

    /**
     * Burstiness Score Calculation
     * Low burstiness (<25) strongly indicates AI
     */
    private double calculateBurstinessScore(double burstiness) {
        if (burstiness < BURSTINESS_THRESHOLD_LOW) {
            return 1.0; // Very likely AI
        } else if (burstiness < 35.0) {
            return 0.85;
        } else if (burstiness < BURSTINESS_THRESHOLD_HIGH) {
            return 0.6;
        } else if (burstiness < 70.0) {
            return 0.4;
        } else {
            return 0.2; // Likely human
        }
    }

    /**
     * Uniformity Score Calculation
     * High uniformity (>50) indicates AI
     */
    private double calculateUniformityScore(double uniformity) {
        if (uniformity > 70.0) {
            return 1.0;
        } else if (uniformity > UNIFORMITY_THRESHOLD) {
            return 0.7 + (uniformity - 50.0) / 100.0;
        } else if (uniformity > 30.0) {
            return 0.5;
        } else {
            return 0.2;
        }
    }

    /**
     * Diversity Score Calculation
     * Modern AI can have high diversity, but in specific patterns
     */
    private double calculateDiversityScore(double diversity) {
        // Very high diversity (>0.75) with low burstiness = AI trying to seem diverse
        if (diversity > 0.75) {
            return 0.6; // Moderate AI signal
        } else if (diversity > DIVERSITY_THRESHOLD) {
            return 0.5;
        } else if (diversity > 0.50) {
            return 0.6;
        } else if (diversity > 0.35) {
            return 0.7;
        } else {
            return 0.85; // Low diversity = AI
        }
    }

    /**
     * Perplexity Score Calculation
     * Lower perplexity indicates more predictable (AI-like) text
     */
    private double calculatePerplexityScore(double perplexity) {
        if (perplexity < 30.0) {
            return 0.9;
        } else if (perplexity < PERPLEXITY_THRESHOLD) {
            return 0.7;
        } else if (perplexity < 80.0) {
            return 0.5;
        } else if (perplexity < 120.0) {
            return 0.3;
        } else {
            return 0.1;
        }
    }

    /**
     * Apply non-linear transformation to improve score distribution
     */
    private double applyNonLinearTransform(double rawScore) {
        // Sigmoid-like transformation to spread scores in middle range
        return 1.0 / (1.0 + Math.exp(-8.0 * (rawScore - 0.5)));
    }

    /**
     * Apply high-confidence rules for definitive AI detection
     */
    private double applyHighConfidenceRules(double score, double burstiness, 
                                           double uniformity, double diversity) {
        // Rule 1: Very low burstiness + high uniformity = definitely AI
        if (burstiness < 20.0 && uniformity > 60.0) {
            return Math.max(score, 0.92);
        }
        
        // Rule 2: Extremely low burstiness alone
        if (burstiness < 15.0) {
            return Math.max(score, 0.88);
        }
        
        // Rule 3: Perfect storm - low burstiness, high uniformity, moderate diversity
        if (burstiness < 25.0 && uniformity > 55.0 && diversity > 0.5 && diversity < 0.75) {
            return Math.max(score, 0.90);
        }
        
        // Rule 4: Very high burstiness = likely human
        if (burstiness > 80.0) {
            return Math.min(score, 0.25);
        }
        
        return score;
    }

    /**
     * Calculate confidence level based on feature consistency
     */
    public String calculateConfidence(Map<String, Double> features, double aiProbability) {
        double burstiness = features.getOrDefault("burstiness", 0.0);
        double uniformity = features.getOrDefault("uniformity", 0.0);
        double diversity = features.getOrDefault("lexical_diversity", 0.0);
        
        // Check for strong signals
        boolean strongAISignals = (burstiness < 20.0 && uniformity > 60.0) ||
                                 (burstiness < 15.0) ||
                                 (aiProbability > 85.0);
        
        boolean strongHumanSignals = (burstiness > 70.0) ||
                                    (aiProbability < 20.0);
        
        // Check for conflicting signals
        boolean conflictingSignals = (burstiness < 30.0 && diversity > 0.75) ||
                                    (burstiness > 60.0 && uniformity > 60.0);
        
        if (strongAISignals || strongHumanSignals) {
            return "High";
        } else if (conflictingSignals) {
            return "Low";
        } else if (aiProbability > 65.0 || aiProbability < 35.0) {
            return "Medium-High";
        } else {
            return "Medium";
        }
    }

    /**
     * Generate human-readable classification
     */
    public String classifyText(double aiProbability) {
        if (aiProbability < 25.0) {
            return "Likely Human-written";
        } else if (aiProbability < 50.0) {
            return "Possibly Human with AI assistance";
        } else if (aiProbability < 75.0) {
            return "Possibly AI-generated or Mixed";
        } else if (aiProbability < 90.0) {
            return "Likely AI-generated";
        } else {
            return "Highly likely AI-generated (Claude-style)";
        }
    }

    /**
     * Get detailed analysis explanation
     */
    public String getAnalysisExplanation(Map<String, Double> features, double aiProbability) {
        StringBuilder explanation = new StringBuilder();
        
        double burstiness = features.getOrDefault("burstiness", 0.0);
        double uniformity = features.getOrDefault("uniformity", 0.0);
        double diversity = features.getOrDefault("lexical_diversity", 0.0);
        
        explanation.append("Analysis: ");
        
        if (burstiness < 25.0) {
            explanation.append("Very consistent sentence structure (AI indicator). ");
        } else if (burstiness > 70.0) {
            explanation.append("Highly variable sentence structure (human indicator). ");
        }
        
        if (uniformity > 60.0) {
            explanation.append("Uniform sentence patterns (AI indicator). ");
        }
        
        if (diversity < 0.4) {
            explanation.append("Limited vocabulary variation (AI indicator). ");
        } else if (diversity > 0.75) {
            explanation.append("High vocabulary diversity. ");
        }
        
        return explanation.toString();
    }
}
