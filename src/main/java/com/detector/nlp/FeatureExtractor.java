package com.detector.nlp;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.*;

/**
 * Advanced Feature Extraction Engine
 * Extracts stylometric and statistical features for AI detection
 */
public class FeatureExtractor {
    
    private final TextPreprocessor preprocessor;

    public FeatureExtractor() {
        this.preprocessor = new TextPreprocessor();
    }

    /**
     * Extract all features from text
     */
    public Map<String, Double> extractAllFeatures(String text) {
        Map<String, Double> features = new LinkedHashMap<>();
        
        List<String> tokens = preprocessor.tokenize(text);
        List<String> tokensNoStop = preprocessor.tokenizeWithoutStopwords(text);
        List<Integer> sentenceLengths = preprocessor.getSentenceLengths(text);
        
        // Core linguistic features
        features.put("lexical_diversity", calculateLexicalDiversity(tokens));
        features.put("ttr", calculateTypeTokenRatio(tokens));
        features.put("diversity", calculateLexicalDiversity(tokens) * 100);
        
        // Sentence structure features
        features.put("burstiness", calculateBurstiness(sentenceLengths));
        features.put("uniformity", calculateUniformity(sentenceLengths));
        features.put("sentence_variance", calculateSentenceVariance(sentenceLengths));
        
        // Repetition patterns
        features.put("repetition", calculateRepetitionScore(tokens, 3) * 100);
        features.put("ngram_repetition_2", calculateRepetitionScore(tokens, 2));
        features.put("ngram_repetition_3", calculateRepetitionScore(tokens, 3));
        
        // Information theory features
        features.put("entropy", calculateEntropy(tokens));
        features.put("perplexity", calculatePerplexity(tokens));
        
        // Vocabulary features
        features.put("avg_word_length", calculateAvgWordLength(tokens));
        features.put("simplicity", calculateSimplicity(tokens));
        
        // AI-specific markers
        features.put("keywords", calculateKeywordScore(text) * 100);
        features.put("formal_markers", calculateFormalMarkers(text));
        features.put("transition_density", calculateTransitionDensity(text));
        
        // Advanced metrics
        features.put("hapax_legomena", calculateHapaxLegomena(tokens));
        features.put("yules_k", calculateYulesK(tokens));
        features.put("gunning_fog", calculateGunningFog(text, sentenceLengths, tokens));
        
        return features;
    }

    /**
     * Lexical Diversity: Unique words / Total words
     * Lower diversity often indicates AI-generated text
     */
    public double calculateLexicalDiversity(List<String> tokens) {
        if (tokens.isEmpty()) return 0.0;
        Set<String> uniqueWords = new HashSet<>(tokens);
        return (double) uniqueWords.size() / tokens.size();
    }

    /**
     * Type-Token Ratio (TTR)
     * Classic measure of vocabulary richness
     */
    public double calculateTypeTokenRatio(List<String> tokens) {
        if (tokens.isEmpty()) return 0.0;
        
        // Use moving window TTR for longer texts (more stable)
        if (tokens.size() > 1000) {
            return calculateMovingWindowTTR(tokens, 1000);
        }
        
        return calculateLexicalDiversity(tokens);
    }

