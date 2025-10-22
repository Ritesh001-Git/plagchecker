
import java.util.*;

public class PlagiarismCheckerAi {

    public static List<String> getWords(String text) {
        text = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        return Arrays.asList(text.split(" "));
    }

    public static List<Integer> getSentenceLengths(String text) {
        String[] sentences = text.split("[.!?]");
        List<Integer> lengths = new ArrayList<>();
        for (String s : sentences) {
            String[] words = s.trim().split("\\s+");
            if (words.length > 1) lengths.add(words.length);
        }
        return lengths;
    }

    public static double lexicalDiversity(List<String> words) {
        Set<String> unique = new HashSet<>(words);
        return (double) unique.size() / words.size();
    }

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
        for (int count : map.values()) if (count > 1) repeated++;
        return (double) repeated / map.size();
    }

    public static double sentenceUniformity(List<Integer> lengths) {
        if (lengths.size() < 2) return 0;
        double mean = lengths.stream().mapToInt(i -> i).average().orElse(0);
        double variance = 0;
        for (int len : lengths) variance += Math.pow(len - mean, 2);
        variance /= lengths.size();
        double stdDev = Math.sqrt(variance);
        return 1.0 / (1.0 + stdDev);
    }

    public static double keywordScore(String text) {
        String[] aiWords = {"in conclusion", "overall", "in summary", "therefore", "it is important to note"};
        text = text.toLowerCase();
        int matches = 0;
        for (String kw : aiWords) if (text.contains(kw)) matches++;
        return Math.min(1.0, matches / 5.0);
    }

    public static double calculateAIScore(String text) {
        List<String> words = getWords(text);
        List<Integer> sentenceLengths = getSentenceLengths(text);
        double diversity = lexicalDiversity(words);
        double repetition = repetitionScore(words, 3);
        double uniformity = sentenceUniformity(sentenceLengths);
        double keywords = keywordScore(text);
        double aiScore = (repetition + (1 - diversity) + uniformity + keywords) / 4.0;
        return aiScore * 100;
    }
}
