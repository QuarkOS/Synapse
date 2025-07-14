package org.quarkos.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ConfigurationValidator utility.
 * Tests configuration validation logic without requiring actual .env files.
 */
class ConfigurationValidatorTest {

    @TempDir
    Path tempDir;
    
    private String originalUserDir;

    @BeforeEach
    void setUp() {
        // Save original working directory
        originalUserDir = System.getProperty("user.dir");
        // Set working directory to temp directory for testing
        System.setProperty("user.dir", tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        // Restore original working directory
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void testValidateConfiguration_MissingEnvFile() {
        // Test case: No .env file exists
        boolean result = ConfigurationValidator.validateConfiguration(
            tempDir.resolve("nonexistent/.env").toString());
        assertFalse(result, "Configuration validation should fail when .env file is missing");
    }

    @Test
    void testValidateConfiguration_EmptyEnvFile() throws IOException {
        // Create empty .env file
        String envPath = createEnvFile("");
        
        boolean result = ConfigurationValidator.validateConfiguration(envPath);
        assertFalse(result, "Configuration validation should fail when API key is missing");
    }

    @Test
    void testValidateConfiguration_PlaceholderApiKey() throws IOException {
        // Create .env file with placeholder API key
        String envPath = createEnvFile("GOOGLE_API_KEY=your_api_key_here");
        
        boolean result = ConfigurationValidator.validateConfiguration(envPath);
        assertFalse(result, "Configuration validation should fail when API key is placeholder");
    }

    @Test
    void testValidateConfiguration_ShortApiKey() throws IOException {
        // Create .env file with short API key (likely invalid)
        String envPath = createEnvFile("GOOGLE_API_KEY=short");
        
        // This should pass validation but with warnings
        // Since warnings don't fail validation, it should return true
        boolean result = ConfigurationValidator.validateConfiguration(envPath);
        assertTrue(result, "Configuration validation should pass with warnings for short API key");
    }

    @Test
    void testValidateConfiguration_ValidApiKey() throws IOException {
        // Create .env file with valid-looking API key
        String envPath = createEnvFile("GOOGLE_API_KEY=AIzaSyAbCdEfGhIjKlMnOpQrStUvWxYz1234567");
        
        boolean result = ConfigurationValidator.validateConfiguration(envPath);
        assertTrue(result, "Configuration validation should pass with valid API key");
    }

    @Test
    void testValidateConfiguration_WithOptionalSettings() throws IOException {
        // Create .env file with multiple settings
        String content = """
            GOOGLE_API_KEY=AIzaSyAbCdEfGhIjKlMnOpQrStUvWxYz1234567
            WHISPER_MODEL=base
            WHISPER_DEVICE=cpu
            """;
        String envPath = createEnvFile(content);
        
        boolean result = ConfigurationValidator.validateConfiguration(envPath);
        assertTrue(result, "Configuration validation should pass with valid settings");
    }

    @Test
    void testValidateConfiguration_InvalidWhisperModel() throws IOException {
        // Create .env file with invalid Whisper model
        String content = """
            GOOGLE_API_KEY=AIzaSyAbCdEfGhIjKlMnOpQrStUvWxYz1234567
            WHISPER_MODEL=invalid_model
            """;
        String envPath = createEnvFile(content);
        
        // Should pass validation but with warnings
        boolean result = ConfigurationValidator.validateConfiguration(envPath);
        assertTrue(result, "Configuration validation should pass with warnings for invalid Whisper model");
    }

    @Test
    void testPrintSetupGuidance() {
        // Test that setup guidance doesn't throw exceptions
        assertDoesNotThrow(() -> {
            ConfigurationValidator.printSetupGuidance();
        }, "Setup guidance should not throw exceptions");
    }

    /**
     * Helper method to create a .env file in the test directory
     * @return the path to the created .env file
     */
    private String createEnvFile(String content) throws IOException {
        File envFile = tempDir.resolve(".env").toFile();
        try (FileWriter writer = new FileWriter(envFile)) {
            writer.write(content);
        }
        return envFile.getAbsolutePath();
    }
}