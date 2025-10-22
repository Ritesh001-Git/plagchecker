import java.util.*;

public class PlagiarismChecker {

    // Step 1: Tokenize text
    public static List<String> tokenizeText(String text, int n) {
        text = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        List<String> words = Arrays.asList(text.split(" "));
        List<String> ngrams = new ArrayList<>();

        for (int i = 0; i <= words.size() - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + n; j++) {
                sb.append(words.get(j));
                if (j < i + n - 1) sb.append(" ");
            }
            ngrams.add(sb.toString());
        }
        return ngrams;
    }

    // Step 2: Generate hashes
    public static Set<Long> generateHashes(List<String> tokens) {
        Set<Long> hashes = new HashSet<>();
        long base = 31, mod = 1000000009L;
        for (String token : tokens) {
            long hash = 0;
            for (char c : token.toCharArray()) {
                hash = (hash * base + c) % mod;
            }
            hashes.add(hash);
        }
        return hashes;
    }

    // Step 3: Compare two texts
    public static double compareTexts(String text1, String text2) {
        int n = 3;
        List<String> ngrams1 = tokenizeText(text1, n);
        List<String> ngrams2 = tokenizeText(text2, n);

        Set<Long> hashes1 = generateHashes(ngrams1);
        Set<Long> hashes2 = generateHashes(ngrams2);

        if (hashes1.isEmpty() || hashes2.isEmpty()) return 0.0;

        Set<Long> intersection = new HashSet<>(hashes1);
        intersection.retainAll(hashes2);

        int common = intersection.size();
        int total = hashes1.size() + hashes2.size() - common;

        return (common * 100.0) / total;
    }
}
