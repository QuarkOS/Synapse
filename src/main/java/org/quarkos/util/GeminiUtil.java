package org.quarkos.util;

import com.google.genai.types.FunctionCall;
import org.quarkos.spotify.SpotifyAuthenticator;
import org.quarkos.spotify.SpotifyController;
import org.quarkos.voice.AzureTextToSpeech;
import se.michaelthelin.spotify.SpotifyApi;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GeminiUtil {
    private static final SpotifyApi spotifyApi = SpotifyAuthenticator.getSpotifyApi();
    private static final SpotifyController spotifyController = new SpotifyController(spotifyApi);
    private static final AzureTextToSpeech tts = new AzureTextToSpeech();
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * Handles a function call from the Gemini model by dispatching it to the appropriate method.
     *
     * @param functionCall The FunctionCall object from the Gemini response.
     */
    public static void handleFunctionCall(FunctionCall functionCall) {
        String functionName = functionCall.name()
                .orElseThrow(() -> new IllegalArgumentException("Function call is missing a name."));
        Map<String, Object> args = functionCall.args().orElse(java.util.Collections.emptyMap());

        switch (functionName) {
            case "getWeather":
                String city = (String) args.get("city");
                getWeather(city);
                break;
            case "playMusic":
                String songName = (String) args.get("songName");
                String artist = (String) args.get("artist");
                playMusic(songName, artist);
                break;
            case "controlPlayback":
                String action = (String) args.get("action");
                controlPlayback(action);
                break;
            case "setTimer":
                String durationStr = (String) args.get("duration");
                String timerName = (String) args.get("timerName");
                setTimer(durationStr, timerName);
                break;
            case "setAlarm":
                String timeStr = (String) args.get("time");
                String label = (String) args.get("label");
                setAlarm(timeStr, label);
                break;
            case "getTime":
                String timezone = (String) args.get("timezone");
                System.out.println("Getting time for timezone: " + timezone);
                // Implement the actual logic to get time here
                break;
            default:
                System.err.println("Unknown function call: " + functionName);
                break;
        }
    }

    /**
     * Example function that could be called by the model.
     *
     * @param city The city to get the weather for.
     */
    private static void getWeather(String city) {
        System.out.println("Getting weather for " + city);
        // Implement the actual logic to get weather here
    }

    private static void playMusic(String songName, String artist) {
        if (songName == null || songName.isBlank()) {
            tts.speak("Please specify a song to play.");
            return;
        }
        String response = "Playing " + songName;
        if (artist != null && !artist.isBlank()) {
            response += " by " + artist;
        }
        response += " on Spotify.";
        tts.speak(response);
        spotifyController.searchAndPlayTrack(songName, artist);
    }

    private static void controlPlayback(String action) {
        if (action == null || action.isBlank()) {
            return;
        }
        tts.speak(action.substring(0, 1).toUpperCase() + action.substring(1) + "ing music.");
        switch (action.toLowerCase()) {
            case "pause":
                spotifyController.pause();
                break;
            case "resume":
                spotifyController.resume();
                break;
            case "skip":
                spotifyController.skip();
                break;
            case "previous":
                spotifyController.previous();
                break;
        }
    }

    private static void setTimer(String durationStr, String timerName) {
        // Basic parsing, needs a more robust implementation
        long seconds = 0;
        try {
            String[] parts = durationStr.split("\\s+");
            for (int i = 0; i < parts.length; i += 2) {
                int value = Integer.parseInt(parts[i]);
                String unit = parts[i+1].toLowerCase();
                if (unit.startsWith("minute")) {
                    seconds += value * 60;
                } else if (unit.startsWith("second")) {
                    seconds += value;
                } else if (unit.startsWith("hour")) {
                    seconds += value * 3600;
                }
            }
        } catch (Exception e) {
            tts.speak("Sorry, I didn't understand the timer duration.");
            return;
        }

        String timerLabel = timerName != null && !timerName.isBlank() ? timerName : "Timer";
        tts.speak("Setting a " + durationStr + " timer.");
        executorService.schedule(() -> tts.speak(timerLabel + " is up!"), seconds, TimeUnit.SECONDS);
    }

    private static void setAlarm(String timeStr, String label) {
        tts.speak("Sorry, setting alarms is not implemented yet.");
        // Implement alarm logic here, likely involving parsing timeStr and scheduling.
    }
}
