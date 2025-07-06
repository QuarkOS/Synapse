package org.quarkos.example;

import org.quarkos.spotify.SpotifyAuthenticator;
import org.quarkos.spotify.SpotifyController;
import org.quarkos.voice.WhisperVoiceListener;
import org.quarkos.voice.command.CommandParser;

import java.util.Scanner;

/**
 * An example class demonstrating the complete flow from voice recognition
 * to Spotify playback control.
 *
 * How to Run:
 * 1. Ensure your .env file is set up with SPOTIFY_CLIENT_ID and SPOTIFY_CLIENT_SECRET.
 * 2. Ensure you have authenticated with Google Cloud CLI (`gcloud auth application-default login`).
 * 3. Run the main() method in this class.
 *
 * Expected Flow:
 * 1. The application will print a Spotify login URL. Open it in your browser and log in.
 * 2. After you log in, the application will detect the callback and complete authentication.
 * 3. The voice listener will start automatically.
 * 4. Speak a command like "Play Bohemian Rhapsody" into your microphone.
 * 5. The application will print the transcribed text and execute the command.
 * 6. To stop the application, type "exit" in the console where it's running and press Enter.
 */
public class VoiceControlledSpotifyExample {

    public static void main(String[] args) {
        System.out.println("--- Starting Synapse Voice Controller ---");

        // Step 1: Handle Spotify Authentication
        // The SpotifyAuthenticator handles the entire OAuth 2.0 flow.
        // The .authenticate() method is synchronous; it will block the main thread
        // until the user has successfully logged in via their browser.
        SpotifyAuthenticator authenticator = new SpotifyAuthenticator();
        authenticator.authenticate();

        System.out.println("\n✅ Spotify authentication successful!");

        // Step 2: Create the Spotify Controller
        // This object will be responsible for all interactions with the Spotify API,
        // such as searching for and playing tracks. It needs the authenticated API client.
        SpotifyController spotifyController = new SpotifyController(authenticator.getSpotifyApi());

        // Step 3: Create the Command Parser
        // The parser's job is to take raw text and convert it into a concrete Command object.
        // It needs the spotifyController to pass it to the commands it creates.
        CommandParser commandParser = new CommandParser(spotifyController);

        // Step 4: Create and Start the Voice Listener
        // The VoiceListener handles all microphone input and streams it to the
        // Google Cloud Speech-to-Text API. When it gets a result, it passes the
        // text to the commandParser.
        WhisperVoiceListener voiceListener = new WhisperVoiceListener(commandParser);
        voiceListener.startListening(); // This starts the listener on a new background thread.

        // Step 5: Keep the main application alive
        // Since the voice listener runs on a background thread, the main method would
        // exit immediately without this loop. We'll wait here for the user to type "exit".
        System.out.println("\n--- Application is running ---");
        System.out.println("Speak your commands or type 'exit' in this console to quit.");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    if ("exit".equalsIgnoreCase(input)) {
                        break; // Exit the loop if the user types "exit"
                    }
                }
            }
        }

        // Step 6: Gracefully shut down the application
        System.out.println("--- Shutting down ---");
        voiceListener.stopListening(); // Signal the voice listener to stop
        System.out.println("Application has been terminated.");
    }
}