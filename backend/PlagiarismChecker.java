import java.util.*;

public class PlagiarismChecker {

    // ✅ Step 1: Tokenize text into n-grams
    public static List<String> tokenizeText(String text, int n) {
        // Normalize text (lowercase + remove punctuation)
        text = text.toLowerCase()
                   .replaceAll("[^a-z0-9]+", " ")
                   .trim();

        List<String> words = Arrays.asList(text.split("\\s+"));
        List<String> ngrams = new ArrayList<>();

        if (words.size() < n) return ngrams;

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

    // ✅ Step 2: Generate unique hashes for n-grams
    public static Set<Long> generateHashes(List<String> tokens) {
        Set<Long> hashes = new HashSet<>();
        long base = 31, mod = 1_000_000_009L;
        for (String token : tokens) {
            long hash = 0;
            for (char c : token.toCharArray()) {
                hash = (hash * base + c) % mod;
            }
            hashes.add(hash);
        }
        return hashes;
    }

     // ✅ Step 3: Compare using n-gram Jaccard similarity (1–3 grams)
     public static double ngramSimilarity(String text1, String text2, int n) {
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

    // ✅ Step 4: Cosine Similarity based on term frequency
    public static double cosineSimilarity(String text1, String text2) {
        Map<String, Integer> freq1 = getFrequencyMap(text1);
        Map<String, Integer> freq2 = getFrequencyMap(text2);

        Set<String> allWords = new HashSet<>(freq1.keySet());
        allWords.addAll(freq2.keySet());

        double dot = 0, mag1 = 0, mag2 = 0;
        for (String w : allWords) {
            int x = freq1.getOrDefault(w, 0);
            int y = freq2.getOrDefault(w, 0);
            dot += x * y;
            mag1 += x * x;
            mag2 += y * y;
        }

        if (mag1 == 0 || mag2 == 0) return 0.0;
        return (dot / (Math.sqrt(mag1) * Math.sqrt(mag2))) * 100.0;
    }

    private static Map<String, Integer> getFrequencyMap(String text) {
        text = text.toLowerCase().replaceAll("[^a-z0-9]+", " ");
        Map<String, Integer> freq = new HashMap<>();
        for (String w : text.split("\\s+")) {
            if (!w.isEmpty()) freq.put(w, freq.getOrDefault(w, 0) + 1);
        }
        return freq;
    }

    // ✅ Step 5: LCS (Longest Common Subsequence) similarity
    public static double lcsSimilarity(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        int m = a.length(), n = b.length();
        if (m == 0 || n == 0) return 0.0;

        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1))
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                else
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }

        double lcsLen = dp[m][n];
        return (lcsLen * 200.0) / (m + n); // normalized 0–100
    }

    // ✅ Step 6: Combined similarity result
    public static Map<String, Double> getDetailedSimilarity(String text1, String text2) {
        double jaccard = ngramSimilarity(text1, text2, 1);
        double bigram = ngramSimilarity(text1, text2, 2);
        double trigram = ngramSimilarity(text1, text2, 3);
        double cosine = cosineSimilarity(text1, text2);
        double lcs = lcsSimilarity(text1, text2);

        double overall = (jaccard + bigram + trigram + cosine + lcs) / 5.0;

        Map<String, Double> result = new LinkedHashMap<>();
        result.put("overall", overall);
        result.put("jaccard", jaccard);
        result.put("cosine", cosine);
        result.put("lcs", lcs);
        result.put("ngram", (bigram + trigram) / 2.0);

        return result;
    }

    // ✅ Step 7: Simple compareTexts() for MiniServer
    public static double compareTexts(String text1, String text2) {
        return getDetailedSimilarity(text1, text2).get("overall");
    }
}
