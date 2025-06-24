package org.quarkos.util;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ContextUtil {

    private static final String PDF_CONTEXT_PATH = "src/main/java/org/quarkos/context/pdf";
    private static final String TXT_CONTEXT_PATH = "src/main/java/org/quarkos/context/txt";

    public static Map<String, byte[]> getAllContexts() {
        Map<String, byte[]> allContexts = new HashMap<>();
        addPdfContextsAsText(allContexts, PDF_CONTEXT_PATH);
        addTxtContextsAsBytes(allContexts, TXT_CONTEXT_PATH);
        return allContexts;
    }

    private static void addPdfContextsAsText(Map<String, byte[]> contexts, String directoryPath) {
        String[] files = FileUtil.getFileNamesFromDirectory(directoryPath);
        if (files != null) {
            for (String fileName : files) {
                if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
                    String filePath = directoryPath + "/" + fileName;
                    String textContent = FileUtil.extractTextFromPdf(filePath);
                    if (textContent != null && !textContent.trim().isEmpty()) {
                        contexts.put(fileName, textContent.getBytes(StandardCharsets.UTF_8));
                    } else {
                        String newKey = fileName + ".txt";
                        String message = "The PDF file '" + fileName + "' contains no extractable text. It might contain only images.";
                        contexts.put(newKey, message.getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        }
    }

    private static void addTxtContextsAsBytes(Map<String, byte[]> contexts, String directoryPath) {
        String[] files = FileUtil.getFileNamesFromDirectory(directoryPath);
        if (files != null) {
            for (String fileName : files) {
                if (fileName != null && !fileName.isEmpty()) {
                    String filePath = directoryPath + "/" + fileName;
                    byte[] bytes = FileUtil.readFileAsByteArray(filePath);
                    if (bytes != null) {
                        contexts.put(fileName, bytes);
                    }
                }
            }
        }
    }
}