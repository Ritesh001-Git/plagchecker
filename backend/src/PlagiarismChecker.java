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

        String[] sentenceArray = text.split("\\.");
        List<Integer> sentenceLengths = new ArrayList<>();
        for(String sentence : sentenceArray){
            String[]sentenceWords = sentence.trim().split("\\s+");
            if (sentenceWords.length > 0 && !sentenceWords[0].isEmpty()) {
                sentenceLengths.add(sentenceWords.length);
            }
        }
        

        return result;



    }
    public static void main(String[] args) {
        String inputFile = "This is a sample text for plagiarism checking.";
        int n=3; // n-gram size
        Map<String, Object> tokenizedData = tokenizeText(inputFile, n);
    }
}