import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlagiarismChecker{

    public static List<String> tokenizeText(String text,int n){
        // --- 1. Clean the text ---
        text=text.toLowerCase().replaceAll("[^a-z0-9.\\s]", " ").replaceAll("\\s+", " ").trim();

        // --- 2. Tokenize into words ---
        List<String> words=Arrays.asList(text.split(" "));

        // --- 3. Generate n-grams ---
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
    // 🔹 Function 2: Generate hash values for each token using polynomial rolling hash
    publis static Set<Long>generateHashes(List<String> tokens){
        Set<Long> hashes = new HashSet<>();
        long base = 31;           // base for rolling hash
        long mod = 1000000009L;   // large prime for modulo
        
    }

    public static void main(String[] args) {
        String inputFile = "This is a sample text for plagiarism checking.";
        int n=3; // n-gram size
        List<String> ngrams = tokenizeText(inputFile, n);
    }
}