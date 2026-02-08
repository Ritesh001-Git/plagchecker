package com.detector.nlp;

import org.apache.commons.lang3.StringUtils;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Text Preprocessing Engine
 * Handles tokenization, normalization, stopword removal, and sentence segmentation
 */
public class TextPreprocessor {
    
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?]+");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    
    // Common English stopwords
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
        "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
        "to", "was", "were", "will", "with", "this", "but", "they", "have",
        "had", "what", "when", "where", "who", "which", "why", "how"
    ));

    /**
     * Normalize text: lowercase, remove special characters, normalize whitespace
     */
    public String normalizeText(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        
        // Convert to lowercase
        text = text.toLowerCase();
        
        // Remove URLs
        text = text.replaceAll("https?://\\S+", " ");
        
        // Remove email addresses
        text = text.replaceAll("\\S+@\\S+", " ");
        
        // Keep only letters, numbers, and basic punctuation
        text = text.replaceAll("[^a-z0-9\\s.!?,;:'-]", " ");
        
        // Normalize whitespace
        text = WHITESPACE_PATTERN.matcher(text).replaceAll(" ");
        
        return text.trim();
    }

    /**
     * Tokenize text into words
     */
    public List<String> tokenize(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        
        String normalized = normalizeText(text);
        var matcher = WORD_PATTERN.matcher(normalized);
        List<String> tokens = new ArrayList<>();
        
        while (matcher.find()) {
            String token = matcher.group().toLowerCase();
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        
        return tokens;
    }

    /**
     * Tokenize and remove stopwords
     */
    public List<String> tokenizeWithoutStopwords(String text) {
        List<String> tokens = tokenize(text);
        return tokens.stream()
                .filter(token -> !STOPWORDS.contains(token))
                .filter(token -> token.length() > 2) // Remove very short words
                .collect(Collectors.toList());
    }

    /**
     * Segment text into sentences
     */
    public List<String> segmentSentences(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        
        String[] sentences = SENTENCE_PATTERN.split(text);
        return Arrays.stream(sentences)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(s -> s.split("\\s+").length > 3) // At least 3 words
                .collect(Collectors.toList());
    }

    /**
     * Get sentence lengths (in words)
     */
    public List<Integer> getSentenceLengths(String text) {
        List<String> sentences = segmentSentences(text);
        return sentences.stream()
                .map(this::tokenize)
                .map(List::size)
                .filter(len -> len > 0)
                .collect(Collectors.toList());
    }

    /**
     * Generate n-grams from tokens
     */
    public List<String> generateNGrams(List<String> tokens, int n) {
        if (tokens.size() < n) {
            return Collections.emptyList();
        }
        
        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i <= tokens.size() - n; i++) {
            String ngram = String.join(" ", tokens.subList(i, i + n));
            ngrams.add(ngram);
        }
        
        return ngrams;
    }

    /**
     * Calculate word frequency distribution
     */
    public Map<String, Integer> getWordFrequency(List<String> tokens) {
        Map<String, Integer> frequency = new HashMap<>();
        for (String token : tokens) {
            frequency.put(token, frequency.getOrDefault(token, 0) + 1);
        }
        return frequency;
    }

    /**
     * Validate text meets minimum requirements
     */
    public boolean isValidText(String text, int minChars) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        
        String cleaned = text.trim();
        if (cleaned.length() < minChars) {
            return false;
        }
        
        // Check if it has enough actual words
        List<String> tokens = tokenize(cleaned);
        return tokens.size() >= 20; // At least 20 words
    }

    /**
     * Get character count (excluding whitespace)
     */
    public int getCharacterCount(String text) {
        if (StringUtils.isBlank(text)) {
            return 0;
        }
        return text.replaceAll("\\s", "").length();
    }

    /**
     * Get word count
     */
    public int getWordCount(String text) {
        return tokenize(text).size();
    }
}
