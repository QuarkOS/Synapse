package org.quarkos.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtil {
    public static String extractTextFromResponse(String jsonString) {
        // Strip markdown code block fences if they exist
        if (jsonString.trim().startsWith("```json")) {
            jsonString = jsonString.trim().substring(7, jsonString.length() - 3).trim();
        } else if (jsonString.trim().startsWith("```")) {
            jsonString = jsonString.trim().substring(3, jsonString.length() - 3).trim();
        }

        JsonNode rootNode = null;
        try {
            rootNode = new ObjectMapper().readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        JsonNode textField = rootNode.get("Text");

        return textField.asText();
    }
}
