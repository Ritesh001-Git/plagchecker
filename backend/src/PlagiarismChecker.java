import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlagiarismChecker{

    public static Map<String, Object> tokenizeText(String text,int n){
        Map<String, Object> result = new HashMap<>();
        // --- 1. Clean the text ---
        text=text.toLowerCase().replaceAll("[^a-z0-9.\\s]", " ").replaceAll("\\s+", " ").trim();

        // --- 2. Tokenize into words ---
        List<String> words=Arrays.asList(text.split(" "));
        result.put("words", words);

        // --- 2. Tokenize into sentences ---
        String[] sentenceArray = text.split("\\.");
        List<Integer> sentenceLengths = new ArrayList<>();
        for(String sentence : sentenceArray){
            String[]sentenceWords = sentence.trim().split("\\s+");
            if (sentenceWords.length > 0 && !sentenceWords[0].isEmpty()) {
                sentenceLengths.add(sentenceWords.length);
            }
        }
        
        // --- 4. Generate n-grams ---
        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i <= words.size() - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + n; j++) {
                sb.append(words.get(j));
                if (j < i + n - 1) sb.append(" ");
            }
            ngrams.add(sb.toString());
        }
        result.put("ngrams", ngrams);
        return result;



    }
    public static void main(String[] args) {
        String inputFile = "This is a sample text for plagiarism checking.";
        int n=3; // n-gram size
        Map<String, Object> tokenizedData = tokenizeText(inputFile, n);
    }
}