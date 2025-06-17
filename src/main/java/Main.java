import org.quarkos.ai.Gemini;

public class Main {
    public static void main(String[] args) {
        // Using the new method that reads the PDF directly without server upload
        Gemini.generateStructuredResponseWithPDFPath(
            "Generate a summary of the PDF document provided in the context.",
            "quiz_context.pdf"
        );
    }
}