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
}
