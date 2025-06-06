import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtil {
    public static String extractTextFromResponse(String jsonString) {
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
