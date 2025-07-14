package org.quarkos.ai;

import com.google.common.collect.ImmutableMap;
import com.google.genai.Caches;
import com.google.genai.Client;
import com.google.genai.types.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.quarkos.Configuration;
import org.quarkos.Model;
import org.quarkos.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.*;
import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

        GenerateContentResponse response = client.models.generateContent("models/" + modelName, content, config);
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

    public static Map.Entry<String, Long> generateStructuredResponse(String prompt, Model model) {
        TimerUtil.start();
        Content content = Content.fromParts(Part.fromText(prompt));
        Schema schema = createDefaultSchema(prompt);
        GenerateContentConfig config = createDefaultConfig(schema);
        return executeGeneration(model.getModelName(), content, config);
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
        Schema schema = createDefaultSchema(prompt);
        GenerateContentConfig config = createDefaultConfig(schema);
        return executeGeneration(modelName, content, config);
    }

    private static Object uploadFile(String filePath) {
        UploadFileConfig uploadConfig = UploadFileConfig.builder()
                .mimeType("application/pdf")
                .build();

        logger.info("Uploading file: " + filePath);

        return client.files.upload(new File(filePath), uploadConfig);
    }

    private static void createContextCache(String contextName) {
        List<Content> contents = new ArrayList<>();
        for (int i = 0; i < FileUtil.getFileNamesFromDirectory(ContextUtil.PDF_CONTEXT_PATH).length; i++) {
            contents.add(Content.fromParts(Part.fromBytes(FileUtil.readFileAsByteArray(ContextUtil.PDF_CONTEXT_PATH + File.separator + FileUtil.getFileNamesFromDirectory(ContextUtil.PDF_CONTEXT_PATH)[i]), "application/pdf")));
        }

        CreateCachedContentConfig cachedContentConfig = CreateCachedContentConfig.builder()
                .contents(contents)
                .displayName("Context cache for " + contextName)
                .expireTime(Instant.now().plusSeconds(300))
                .build();

        cachedContent = client.caches.create(Gemini.currentModel.getModelName(), cachedContentConfig);
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

        String cacheName = cache.displayName()
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

        Schema schema = createDefaultSchema(prompt);
        GenerateContentConfig configWithCache = createConfigWithCache(schema, contextCache);

        return executeGeneration(modelName, promptContent, configWithCache);
    }

//    public static Optional<byte[]> generateAudioFromText(String text, String modelName) {
//        TimerUtil.start();
//        VoiceConfig voiceConfig = VoiceConfig.builder().prebuiltVoiceConfig(PrebuiltVoiceConfig.builder().voiceName("Kore")).build();
//        GenerateContentConfig speechConfig = GenerateContentConfig.builder().responseModalities("AUDIO")
//                .speechConfig(SpeechConfig.builder().voiceConfig(voiceConfig)).build();
//
//        GenerateContentResponse response = client.models.generateContent(modelName,
//                Content.fromParts(Part.fromText(text)),
//                speechConfig);
//
//        Optional<Blob> inlineData = response.parts().get(0).inlineData();
//        if (inlineData.isPresent()) {
//            return inlineData.get().data();
//        } else {
//            throw new RuntimeException("No inline data found in the response.");
//        }
//    }
//
//    public static void playAudio(String textToSpeak) {
//        logger.info("Requesting audio data from AI...");
//        Optional<byte[]> audioBytesOptional = generateAudioFromText(textToSpeak, Gemini.DEFAULT_SPEECH_MODEL.getModelName());
//
//        audioBytesOptional.ifPresentOrElse(audioBytes -> {
//            try {
//                logger.info("Received {} bytes of MP3 audio data. Preparing for playback.", audioBytes.length);
//                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioBytes);
//
//                try (AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(byteArrayInputStream)) {
//                    AudioFormat baseFormat = mp3Stream.getFormat();
//                    AudioFormat decodedFormat = new AudioFormat(
//                            AudioFormat.Encoding.PCM_SIGNED,
//                            baseFormat.getSampleRate(),
//                            16,
//                            baseFormat.getChannels(),
//                            baseFormat.getChannels() * 2,
//                            baseFormat.getSampleRate(),
//                            false
//                    );
//
//                    logger.info("Decoded audio format: {}", decodedFormat);
//
//                    try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(decodedFormat, mp3Stream);
//                         Clip clip = AudioSystem.getClip()) {
//
//                        clip.open(pcmStream);
//
//                        // --- START OF FIX ---
//
//                        // 1. Start playback (this is non-blocking)
//                        clip.start();
//                        logger.info("Playing audio...");
//
//                        // 2. Wait for playback to complete (this is blocking)
//                        // This replaces the entire while-loop and listener logic.
//                        clip.drain();
//
//                        logger.info("Playback finished.");
//
//                        // --- END OF FIX ---
//                    }
//                }
//            } catch (UnsupportedAudioFileException e) {
//                logger.error("MP3 audio format not supported. Ensure the mp3spi library is in your classpath.", e);
//            } catch (LineUnavailableException e) {
//                logger.error("Audio line for playback is unavailable.", e);
//            } catch (IOException e) {
//                logger.error("I/O error during audio playback.", e);
//            }
//            // InterruptedException is not thrown by drain(), so it's less critical here
//            // but good to keep if you add other blocking calls.
//
//        }, () -> {
//            logger.error("Failed to generate or retrieve audio data for the text: '{}'", textToSpeak);
//        });
//    }
}