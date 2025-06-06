import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.*;
import io.github.cdimascio.dotenv.Dotenv;

public class Gemini {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        final String GOOGLE_API_KEY = dotenv.get("GOOGLE_API_KEY");

        // Initialize the client with the API key
        Client client = Client.builder()
                .apiKey(GOOGLE_API_KEY)
                .build();

        String output = generateStructuredResponse(client, "Was ist NAT?");
        System.out.println(output);

        System.out.println(JSONUtil.extractTextFromResponse(output));

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
