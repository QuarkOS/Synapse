package org.quarkos.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for validating configuration and environment setup.
 * Provides methods to check required configurations and provide helpful error messages.
 */
public class ConfigurationValidator {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationValidator.class);
    
    /**
     * Validates the application configuration and reports any issues.
     * 
     * @return true if configuration is valid, false otherwise
     */
    public static boolean validateConfiguration() {
        return validateConfiguration("src/main/resources/.env");
    }
    
    /**
     * Validates the application configuration with a custom .env file path.
     * 
     * @param envFilePath path to the .env file
     * @return true if configuration is valid, false otherwise
     */
    public static boolean validateConfiguration(String envFilePath) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Check if .env file exists
        File envFile = new File(envFilePath);
        if (!envFile.exists()) {
            errors.add("Missing .env file at " + envFilePath);
            errors.add("Please create the file and add your GOOGLE_API_KEY");
        } else {
            try {
                // Load from custom path if testing
                Dotenv dotenv;
                if (envFilePath.equals("src/main/resources/.env")) {
                    dotenv = Dotenv.load();
                } else {
                    dotenv = Dotenv.configure()
                            .directory(envFile.getParent())
                            .filename(envFile.getName())
                            .load();
                }
                
                // Validate Google API Key
                String apiKey = dotenv.get("GOOGLE_API_KEY");
                if (apiKey == null || apiKey.trim().isEmpty()) {
                    errors.add("GOOGLE_API_KEY is not set in .env file");
                } else if (apiKey.equals("your_api_key_here")) {
                    errors.add("GOOGLE_API_KEY still contains placeholder value");
                    errors.add("Please replace with your actual Google API key");
                } else if (apiKey.length() < 20) {
                    warnings.add("GOOGLE_API_KEY appears to be too short - please verify it's correct");
                }
                
                // Check for optional configurations
                String whisperModel = dotenv.get("WHISPER_MODEL");
                if (whisperModel != null && !isValidWhisperModel(whisperModel)) {
                    warnings.add("Invalid WHISPER_MODEL specified: " + whisperModel);
                }
                
            } catch (Exception e) {
                errors.add("Error reading .env file: " + e.getMessage());
            }
        }
        
        // Check Java version
        String javaVersion = System.getProperty("java.version");
        if (!isValidJavaVersion(javaVersion)) {
            warnings.add("Java version " + javaVersion + " may not be compatible. Java 17+ recommended.");
        }
        
        // Print validation results
        if (!errors.isEmpty()) {
            logger.error("Configuration validation failed:");
            for (String error : errors) {
                logger.error("  ❌ " + error);
            }
        }
        
        if (!warnings.isEmpty()) {
            logger.warn("Configuration warnings:");
            for (String warning : warnings) {
                logger.warn("  ⚠️ " + warning);
            }
        }
        
        if (errors.isEmpty() && warnings.isEmpty()) {
            logger.info("✅ Configuration validation passed");
        }
        
        return errors.isEmpty();
    }
    
    /**
     * Validates if the specified Whisper model is supported.
     */
    private static boolean isValidWhisperModel(String model) {
        String[] validModels = {"tiny", "base", "small", "medium", "large", "large-v2", "large-v3"};
        for (String validModel : validModels) {
            if (validModel.equals(model)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if the Java version is compatible (17+).
     */
    private static boolean isValidJavaVersion(String version) {
        try {
            // Extract major version number
            String[] parts = version.split("\\.");
            int majorVersion;
            
            if (parts[0].equals("1")) {
                // Java 8 format: 1.8.0_xxx
                majorVersion = Integer.parseInt(parts[1]);
            } else {
                // Java 9+ format: 11.0.2, 17.0.1, etc.
                majorVersion = Integer.parseInt(parts[0]);
            }
            
            return majorVersion >= 17;
        } catch (Exception e) {
            logger.warn("Could not parse Java version: " + version);
            return true; // Assume it's fine if we can't parse
        }
    }
    
    /**
     * Provides setup guidance for first-time users.
     */
    public static void printSetupGuidance() {
        System.out.println("\n🧠 Synapse Setup Guide");
        System.out.println("====================");
        System.out.println();
        System.out.println("To get started with Synapse:");
        System.out.println();
        System.out.println("1. 🔑 Get a Google Gemini API Key:");
        System.out.println("   • Visit: https://makersuite.google.com/app/apikey");
        System.out.println("   • Create a new API key");
        System.out.println();
        System.out.println("2. 📁 Create configuration file:");
        System.out.println("   • Create: src/main/resources/.env");
        System.out.println("   • Add: GOOGLE_API_KEY=your_actual_api_key");
        System.out.println();
        System.out.println("3. ▶️ Build and run:");
        System.out.println("   • mvn clean package");
        System.out.println("   • java -jar target/synapse-1.0-SNAPSHOT.jar");
        System.out.println();
        System.out.println("For more help, see: README.md or CONTRIBUTING.md");
        System.out.println();
    }
}