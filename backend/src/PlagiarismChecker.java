public class PlagiarismChecker{

    public static void removePunctuation(String text){
        text=text.toLowerCase();
        text=text.replaceAll("[^a-zA-Z0-9\\s]", "");
    }
    public static void tokenizeText(String text){
        String[] tokens = text.split("\\s+");

    }
    public static void main(String[] args) {
        String inputFile = "This is a sample text for plagiarism checking.";
    }
}