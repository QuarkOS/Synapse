package org.quarkos.ai;

import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.quarkos.util.ClipboardUtil;
import org.quarkos.util.JSONUtil;
import org.quarkos.util.ScreenshotUtil;

public class Gemini {
    static Dotenv dotenv = Dotenv.load();
    static final String GOOGLE_API_KEY = dotenv.get("GOOGLE_API_KEY");

    // Initialize the client with the API key
    static Client client = Client.builder()
            .apiKey(GOOGLE_API_KEY)
            .build();

    public static String generateStructuredResponse(String prompt) {
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


        ClipboardUtil.copyToClipboard(JSONUtil.extractTextFromResponse(response.text()));
        return response.text();
    }

    public static String generateStructuredResponseWithImageData(String prompt) {
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

        byte[] screenshotBytes = null;

        try {
            screenshotBytes = ScreenshotUtil.getScreenshot();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Content content =
                Content.fromParts(
                        Part.fromText(prompt),
                        Part.fromBytes(screenshotBytes, "image/png")
                );

        GenerateContentResponse response =
                client.models.generateContent("gemini-2.5-flash-preview-05-20", content, config);

        ClipboardUtil.copyToClipboard(JSONUtil.extractTextFromResponse(response.text()));
        return response.text();
    }
}
