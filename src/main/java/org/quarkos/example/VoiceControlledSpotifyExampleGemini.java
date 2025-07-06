package org.quarkos.example;

import org.quarkos.spotify.SpotifyAuthenticator;
import org.quarkos.spotify.SpotifyController;
import org.quarkos.voice.WhisperVoiceListener;
import org.quarkos.voice.command.GeminiCommandParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;

public class VoiceControlledSpotifyExampleGemini {
    private static final Logger logger = LoggerFactory.getLogger(VoiceControlledSpotifyExampleGemini.class);

    public static void main(String[] args) {
        logger.info("Starting voice-controlled Spotify example with Gemini...");

        // Authenticate with Spotify
        logger.info("Step 1: Authenticating with Spotify...");
        SpotifyAuthenticator authenticator = new SpotifyAuthenticator();
        authenticator.authenticate();
        SpotifyApi spotifyApi = authenticator.getSpotifyApi();
        logger.info("Spotify authentication successful.");

        // Initialize the components
        logger.info("Step 2: Initializing components...");
        SpotifyController spotifyController = new SpotifyController(spotifyApi);
        GeminiCommandParser geminiCommandParser = new GeminiCommandParser(spotifyController);
        WhisperVoiceListener voiceListener = new WhisperVoiceListener(geminiCommandParser);
        logger.info("Components initialized.");

        // Start listening for voice commands
        logger.info("Step 3: Starting voice listener...");
        voiceListener.startListening();

        // Add a shutdown hook to gracefully stop the listener
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered. Stopping voice listener.");
            voiceListener.stopListening();
        }));

        logger.info("Voice control is active. Say a command like \"Play Bohemian Rhapsody by Queen\".");
    }
}
