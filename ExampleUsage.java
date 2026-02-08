import com.detector.service.AIContentDetectorService;
import com.detector.model.AIDetectionResult;
import java.util.Map;

/**
 * Example usage of AI Content Detector
 */
public class ExampleUsage {
    
    public static void main(String[] args) {
        // Initialize the detector service
        AIContentDetectorService detector = new AIContentDetectorService();
        
        // Example 1: Detect AI-generated text
        detectAIText(detector);
        
        // Example 2: Detect human-written text
        detectHumanText(detector);
        
        // Example 3: Batch processing
        batchDetection(detector);
        
        // Cleanup
        detector.shutdown();
    }
    
    /**
     * Example 1: Typical AI-generated text
     */
    public static void detectAIText(AIContentDetectorService detector) {
        System.out.println("\n=== Example 1: AI-Generated Text ===");
        
        String aiText = "In conclusion, the analysis demonstrates that artificial intelligence " +
                       "represents a transformative technology with significant implications for society. " +
                       "Furthermore, it is important to note that machine learning algorithms continue " +
                       "to evolve at a rapid pace. Moreover, the integration of AI systems into various " +
                       "industries has led to increased efficiency and productivity. Therefore, we can " +
                       "conclude that the future of AI holds tremendous potential for innovation and growth.";
        
        AIDetectionResult result = detector.detectAIContent(aiText);
        
        System.out.println("Text: " + aiText.substring(0, 100) + "...");
        System.out.println("\nResults:");
        System.out.println("  AI Probability: " + String.format("%.2f%%", result.getAiProbability()));
        System.out.println("  Confidence: " + result.getConfidence());
        System.out.println("  Classification: " + result.getClassification());
        
        System.out.println("\nKey Metrics:");
        Map<String, Double> features = result.getFeatures();
        System.out.println("  Burstiness: " + String.format("%.2f", features.get("burstiness")));
        System.out.println("  Uniformity: " + String.format("%.2f", features.get("uniformity")));
        System.out.println("  Diversity: " + String.format("%.2f", features.get("diversity")));
        System.out.println("  Keywords: " + String.format("%.2f", features.get("keywords")));
    }
    
    /**
     * Example 2: Typical human-written text
     */
    public static void detectHumanText(AIContentDetectorService detector) {
        System.out.println("\n=== Example 2: Human-Written Text ===");
        
        String humanText = "I love coffee! Why? Because mornings are impossible without it. " +
                          "My favorite is Ethiopian Yirgacheffe - the floral notes are incredible. " +
                          "Do you have a preference? I've tried so many different beans. " +
                          "Some days I go for a dark roast, other times a light one. " +
                          "Really depends on my mood, you know? The aroma alone is worth it!";
        
        AIDetectionResult result = detector.detectAIContent(humanText);
        
        System.out.println("Text: " + humanText.substring(0, 100) + "...");
        System.out.println("\nResults:");
        System.out.println("  AI Probability: " + String.format("%.2f%%", result.getAiProbability()));
        System.out.println("  Confidence: " + result.getConfidence());
        System.out.println("  Classification: " + result.getClassification());
        
        System.out.println("\nKey Metrics:");
        Map<String, Double> features = result.getFeatures();
        System.out.println("  Burstiness: " + String.format("%.2f", features.get("burstiness")));
        System.out.println("  Uniformity: " + String.format("%.2f", features.get("uniformity")));
        System.out.println("  Diversity: " + String.format("%.2f", features.get("diversity")));
        System.out.println("  Keywords: " + String.format("%.2f", features.get("keywords")));
    }
    
    /**
     * Example 3: Batch processing multiple texts
     */
    public static void batchDetection(AIContentDetectorService detector) {
        System.out.println("\n=== Example 3: Batch Processing ===");
        
        Map<String, String> texts = Map.of(
            "email1", "Please review the attached document and provide your feedback by end of day.",
            "essay1", "The implications of climate change extend far beyond environmental concerns. " +
                     "Indeed, the socioeconomic ramifications are profound and multifaceted.",
            "tweet1", "Just had the best burger ever! 🍔 Why don't more places do this?!"
        );
        
        Map<String, AIDetectionResult> results = detector.detectBatch(texts);
        
        System.out.println("Processed " + results.size() + " texts:\n");
        
        results.forEach((id, result) -> {
            System.out.println(id + ":");
            System.out.println("  AI Probability: " + String.format("%.2f%%", result.getAiProbability()));
            System.out.println("  Classification: " + result.getClassification());
        });
    }
}
