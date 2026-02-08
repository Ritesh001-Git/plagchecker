package com.detector.nlp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FeatureExtractor
 */
class FeatureExtractorTest {
    
    private FeatureExtractor featureExtractor;
    private TextPreprocessor preprocessor;

    @BeforeEach
    void setUp() {
        featureExtractor = new FeatureExtractor();
        preprocessor = new TextPreprocessor();
    }

    @Test
    void testLexicalDiversity_EmptyList() {
        List<String> tokens = Collections.emptyList();
        double diversity = featureExtractor.calculateLexicalDiversity(tokens);
        assertEquals(0.0, diversity, 0.001);
    }

    @Test
    void testLexicalDiversity_AllUnique() {
        List<String> tokens = Arrays.asList("hello", "world", "test", "example");
        double diversity = featureExtractor.calculateLexicalDiversity(tokens);
        assertEquals(1.0, diversity, 0.001);
    }

    @Test
    void testLexicalDiversity_SomeRepeats() {
        List<String> tokens = Arrays.asList("hello", "hello", "world", "world");
        double diversity = featureExtractor.calculateLexicalDiversity(tokens);
        assertEquals(0.5, diversity, 0.001);
    }

    @Test
    void testBurstiness_UniformSentences() {
        List<Integer> lengths = Arrays.asList(10, 10, 10, 10);
        double burstiness = featureExtractor.calculateBurstiness(lengths);
        assertEquals(0.0, burstiness, 0.001);
    }

    @Test
    void testBurstiness_VariedSentences() {
        List<Integer> lengths = Arrays.asList(5, 15, 8, 20);
        double burstiness = featureExtractor.calculateBurstiness(lengths);
        assertTrue(burstiness > 30.0, "Burstiness should be > 30 for varied sentences");
    }

    @Test
    void testUniformity_HighVariance() {
        List<Integer> lengths = Arrays.asList(5, 50, 10, 45);
        double uniformity = featureExtractor.calculateUniformity(lengths);
        assertTrue(uniformity < 20.0, "Uniformity should be low for high variance");
    }

    @Test
    void testUniformity_LowVariance() {
        List<Integer> lengths = Arrays.asList(10, 11, 10, 11);
        double uniformity = featureExtractor.calculateUniformity(lengths);
        assertTrue(uniformity > 60.0, "Uniformity should be high for low variance");
    }

    @Test
    void testRepetitionScore_NoRepetition() {
        List<String> tokens = Arrays.asList("unique", "words", "every", "time");
        double repetition = featureExtractor.calculateRepetitionScore(tokens, 2);
        assertEquals(0.0, repetition, 0.001);
    }

    @Test
    void testRepetitionScore_WithRepetition() {
        List<String> tokens = Arrays.asList("hello", "world", "hello", "world");
        double repetition = featureExtractor.calculateRepetitionScore(tokens, 2);
        assertTrue(repetition > 0.3, "Should detect repetition");
    }

    @Test
    void testEntropy_SingleWord() {
        List<String> tokens = Arrays.asList("test", "test", "test");
        double entropy = featureExtractor.calculateEntropy(tokens);
        assertEquals(0.0, entropy, 0.001);
    }

    @Test
    void testEntropy_UniformDistribution() {
        List<String> tokens = Arrays.asList("a", "b", "c", "d");
        double entropy = featureExtractor.calculateEntropy(tokens);
        assertEquals(2.0, entropy, 0.001);
    }

    @Test
    void testPerplexity() {
        List<String> tokens = Arrays.asList("a", "b", "c", "d");
        double perplexity = featureExtractor.calculatePerplexity(tokens);
        assertEquals(4.0, perplexity, 0.001);
    }

    @Test
    void testAvgWordLength() {
        List<String> tokens = Arrays.asList("hi", "test", "example");
        double avgLen = featureExtractor.calculateAvgWordLength(tokens);
        double expected = (2.0 + 4.0 + 7.0) / 3.0;
        assertEquals(expected, avgLen, 0.001);
    }

    @Test
    void testKeywordScore_NoKeywords() {
        String text = "This is a simple sentence without any formal markers.";
        double score = featureExtractor.calculateKeywordScore(text);
        assertEquals(0.0, score, 0.001);
    }

    @Test
    void testKeywordScore_WithKeywords() {
        String text = "In conclusion, this demonstrates that furthermore, it is important to note.";
        double score = featureExtractor.calculateKeywordScore(text);
        assertTrue(score > 0.4, "Should detect AI keywords");
    }

    @Test
    void testHapaxLegomena() {
        List<String> tokens = Arrays.asList("unique", "word", "test", "test");
        double hapax = featureExtractor.calculateHapaxLegomena(tokens);
        assertEquals(0.5, hapax, 0.001);
    }

    @Test
    void testExtractAllFeatures_ValidText() {
        String text = "This is a test. This is another test. Testing is important.";
        Map<String, Double> features = featureExtractor.extractAllFeatures(text);
        
        assertNotNull(features);
        assertTrue(features.containsKey("lexical_diversity"));
        assertTrue(features.containsKey("burstiness"));
        assertTrue(features.containsKey("entropy"));
        assertTrue(features.containsKey("perplexity"));
        assertTrue(features.size() > 10, "Should extract many features");
    }

    @Test
    void testExtractAllFeatures_AIText() {
        String aiText = "In conclusion, the analysis demonstrates several key points. " +
                       "Furthermore, it is important to note that the implications are significant. " +
                       "Moreover, the findings suggest that additional research is warranted. " +
                       "Therefore, we can conclude that the hypothesis is supported.";
        
        Map<String, Double> features = featureExtractor.extractAllFeatures(aiText);
        
        // AI text typically has:
        // - Lower burstiness
        // - Higher uniformity
        // - Moderate to high keyword score
        
        double burstiness = features.get("burstiness");
        double keywords = features.get("keywords");
        
        assertTrue(burstiness < 40.0, "AI text should have lower burstiness");
        assertTrue(keywords > 20.0, "AI text should have detectable keywords");
    }

    @Test
    void testExtractAllFeatures_HumanText() {
        String humanText = "I love coffee! Why? Because mornings are impossible without it. " +
                          "My favorite is Ethiopian Yirgacheffe. Do you have a preference? " +
                          "The floral notes are incredible - seriously, try it!";
        
        Map<String, Double> features = featureExtractor.extractAllFeatures(humanText);
        
        // Human text typically has:
        // - Higher burstiness
        // - Lower uniformity
        // - Lower keyword score
        
        double burstiness = features.get("burstiness");
        double keywords = features.get("keywords");
        
        assertTrue(burstiness > 30.0, "Human text should have higher burstiness");
        assertTrue(keywords < 10.0, "Human text should have fewer AI keywords");
    }
}
