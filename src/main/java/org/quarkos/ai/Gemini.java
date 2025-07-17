package org.quarkos.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.*;
import com.google.gson.Gson;
import io.github.cdimascio.dotenv.Dotenv;
import org.quarkos.Model;
import org.quarkos.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.util.*;

public class Gemini {
    static Dotenv dotenv = Dotenv.load();
    static final String GOOGLE_API_KEY = dotenv.get("GOOGLE_API_KEY");
    private static final Logger logger = LoggerFactory.getLogger(Gemini.class);
    private static final Map<String, CachedContent> cacheManager = new HashMap<>();

    public static final Model DEFAULT_MODEL = Model.GEMINI_2_5_FLASH_LITE_PREVIEW_06_17;
    //public static final Model DEFAULT_SPEECH_MODEL = Model.GEMINI_2_5_FLASH_PREVIEW_TTS;
    public static Model currentModel = DEFAULT_MODEL;

    private static CachedContent cachedContent;

    static Client client = Client.builder()
            .apiKey(GOOGLE_API_KEY)
            .build();

    private final static Chat activeChat = createNewChat();

    private static Schema createSongArtistSchema() {
        return Schema.builder()
                .type("object")
                .properties(
                        ImmutableMap.of(
                                "songName", Schema.builder().type(Type.Known.STRING).description("The name of the song.").build(),
                                "artist", Schema.builder().type(Type.Known.STRING).description("The artist of the song.").build()
                        ))
                .build();
    }

