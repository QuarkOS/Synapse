package org.quarkos.voice.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.quarkos.Model;
import org.quarkos.ai.Gemini;
import org.quarkos.spotify.SpotifyController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * A command parser that uses the Gemini AI to interpret audio commands.
 * It sends the audio data directly to the AI to extract song and artist information,
 * and then executes the playback command.
 */
public class GeminiCommandParser {

    private final SpotifyController spotifyController;
    private static final Logger logger = LoggerFactory.getLogger(GeminiCommandParser.class);
    private static final Gson gson = new Gson();

    public GeminiCommandParser(SpotifyController spotifyController) {
        this.spotifyController = spotifyController;
    }

    /**
     * Parses the user's spoken command by sending the raw audio data to the Gemini AI.
     *
     * @param audioData The raw byte array of the captured audio command.
     */
    public void parse(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            logger.warn("Audio data is empty, cannot parse command.");
            return;
        }

        try {
            logger.info("Step 4: Sending audio to Gemini AI for parsing...");
            // Use Gemini AI to extract song and artist from the audio data.
            Map.Entry<String, Long> response = Gemini.extractSongFromAudio(audioData, Model.GEMINI_2_5_FLASH_LITE_PREVIEW_06_17.getModelName());
            String jsonResponse = response.getKey();
            logger.info("Step 5: Received response from Gemini AI: {}", jsonResponse);

            // Parse the JSON response from the AI.
            logger.info("Step 6: Parsing JSON response...");
            JsonObject result = gson.fromJson(jsonResponse, JsonObject.class);
            String songName = result.has("songName") ? result.get("songName").getAsString() : null;
            String artist = result.has("artist") ? result.get("artist").getAsString() : null;
            logger.info("Parsed song: '{}', artist: '{}'", songName, artist);

            if (songName != null && !songName.isEmpty()) {
                // Execute the command to play the song.
                logger.info("Step 7: Executing play song command...");
                new PlaySongCommand(spotifyController, songName, artist).execute();
            } else {
                logger.warn("AI could not determine a song name from the audio.");
            }
        } catch (Exception e) {
            logger.error("Failed to parse audio command with Gemini AI", e);
        }
    }
}
