package org.quarkos.ai;

import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.quarkos.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Gemini {
    static Dotenv dotenv = Dotenv.load();
    static final String GOOGLE_API_KEY = dotenv.get("GOOGLE_API_KEY");

    public static final List<String> models = List.of( // Might turn into an enum later, but for now this is fine
        "gemini-2.5-flash-preview-05-20",
        "gemini-2.5-flash",
        //"gemini-2.5-pro", // Probably too slow and most definitely too expensive; Might add usage for premium API users later
        "gemini-2.5-flash-lite-preview-06-17", // Perfect for quiz questions, fast and good
        "gemini-2.0-flash", // Pretty good, pretty much the same as gemini-2.5-flash-lite-preview-06-17, however the answers are sometimes not elaborated enough
        "gemini-2.0-flash-lite" // It's alright, but not as good as gemini-2.5-flash-lite-preview-06-17
    );

    // Initialize the client with the API key
    static Client client = Client.builder()
        .apiKey(GOOGLE_API_KEY)
        .build();

    public static Map.Entry<String, Long> generateStructuredResponse(String prompt, String modelName) {
        TimerUtil.start();
        Schema schema = createDefaultSchema(prompt);
        GenerateContentConfig config = createDefaultConfig(schema);

        GenerateContentResponse response =
                client.models.generateContent(modelName, prompt, config);
        TimerUtil.lap("AI response generation");

        ClipboardUtil.copyToClipboard(JSONUtil.extractTextFromResponse(response.text()));
        return new AbstractMap.SimpleEntry<>(response.text(), TimerUtil.stop());
    }

    public static Map.Entry<String, Long> generateStructuredResponseWithImageData(String prompt, String modelName) {
        TimerUtil.start();
        Schema schema = createDefaultSchema(prompt);
        GenerateContentConfig config = createDefaultConfig(schema);

        byte[] screenshotBytes;

        try {
            screenshotBytes = ScreenshotUtil.getScreenshot();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        TimerUtil.lap("Screenshot taken");
        Content content =
                Content.fromParts(
                        Part.fromText(prompt),
                        Part.fromBytes(screenshotBytes, "image/png")
                );
        TimerUtil.lap("Content preparation");

        GenerateContentResponse response =
                client.models.generateContent(modelName, content, config);
        TimerUtil.lap("AI response generation");

        ClipboardUtil.copyToClipboard(JSONUtil.extractTextFromResponse(response.text()));
        return new AbstractMap.SimpleEntry<>(response.text(), TimerUtil.stop());
    }

    public static Map.Entry<String, Long> generateStructuredResponseWithMultipleContexts(String prompt, Map<String, byte[]> contexts, String modelName) {
        TimerUtil.start();
        Schema schema = createDefaultSchema(prompt);
        GenerateContentConfig config = createDefaultConfig(schema);

        List<Part> parts = new ArrayList<>();
        parts.add(Part.fromText(prompt));

        for (Map.Entry<String, byte[]> entry : contexts.entrySet()) {
            byte[] fileBytes = entry.getValue();
            String mimeType;
            mimeType = "text/plain"; // Currently only text files are supported, but this could be extended later

            parts.add(Part.fromBytes(fileBytes, mimeType));
        }

        Content content = Content.fromParts(parts.toArray(new Part[0]));
        TimerUtil.lap("Content preparation");

        GenerateContentResponse response =
                client.models.generateContent(modelName, content, config);
        TimerUtil.lap("AI response generation");

        ClipboardUtil.copyToClipboard(JSONUtil.extractTextFromResponse(response.text()));
        long elapsedTime = TimerUtil.stop();
        return new AbstractMap.SimpleEntry<>(response.text(), elapsedTime);
    }

    private static Schema createDefaultSchema(String prompt) {
        return Schema.builder()
                .type("object")
                .properties(
                        ImmutableMap.of(
                                "Text", Schema.builder().type(Type.Known.STRING).description(prompt).build()
                        ))
                .build();
    }

    private static GenerateContentConfig createDefaultConfig(Schema schema) {
        return GenerateContentConfig
                .builder()
                .thinkingConfig(
                        ThinkingConfig
                                .builder()
                                .thinkingBudget(-1)
                                .build()
                )
                .responseMimeType("application/json")
                .responseSchema(schema)
                .build();
    }
}
