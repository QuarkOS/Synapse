import org.quarkos.ai.Gemini;

public class Main {
    public static void main(String[] args) {
        // Using the new method that reads the PDF directly without server upload
        Gemini.generateStructuredResponseWithPDFPath(
            "Read the entire PDF and output the entire text please",
            "Die_Chemie_der_Düfte&Aromen.pdf"
        );
    }
}