    private double calculateMovingWindowTTR(List<String> tokens, int windowSize) {
        List<Double> ttrs = new ArrayList<>();
        for (int i = 0; i <= tokens.size() - windowSize; i += windowSize / 2) {
            List<String> window = tokens.subList(i, Math.min(i + windowSize, tokens.size()));
            Set<String> unique = new HashSet<>(window);
            ttrs.add((double) unique.size() / window.size());
        }
        return ttrs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * Burstiness: Measures variation in sentence lengths
     * AI text tends to have lower burstiness (more uniform)
     */
    public double calculateBurstiness(List<Integer> sentenceLengths) {
        if (sentenceLengths.size() < 2) return 0.0;
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        sentenceLengths.forEach(stats::addValue);
        
        double mean = stats.getMean();
        double stdDev = stats.getStandardDeviation();
        
        if (mean < 0.001) return 0.0;
        
        return (stdDev / mean) * 100; // Return as percentage
    }

    /**
     * Uniformity: Inverse of sentence length variance
     * Higher uniformity suggests AI generation
     */
    public double calculateUniformity(List<Integer> sentenceLengths) {
        if (sentenceLengths.size() < 2) return 0.0;
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        sentenceLengths.forEach(stats::addValue);
        
        double stdDev = stats.getStandardDeviation();
        return (1.0 / (1.0 + stdDev)) * 100; // Return as percentage
    }

    /**
     * Sentence Variance
     */
    public double calculateSentenceVariance(List<Integer> sentenceLengths) {
        if (sentenceLengths.size() < 2) return 0.0;
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        sentenceLengths.forEach(stats::addValue);
        
        return stats.getVariance();
    }

    /**
     * N-gram Repetition Score
     * Measures how often n-grams repeat
     */
    public double calculateRepetitionScore(List<String> tokens, int n) {
        if (tokens.size() < n) return 0.0;
        
        List<String> ngrams = preprocessor.generateNGrams(tokens, n);
        Map<String, Integer> ngramCounts = new HashMap<>();
        
        for (String ngram : ngrams) {
            ngramCounts.put(ngram, ngramCounts.getOrDefault(ngram, 0) + 1);
        }
        
        long repeatedNgrams = ngramCounts.values().stream()
                .filter(count -> count > 1)
                .count();
        
        return ngramCounts.isEmpty() ? 0.0 : (double) repeatedNgrams / ngramCounts.size();
    }

    /**
     * Shannon Entropy: Measures unpredictability of word distribution
     * Lower entropy can indicate AI-generated text
     */
    public double calculateEntropy(List<String> tokens) {
        if (tokens.isEmpty()) return 0.0;
        
        Map<String, Integer> frequency = preprocessor.getWordFrequency(tokens);
        double totalWords = tokens.size();
        double entropy = 0.0;
        
        for (int freq : frequency.values()) {
            double probability = freq / totalWords;
            if (probability > 0) {
                entropy += -probability * (Math.log(probability) / Math.log(2));
            }
        }
        
        return entropy;
    }

    /**
     * Perplexity: Exponential of entropy
     * Measures predictability of the text
     */
    public double calculatePerplexity(List<String> tokens) {
        double entropy = calculateEntropy(tokens);
        return Math.pow(2, entropy);
    }

    /**
     * Average Word Length
     */
    public double calculateAvgWordLength(List<String> tokens) {
        if (tokens.isEmpty()) return 0.0;
        return tokens.stream()
                .mapToInt(String::length)
                .average()
                .orElse(0.0);
    }

    /**
     * Simplicity Score: AI often uses simpler, shorter words
     */
    public double calculateSimplicity(List<String> tokens) {
        double avgLen = calculateAvgWordLength(tokens);
        // Map 3-8 letter average to 0-1 scale (shorter = more AI-like)
        return Math.max(0, Math.min(1, (7.0 - avgLen) / 3.0));
    }

    /**
     * AI Keyword/Phrase Detection
     * Common phrases used by AI models (especially Claude-style)
     */
    public double calculateKeywordScore(String text) {
        String lowerText = text.toLowerCase();
        
        String[] aiMarkers = {
            "in conclusion", "overall", "in summary", "therefore",
            "it is important to note", "furthermore", "moreover",
            "in other words", "to summarize", "as a result",
            "it's worth noting", "importantly", "notably",
            "in essence", "essentially", "fundamentally",
            "it should be noted", "one might argue", "arguably",
            "from this perspective", "in this context"
        };
        
        int count = 0;
        for (String marker : aiMarkers) {
            if (lowerText.contains(marker)) {
                count++;
            }
        }
        
        return Math.min(1.0, count / 5.0);
    }

    /**
     * Formal Language Markers
     */
    public double calculateFormalMarkers(String text) {
        String lowerText = text.toLowerCase();
        
        String[] formalWords = {
            "however", "nonetheless", "nevertheless", "consequently",
            "accordingly", "hence", "thus", "thereby", "wherein"
        };
        
        int count = 0;
        for (String word : formalWords) {
            count += countOccurrences(lowerText, word);
        }
        
        return Math.min(1.0, count / 10.0);
    }

    /**
     * Transition Word Density
     */
    public double calculateTransitionDensity(String text) {
        List<String> tokens = preprocessor.tokenize(text);
        if (tokens.isEmpty()) return 0.0;
        
        Set<String> transitions = new HashSet<>(Arrays.asList(
            "however", "moreover", "furthermore", "additionally",
            "consequently", "therefore", "thus", "hence",
            "meanwhile", "subsequently", "accordingly"
        ));
        
        long transitionCount = tokens.stream()
                .filter(transitions::contains)
                .count();
        
        return (double) transitionCount / tokens.size();
    }

    /**
     * Hapax Legomena: Words that appear only once
     * Higher ratio suggests human writing
     */
    public double calculateHapaxLegomena(List<String> tokens) {
        if (tokens.isEmpty()) return 0.0;
        
        Map<String, Integer> frequency = preprocessor.getWordFrequency(tokens);
        long hapaxCount = frequency.values().stream()
                .filter(count -> count == 1)
                .count();
        
        return (double) hapaxCount / tokens.size();
    }

    /**
     * Yule's K: Measures vocabulary richness
     * Accounts for text length effects
     */
    public double calculateYulesK(List<String> tokens) {
        if (tokens.isEmpty()) return 0.0;
        
        Map<String, Integer> frequency = preprocessor.getWordFrequency(tokens);
        Map<Integer, Integer> spectrumFrequency = new HashMap<>();
        
        for (int freq : frequency.values()) {
            spectrumFrequency.put(freq, spectrumFrequency.getOrDefault(freq, 0) + 1);
        }
        
        double N = tokens.size();
        double sumVrR2 = 0.0;
        
        for (Map.Entry<Integer, Integer> entry : spectrumFrequency.entrySet()) {
            int r = entry.getKey();
            int vr = entry.getValue();
            sumVrR2 += vr * r * r;
        }
        
        double M1 = frequency.size();
        double M2 = sumVrR2;
        
        return 10000 * (M2 - M1) / (N * N);
    }

    /**
     * Gunning Fog Index: Readability measure
     */
    public double calculateGunningFog(String text, List<Integer> sentenceLengths, List<String> tokens) {
        if (sentenceLengths.isEmpty() || tokens.isEmpty()) return 0.0;
        
        double avgSentenceLength = sentenceLengths.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        
        long complexWords = tokens.stream()
                .filter(word -> word.length() > 6)
                .filter(word -> !word.endsWith("ing") && !word.endsWith("ed"))
                .count();
        
        double percentComplexWords = (double) complexWords / tokens.size() * 100;
        
        return 0.4 * (avgSentenceLength + percentComplexWords);
    }

    private int countOccurrences(String text, String word) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(word, index)) != -1) {
            count++;
            index += word.length();
        }
        return count;
    }
}
