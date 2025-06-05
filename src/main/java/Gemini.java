import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.checkerframework.checker.units.qual.C;

public class Gemini {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        final String GOOGLE_API_KEY = dotenv.get("GOOGLE_API_KEY");

        Client client = Client.builder()
                .apiKey(GOOGLE_API_KEY)
                .build();

//        String prompt = "What is the capital of France?";
//        GenerateContentResponse response = client.models.generateContent(
//                "gemini-2.5-flash-preview-05-20",
//                prompt,
//                null
//        );

        System.out.println(generateStructuredResponse(client, "Was ist NAT?"));

    }

    public static String generateStructuredResponse(Client client, String prompt) {
        Schema schema =
                Schema.builder()
                        .type("object")
                        .properties(
                                ImmutableMap.of(
                                        "Text", Schema.builder().type(Type.Known.STRING).description("Concise Answer to the current quiz question").build()
                                ))
                        .build();
        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("application/json")
                        .candidateCount(1)
                        .responseSchema(schema)
                        .build();

        GenerateContentResponse response =
                client.models.generateContent("gemini-2.5-flash-preview-05-20", prompt, config);

        return response.text();
    }
}
