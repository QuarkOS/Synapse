package org.quarkos.ai;

import com.google.genai.types.FunctionCall;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GeminiTest {

    @Test
    public void generateStructuredResponse() {
        Map.Entry<String, Long> response = Gemini.generateStructuredResponse("What is the capital of France?");
        System.out.printf("Response: %s, Time taken: %dms%n", response.getKey(), response.getValue());
        assertNotNull(response.getKey(), "Response should not be null");
        assertTrue(response.getKey().contains("Paris"));
    }

    @Test
    public void generateStructuredResponseWithImageData() {
    }

    @Test
    public void generateStructuredResponseWithMultipleContexts() {

    }

    @Test
    public void addCustomFunctionToGeminiTest() {
        FunctionCall decision = Gemini.addCustomFunctionToGemini("Whats the weather like in St. Johann im Pongau?");
        System.out.println("Decision: " + decision.name());
        System.out.println("Decision: " + decision.id());

        switch (decision.name().get()) {
            case "getWeather" -> {
                System.out.println("Decision: getWeather");
                System.out.println(decision.args().get());
                assertTrue(decision.args().get().containsKey("location"));
                assertTrue(decision.args().get().toString().contains("St. Johann im Pongau"));
            }
            default -> {
                System.out.println("Decision: " + decision.name());
            }
        }

    }
}