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

    public static final List<String> models = List.of(
            "gemini-2.5-flash-preview-05-20", // Zu langsam
            "gemini-2.5-flash", // Zu langsam
            //"gemini-2.5-pro", // Wahrscheinlich zu langsam und sicherlich zu teuer
            "gemini-2.5-flash-lite-preview-06-17", // Perfekt für Quizantworten, schnell und präzise
            "gemini-2.0-flash", // Gut für richtige und schnelle (jedoch nicht immer ganz ausführliche) Antworten
            "gemini-2.0-flash-lite" // Gut
    );

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
                                        "Text", Schema.builder().type(Type.Known.STRING).description(prompt).build()
                                ))
                        .build();
        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("application/json")
                        .candidateCount(1)
                        .responseSchema(schema)
                        .build();

        GenerateContentResponse response =
                client.models.generateContent("gemini-2.0-flash-lite", prompt, config);


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

        byte[] screenshotBytes;

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

    public static String generateStructuredResponseWithPDFContext(String prompt) {
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

        try {
            File file = new File("quiz_context.pdf");
            System.out.println("Uploading PDF file: " + file.getAbsolutePath());
            com.google.genai.types.File pdfFile = client.files.upload(new FileInputStream(file), file.getTotalSpace(), UploadFileConfig.builder().mimeType("application/pdf").build());
            System.out.println("PDF file uploaded successfully.");

            Content content =
                    Content.fromParts(
                            Part.fromText(prompt),
                            Part.fromUri(pdfFile.uri().get(), "application/pdf")
                    );
            System.out.println("Generating response with PDF context...");

            GenerateContentResponse response =
                    client.models.generateContent("gemini-2.5-flash", content, config);
            System.out.println("Response generated successfully.");

            ClipboardUtil.copyToClipboard(JSONUtil.extractTextFromResponse(response.text()));
            System.out.println("Response copied to clipboard.");
            return response.text();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateStructuredResponseWithPDFBytes(String prompt, byte[] pdfBytes) {
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

        System.out.println("Creating content with PDF data...");

        // Create content directly from PDF bytes (no file upload needed)
        Content content =
                Content.fromParts(
                        Part.fromText(prompt),
                        Part.fromBytes(pdfBytes, "application/pdf")
                );

        System.out.println("Generating response with inline PDF data...");

        GenerateContentResponse response =
                client.models.generateContent("gemini-2.5-flash", content, config);

        System.out.println("Response generated successfully.");
        System.out.println("FULL RESPONSE: " + response.text());

        ClipboardUtil.copyToClipboard(JSONUtil.extractTextFromResponse(response.text()));
        System.out.println("Response copied to clipboard.");
        return response.text();
    }

    /**
     * Convenience method that loads a PDF file and processes it
     *
     * @param prompt   The prompt to send with the PDF
     * @param pdfName  Name to the PDF file
     * @return The response text
     */
    public static String generateStructuredResponseWithPDFPath(String prompt, String pdfName) {
        try {
            System.out.println("Reading PDF file: " + pdfName);
            Path pdfPath = Paths.get("src/main/java/org/quarkos/context/pdf/" + pdfName);
            System.out.println("Full Path: " + pdfPath.toAbsolutePath());
            byte[] pdfBytes = Files.readAllBytes(pdfPath);
            return generateStructuredResponseWithPDFBytes(prompt, pdfBytes);
        } catch (IOException e) {
            System.err.println("Error reading PDF file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static Map.Entry<String, Long> generateStructuredResponseWithMultipleContexts(String prompt, Map<String, byte[]> contexts, String modelName) {
        TimerUtil.start();
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

        List<Part> parts = new ArrayList<>();
        parts.add(Part.fromText(prompt));

        for (Map.Entry<String, byte[]> entry : contexts.entrySet()) {
            byte[] fileBytes = entry.getValue();
            String mimeType;
            mimeType = "text/plain";

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
}
