import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlagiarismChecker {

    public static List<String> tokenizeText(String text, int n) {
        // --- 1. Clean the text ---
        text = text.toLowerCase().replaceAll("[^a-z0-9.\\s]", " ").replaceAll("\\s+", " ").trim();

        // --- 2. Tokenize into words ---
        List<String> words = Arrays.asList(text.split(" "));

        // --- 3. Generate n-grams ---
        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i <= words.size() - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + n; j++) {
                sb.append(words.get(j));
                if (j < i + n - 1)
                    sb.append(" ");
            }
            ngrams.add(sb.toString());
        }
        return ngrams;
    }
    // 🔹 Function 2: Generate hash values for each token using polynomial rolling
    // hash
    publis

    static Set<Long>generateHashes(List<String> tokens){
        Set<Long> hashes = new HashSet<>();
        long base = 31;           // base for rolling hash
        long mod = 1000000009L;   // large prime for modulo
        for (String token : tokens) {
            long hash = 0;
            or (char c : token.toCharArray()) {
                hash = (hash * base + c) % mod;
            }
            hashes.add(hash);
        }
        return hashes;
    }

    // 🔹 Function 3: Compare two hash sets and find similarity percentage
    public static double calculateSimilarity(Set<Long> set1, Set<Long> set2) {
        if (set1.isEmpty() || set2.isEmpty())
            return 0.0;

        Set<Long> intersection = new HashSet<>(set1);
        intersection.retainAll(set2); // find common hashes

        int common = intersection.size();
        int total = set1.size() + set2.size() - common; // union size

        return (common * 100.0) / total; // percentage similarity
    }

    // 🔹 Function 4: Classify similarity level
    public static String classifySimilarity(double percentage) {
        if (percentage < 30) return "Low similarity";
        else if (percentage < 70) return "Medium similarity";
        else return "High similarity (Possible Plagiarism)";
    }

    public static void main(String[] args) {
        String text1 = "Artificial intelligence is changing the world rapidly.";
        String text2 = "AI is changing our world very quickly and effectively.";

        int n = 3; // you can change n-gram size

        // Step 1: Tokenize both texts
        List<String> ngrams1 = tokenizeText(text1, n);
        List<String> ngrams2 = tokenizeText(text2, n);

        // Step 2: Generate hashes
        Set<Long> hashes1 = generateHashes(ngrams1);
        Set<Long> hashes2 = generateHashes(ngrams2);

        // Step 3: Calculate similarity
        double similarity = calculateSimilarity(hashes1, hashes2);

        // Step 4: Classify and display
        System.out.println("Similarity: " + String.format("%.2f", similarity) + "%");
        System.out.println("Result: " + classifySimilarity(similarity));
    }
}