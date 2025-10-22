import java.util.*;
public class PlagiarismCheckerAi {
    // 1️⃣ Tokenize paragraph into words
    public static List<String> getWords(String text) {
        text = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        return Arrays.asList(text.split(" "));
    }

    // 2️⃣ Split into sentences and return their lengths
    public static List<Integer> getSentenceLengths(String text) {
        String[] sentences = text.split("[.!?]");
        List<Integer> lengths = new ArrayList<>();
        for (String s : sentences) {
            String[] words = s.trim().split("\\s+");
            if (words.length > 1) lengths.add(words.length);
        }
        return lengths;
    }

    // 3️⃣ Calculate lexical diversity (uniqueWords / totalWords)
    public static double lexicalDiversity(List<String> words) {
        Set<String> unique = new HashSet<>(words);
        return (double) unique.size() / words.size();
    }

    // 4️⃣ Count repeated n-grams (Repetition Score)
    public static double repetitionScore(List<String> words, int n) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i <= words.size() - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + n; j++) {
                sb.append(words.get(j)).append(" ");
            }
            String gram = sb.toString().trim();
            map.put(gram, map.getOrDefault(gram, 0) + 1);
        }
        int repeated = 0;
        for (int count : map.values()) {
            if (count > 1) repeated++;
        }

        return (double) repeated / map.size();
    }

     // 5️⃣ Sentence uniformity (standard deviation of lengths)
     public static double sentenceUniformity(List<Integer> lengths) {
        if (lengths.size() < 2) return 0;
        double mean = lengths.stream().mapToInt(i -> i).average().orElse(0);
        double variance = 0;
        for (int len : lengths) variance += Math.pow(len - mean, 2);
        variance /= lengths.size();
        double stdDev = Math.sqrt(variance);
        // More uniform (smaller stdDev) means more AI-like
        return 1.0 / (1.0 + stdDev); // normalized
    }

    // 6️⃣ Count AI keywords
    public static double keywordScore(String text) {
        String[] aiWords = {"in conclusion", "overall", "in summary", "therefore", "it is important to note"};
        text = text.toLowerCase();
        int matches = 0;
}
