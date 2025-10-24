import java.util.*;

public class PlagiarismCheckerAi {

    // 🧩 1. Clean and tokenize text
    public static List<String> getWords(String text) {
        text = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        String[] parts = text.split(" ");
        List<String> words = new ArrayList<>();
        for (String p : parts) if (!p.isEmpty()) words.add(p);
        return words;
    }

    // 🧩 2. Split into sentence lengths
    public static List<Integer> getSentenceLengths(String text) {
        String[] sentences = text.split("[.!?]");
        List<Integer> lengths = new ArrayList<>();
        for (String s : sentences) {
            String[] words = s.trim().split("\\s+");
            int count = 0;
            for (String w : words) if (!w.isEmpty()) count++;
            if (count > 1) lengths.add(count);
        }
        return lengths;
    }

    // 🧠 Lexical diversity (unique words ÷ total words)
    public static double lexicalDiversity(List<String> words) {
        if (words.isEmpty()) return 0;
        Set<String> unique = new HashSet<>(words);
        return (double) unique.size() / words.size();
    }

    // 🔁 Repetition score (frequency of repeating n-grams)
    public static double repetitionScore(List<String> words, int n) {
        if (words.size() < n) return 0;
        Map<String, Integer> ngrams = new HashMap<>();
        for (int i = 0; i <= words.size() - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + n; j++) {
                sb.append(words.get(j));
                if (j < i + n - 1) sb.append(" ");
            }
            String gram = sb.toString();
            ngrams.put(gram, ngrams.getOrDefault(gram, 0) + 1);
        }
        int repeats = 0;
        for (int count : ngrams.values()) if (count > 1) repeats++;
        return ngrams.isEmpty() ? 0 : (double) repeats / ngrams.size();
    }

    // 📏 Sentence uniformity (inverse of variance)
    public static double sentenceUniformity(List<Integer> lengths) {
        if (lengths.size() < 2) return 0;
        double mean = lengths.stream().mapToDouble(x -> x).average().orElse(0);
        double variance = 0;
        for (double len : lengths) variance += Math.pow(len - mean, 2);
        variance /= lengths.size();
        double stddev = Math.sqrt(variance);
        return 1.0 / (1.0 + stddev);
    }

    // 🌊 Burstiness (variance / mean ratio)
    public static double burstiness(List<Integer> lengths) {
        if (lengths.size() < 2) return 0;
        double mean = lengths.stream().mapToDouble(x -> x).average().orElse(0);
        if (mean < 0.001) return 0;
        double variance = 0;
        for (double len : lengths) variance += Math.pow(len - mean, 2);
        variance /= lengths.size();
        return Math.sqrt(variance) / mean;
    }

    // 🔡 Entropy (word randomness)
    public static double entropy(List<String> words) {
        if (words.isEmpty()) return 0;
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) freq.put(w, freq.getOrDefault(w, 0) + 1);
        double total = words.size();
        double entropy = 0;
        for (int f : freq.values()) {
            double p = f / total;
            if (p > 0) entropy += -p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    // 🪶 Simplicity (short average word length = more AI-like)
    public static double simplicity(List<String> words) {
        if (words.isEmpty()) return 0;
        double avgLen = words.stream().mapToInt(String::length).average().orElse(0);
        return Math.max(0, Math.min(1, (7.0 - avgLen) / 3.0));
    }

    // 📚 Keyword score (AI summary markers)
    public static double keywordScore(String text) {
        String[] cues = {
            "in conclusion", "overall", "in summary", "therefore", 
            "it is important to note", "furthermore", "moreover",
            "in other words", "to summarize", "as a result"
        };
        text = text.toLowerCase();
        int count = 0;
        for (String cue : cues) if (text.contains(cue)) count++;
        return Math.min(1.0, count / 5.0);
    }

    // ⚖️ Weighted AI Score with improved detection for modern AI text
public static Map<String, Double> calculateAIScoreDetailed(String text) {
    List<String> words = getWords(text);
    List<Integer> sentLens = getSentenceLengths(text);

    if (words.size() < 10) {
        Map<String, Double> result = new LinkedHashMap<>();
        result.put("ai_percent", 0.0);
        result.put("diversity", 100.0);
        result.put("repetition", 0.0);
        result.put("uniformity", 0.0);
        result.put("burstiness", 0.0);
        result.put("keywords", 0.0);
        return result;
    }

    double diversity = lexicalDiversity(words);
    double repetition = repetitionScore(words, 3);
    double uniformity = sentenceUniformity(sentLens);
    double burst = burstiness(sentLens);
    double entr = entropy(words);
    double simple = simplicity(words);
    double keywords = keywordScore(text);

    // AI detection logic
    // 1️⃣ Low burstiness strongly indicates AI
    double burstScore;
    if (burst < 0.25) burstScore = 1.0;
    else if (burst < 0.35) burstScore = 0.85;
    else if (burst < 0.5) burstScore = 0.6;
    else burstScore = 0.3;

    // 2️⃣ Adjusted diversity score: modern AI has high diversity
    // High diversity with low burstiness is more AI-like
    double diversityScore = 1.0 - diversity; // keep previous logic but weight later

    // 3️⃣ Uniformity (sentence length consistency)
    double uniformityScore = uniformity; // AI often has uniform sentence length

    // 4️⃣ Entropy normalization
    double entropyScore = Math.min(1.0, entr / 7.0); // weak signal

    // 5️⃣ Simplicity and keywords
    double simpleScore = simple;    // short words = AI
    double keywordScore = keywords; // summary markers

    // Weighted AI score
    double aiScore = (
        burstScore * 0.35 +          // strongest signal
        uniformityScore * 0.25 +     // consistent sentence length
        diversityScore * 0.15 +      // low diversity = AI
        repetition * 0.10 +          // repetitive n-grams
        entropyScore * 0.05 +        // entropy
        simpleScore * 0.05 +         // word simplicity
        keywordScore * 0.05          // AI keywords
    );

    // Boost for long texts
    if (words.size() > 300) {
        double lengthBonus = Math.min(0.25, (words.size() - 300) / 3000.0);
        aiScore += aiScore * lengthBonus;
    }

    // Ensure fully AI-like text gets >90% if burstiness is very low and uniformity high
    if (burst < 0.3 && uniformity > 0.5) {
        aiScore = Math.max(aiScore, 0.9);
    }

    // Clamp between 0 and 1
    aiScore = Math.max(0, Math.min(1, aiScore));
    double finalPercent = aiScore * 100.0;

    // Return metrics
    Map<String, Double> result = new LinkedHashMap<>();
    result.put("ai_percent", finalPercent);
    result.put("diversity", diversity * 100);
    result.put("repetition", repetition * 100);
    result.put("uniformity", uniformity * 100);
    result.put("burstiness", burst * 100);
    result.put("keywords", keywords * 100);

    return result;
}

    // Legacy version
    public static double calculateAIScore(String text) {
        return calculateAIScoreDetailed(text).get("ai_percent");
    }
}