    private static Schema createDefaultSchema() {
        return Schema.builder()
                .type("object")
                .properties(
                        ImmutableMap.of(
                                "Text", Schema.builder().type(Type.Known.STRING).description("The answer to the user's prompt.").build()
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
                    .cachedContent(cachedContent.toString())

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

    private static Map.Entry<String, Long> executeGeneration(String modelName, Content content, GenerateContentConfig config) {
        TimerUtil.lap("Content preparation");

        GenerateContentResponse response = activeChat.sendMessage(content.text()); // Have an active session
        // client.models.generateContent("models/" + modelName, content, config); // old method (one message context only basically)

        TimerUtil.lap("AI response generation");

        ClipboardUtil.copyToClipboard(JSONUtil.extractTextFromResponse(response.text()));
        long elapsedTime = TimerUtil.stop();
        return new AbstractMap.SimpleEntry<>(response.text(), elapsedTime);
    }

    public static Map.Entry<String, Long> extractSongFromAudio(byte[] audioBytes, String modelName) {
        TimerUtil.start();

        String prompt = "From the following audio, extract the song name and the artist. Return the result in a JSON format with the keys 'songName' and 'artist'.";

        List<Part> parts = new ArrayList<>();
        parts.add(Part.fromText(prompt));
        parts.add(Part.fromBytes(audioBytes, "audio/wav"));

        Content content = Content.fromParts(parts.toArray(new Part[0]));

        Schema schema = createSongArtistSchema();
        GenerateContentConfig config = createDefaultConfig(schema);
        TimerUtil.lap("Content preparation");

        GenerateContentResponse response =
                client.models.generateContent(modelName, content, config);
        TimerUtil.lap("AI response generation");

        long elapsedTime = TimerUtil.stop();
        return new AbstractMap.SimpleEntry<>(response.text(), elapsedTime);
    }

    public static Map.Entry<String, Long> generateStructuredResponse(String prompt) {
        TimerUtil.start();
        Content content = Content.fromParts(Part.fromText(prompt));
        Schema schema = createDefaultSchema();
        GenerateContentConfig config = createDefaultConfig(schema);

        System.out.println("Generating structured response for prompt: " + prompt);

        return executeGeneration(Gemini.currentModel.getModelName(), content, config);
    }

    public static Map.Entry<String, Long> generateStructuredResponseWithImageData(String prompt) {
        TimerUtil.start();
        byte[] screenshotBytes;

        try {
            screenshotBytes = ScreenshotUtil.getScreenshot();
        } catch (Exception e) {
            logger.error("Failed to get screenshot:", e);
            throw new RuntimeException(e);
        }
        TimerUtil.lap("Screenshot taken");
        Content content =
                Content.fromParts(
                        Part.fromText(prompt),
                        Part.fromBytes(screenshotBytes, "image/png")
                );
        Schema schema = createDefaultSchema();
        GenerateContentConfig config = createDefaultConfig(schema);
        return executeGeneration(Gemini.currentModel.getModelName(), content, config);
    }

    private static Object uploadFile(String filePath) {
        UploadFileConfig uploadConfig = UploadFileConfig.builder()
                .mimeType("application/pdf")
                .build();

        logger.info("Uploading file: " + filePath);

        return client.files.upload(new File(filePath), uploadConfig);
    }

    private static CachedContent getOrCreateCachedContent(Map<String, byte[]> contexts, String modelName) {
        String cacheKey = "context_cache_for_" + modelName;

        if (cacheManager.containsKey(cacheKey)) {
            logger.info("Cache hit! Reusing existing cached content for model: {}", modelName);
            return cacheManager.get(cacheKey);
        }

        logger.info("Cache miss. Creating a new cached content for model: {}", modelName);
        logger.info("This one-time process may take a moment...");

        List<Content> contentListForCache = new ArrayList<>();
        for (Map.Entry<String, byte[]> entry : contexts.entrySet()) {
            String fileName = entry.getKey();
            byte[] fileBytes = entry.getValue();

            logger.info("   - Uploading '{}' ({} bytes) to the File API...", fileName, fileBytes.length);

            UploadFileConfig uploadConfig = UploadFileConfig.builder()
                    .mimeType("application/pdf")
                    .displayName(fileName)
                    .build();

            com.google.genai.types.File uploadedFile = client.files.upload(fileBytes, uploadConfig);
            logger.info("   - Upload successful. URI: {}", uploadedFile.uri());

            contentListForCache.add(Content.fromParts(Part.fromUri(uploadedFile.uri().get(), "application/pdf")));
        }

        logger.info("All files uploaded. Creating the cache configuration...");
        CreateCachedContentConfig cachedContentConfig = CreateCachedContentConfig.builder()
                .contents(contentListForCache)
                .displayName("Project Context Cache")
                .expireTime(Instant.now().plusSeconds(3600)) // 1h cache time
                .build();

        logger.info("Sending request to create cache for model: {}", modelName);
        CachedContent newCache = client.caches.create(modelName, cachedContentConfig);

        logger.info("Cache created successfully! Name: {}", newCache.name());

        cacheManager.put(cacheKey, newCache);

        return newCache;
    }

    private static GenerateContentConfig createConfigWithCache(Schema schema, CachedContent cache) {
        if (currentModel.isThinkingEnabled()) {
            logger.info("Thinking enabled | supported");
        } else {
            logger.info("Thinking disabled | not supported by this model");
        }

        String cacheName = cache.name()
                .orElseThrow(() -> new IllegalStateException("CachedContent is missing a name. Cannot proceed."));

        return GenerateContentConfig.builder()
                .cachedContent(cacheName)
                .responseMimeType("application/json")
                .responseSchema(schema)
                .build();
    }

    public static Map.Entry<String, Long> generateStructuredResponseWithMultipleContexts(String prompt, Map<String, byte[]> contexts, String modelName) {
        TimerUtil.start();

        CachedContent contextCache = getOrCreateCachedContent(contexts, modelName);

        Content promptContent = Content.fromParts(Part.fromText(prompt));

        Schema schema = createDefaultSchema();
        GenerateContentConfig configWithCache = createConfigWithCache(schema, contextCache);

        return executeGeneration(modelName, promptContent, configWithCache);
    }

    public static FunctionCall addCustomFunctionToGemini(String prompt) {
        Tool tool = Tool.builder()
                .functionDeclarations(FunctionDeclarations.getDeclarations())
                .build();

        List<Content> contents =
                ImmutableList.of(
                        Content.builder()
                                .role("user")
                                .parts(ImmutableList.of(Part.fromText(prompt)))
                                .build());
        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .thinkingConfig(ThinkingConfig.builder().thinkingBudget(-1).build())
                        .tools(ImmutableList.of(tool))
                        .responseMimeType("text/plain")
                        .build();

        ResponseStream<GenerateContentResponse> responseStream =
                client.models.generateContentStream(Gemini.currentModel.getModelName(), contents, config);

        for (GenerateContentResponse res : responseStream) {
            if (res.candidates().isEmpty()
                    || res.candidates().get().get(0).content().isEmpty()
                    || res.candidates().get().get(0).content().get().parts().isEmpty()) {
                continue;
            }

            List<Part> parts = res.candidates().get().get(0).content().get().parts().get();
            for (Part part : parts) {
                if (part.functionCall().isPresent()) {
                    return part.functionCall().get();
                } else {
                    return null;
                }
            }
        }

        responseStream.close();
        return null;
    }

    private static Chat createNewChat() {
        return client.chats.create(Gemini.currentModel.getModelName(), Gemini.createDefaultConfig(Gemini.createDefaultSchema()));
    }
}