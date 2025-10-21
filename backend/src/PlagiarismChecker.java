import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlagiarismChecker{

    public static Map<String, Object> tokenizeText(String text,int n){
        Map<String, Object> result = new HashMap<>();
        text=text.toLowerCase().replaceAll("[^a-z0-9.\\s]", " ").replaceAll("\\s+", " ").trim();

        List<String> words=Arrays.asList(text.split(" "));
        result.put("words", words);

        



    }
    public static void main(String[] args) {
        String inputFile = "This is a sample text for plagiarism checking.";
        int n=3; // n-gram size
        Map<String, Object> tokenizedData = tokenizeText(inputFile, n);
    }
}