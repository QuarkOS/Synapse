package org.quarkos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Configuration constants.
 * Validates that configuration values are properly defined and reasonable.
 */
class ConfigurationTest {

    @Test
    void testPlayTriggersNotEmpty() {
        assertNotNull(Configuration.PLAY_TRIGGERS, "Play triggers should not be null");
        assertFalse(Configuration.PLAY_TRIGGERS.isEmpty(), "Play triggers should not be empty");
        assertTrue(Configuration.PLAY_TRIGGERS.contains("play"), "Should contain 'play' trigger");
    }

    @Test
    void testArtistSeparatorsNotEmpty() {
        assertNotNull(Configuration.ARTIST_SEPARATORS, "Artist separators should not be null");
        assertFalse(Configuration.ARTIST_SEPARATORS.isEmpty(), "Artist separators should not be empty");
        assertTrue(Configuration.ARTIST_SEPARATORS.contains("by"), "Should contain 'by' separator");
    }

    @Test
    void testJunkWordsNotEmpty() {
        assertNotNull(Configuration.JUNK_WORDS, "Junk words should not be null");
        assertFalse(Configuration.JUNK_WORDS.isEmpty(), "Junk words should not be empty");
    }

    @Test
    void testAudioConfiguration() {
        assertTrue(Configuration.SAMPLE_RATE > 0, "Sample rate should be positive");
        assertTrue(Configuration.AUDIO_BUFFER_SIZE > 0, "Audio buffer size should be positive");
        assertTrue(Configuration.AUDIO_FORMAT_CHANNELS > 0, "Audio format channels should be positive");
        assertTrue(Configuration.AUDIO_FORMAT_BITS > 0, "Audio format bits should be positive");
        
        // Test reasonable values
        assertEquals(16000, Configuration.SAMPLE_RATE, "Default sample rate should be 16000");
        assertEquals(1, Configuration.AUDIO_FORMAT_CHANNELS, "Should be mono audio");
        assertEquals(16, Configuration.AUDIO_FORMAT_BITS, "Should be 16-bit audio");
    }

    @Test
    void testVoiceListenerTimingConfiguration() {
        assertTrue(Configuration.SILENCE_DURATION_MS_TO_STOP > 0, 
                  "Silence duration should be positive");
        assertTrue(Configuration.MIN_COMMAND_DURATION_MS > 0, 
                  "Min command duration should be positive");
        assertTrue(Configuration.MAX_COMMAND_DURATION_MS > 0, 
                  "Max command duration should be positive");
        
        // Test logical constraints
        assertTrue(Configuration.MIN_COMMAND_DURATION_MS < Configuration.MAX_COMMAND_DURATION_MS,
                  "Min command duration should be less than max");
        
        assertTrue(Configuration.START_SPEAKING_THRESHOLD > 0,
                  "Start speaking threshold should be positive");
        assertTrue(Configuration.STOP_SPEAKING_THRESHOLD > 0,
                  "Stop speaking threshold should be positive");
    }

    @Test
    void testWhisperConfiguration() {
        assertNotNull(Configuration.WHISPER_MODEL, "Whisper model should not be null");
        assertNotNull(Configuration.WHISPER_DEVICE, "Whisper device should not be null");
        
        // Test valid model values
        String[] validModels = {"tiny", "base", "small", "medium", "large", "large-v2", "large-v3"};
        boolean isValidModel = false;
        for (String model : validModels) {
            if (model.equals(Configuration.WHISPER_MODEL)) {
                isValidModel = true;
                break;
            }
        }
        assertTrue(isValidModel, "Whisper model should be one of the valid options");
        
        // Test valid device values
        assertTrue(Configuration.WHISPER_DEVICE.equals("cpu") || 
                  Configuration.WHISPER_DEVICE.equals("cuda"),
                  "Whisper device should be 'cpu' or 'cuda'");
    }

    @Test
    void testConfigurationImmutability() {
        // Test that lists are not modifiable (if they should be immutable)
        assertThrows(UnsupportedOperationException.class, () -> {
            Configuration.PLAY_TRIGGERS.add("new_trigger");
        }, "Play triggers list should be immutable");
        
        assertThrows(UnsupportedOperationException.class, () -> {
            Configuration.ARTIST_SEPARATORS.add("new_separator");
        }, "Artist separators list should be immutable");
        
        assertThrows(UnsupportedOperationException.class, () -> {
            Configuration.JUNK_WORDS.add("new_junk");
        }, "Junk words list should be immutable");
    }
}