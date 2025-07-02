package org.quarkos.ai;

import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.quarkos.Model;
import org.quarkos.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Gemini {
    static Dotenv dotenv = Dotenv.load();
    static final String GOOGLE_API_KEY = dotenv.get("GOOGLE_API_KEY");
    private static final Logger logger = LoggerFactory.getLogger(Gemini.class);

    public static final Model DEFAULT_MODEL = Model.GEMINI_2_5_FLASH_LITE_PREVIEW_06_17; // Default model to use if none is specified
    public static Model currentModel = DEFAULT_MODEL; // Current model to use, can be changed by the user

    // Initialize the client with the API key
    static Client client = Client.builder()
            .apiKey(GOOGLE_API_KEY)
            .build();

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
        if (currentModel.isThinkingEnabled()) {
            logger.info("Thinking enabled | supported");

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
        } else {
            logger.info("Thinking disabled | not supported by this model");

            return GenerateContentConfig
                    .builder()
                    .responseMimeType("application/json")
                    .responseSchema(schema)
                    .build();
        }
    }

    private static Map.Entry<String, Long> executeGeneration(String modelName, Content content, String prompt) {
        Schema schema = createDefaultSchema(prompt);
        GenerateContentConfig config = createDefaultConfig(schema);
        TimerUtil.lap("Content preparation");

        GenerateContentResponse response =
                client.models.generateContent(modelName, content, config);
        TimerUtil.lap("AI response generation");

        ClipboardUtil.copyToClipboard(JSONUtil.extractTextFromResponse(response.text()));
        long elapsedTime = TimerUtil.stop();
        return new AbstractMap.SimpleEntry<>(response.text(), elapsedTime);
    }

    public static Map.Entry<String, Long> generateStructuredResponse(String prompt, Model model) {
        TimerUtil.start();
        Content content = Content.fromParts(Part.fromText(prompt));
        return executeGeneration(model.getModelName(), content, prompt);
    }

    public static Map.Entry<String, Long> generateStructuredResponseWithImageData(String prompt, String modelName) {
        TimerUtil.start();
        byte[] screenshotBytes;

        try {
            screenshotBytes = ScreenshotUtil.getScreenshot();
        } catch (Exception e) {
            logger.error("Failed to get screenshot: " + e.getMessage());
            throw new RuntimeException(e);
        }
        TimerUtil.lap("Screenshot taken");
        Content content =
                Content.fromParts(
                        Part.fromText(prompt),
                        Part.fromBytes(screenshotBytes, "image/png")
                );
        return executeGeneration(modelName, content, prompt);
    }

    public static Map.Entry<String, Long> generateStructuredResponseWithMultipleContexts(String prompt, Map<String, byte[]> contexts, String modelName) {
        TimerUtil.start();

        List<Part> parts = new ArrayList<>();
        parts.add(Part.fromText(prompt));

        for (Map.Entry<String, byte[]> entry : contexts.entrySet()) {
            byte[] fileBytes = entry.getValue();
            String mimeType;
            mimeType = "text/plain"; // Currently only text files are supported, but this could be extended later

            parts.add(Part.fromBytes(fileBytes, mimeType));
        }

        Content content = Content.fromParts(parts.toArray(new Part[0]));

        return executeGeneration(modelName, content, prompt);
    }
}