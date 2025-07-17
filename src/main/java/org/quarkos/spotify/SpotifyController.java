package org.quarkos.spotify;

import com.google.gson.JsonArray;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 * This class is the main bridge to the Spotify Web API.
 * It handles finding a device to play on, searching for tracks, and initiating playback.
 * It uses a resilient search strategy to provide the best possible user experience.
 */
public class SpotifyController {

    private final SpotifyApi spotifyApi;
    private static final Logger logger = LoggerFactory.getLogger(SpotifyController.class);

    public SpotifyController(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * Finds an available Spotify device to play music on.
     * It prioritizes an already active device (e.g., a currently playing desktop or phone app)
     * to ensure playback happens where the user expects it.
     *
     * @return The ID of an available device, or null if no devices are found.
     */
    private String getAvailableDeviceId() {
        try {
            Device[] devices = spotifyApi.getUsersAvailableDevices().build().execute();
            if (devices == null || devices.length == 0) {
                logger.error("No Spotify devices found. Is Spotify open on any device?");
                return null;
            }

            // Find the device that is currently active.
            Optional<Device> activeDevice = Arrays.stream(devices)
                    .filter(Device::getIs_active)
                    .findFirst();

            if (activeDevice.isPresent()) {
                logger.info("Found active device: " + activeDevice.get().getName());
                return activeDevice.get().getId();
            }

            // If no device is active, fall back to the first device in the list.
            logger.info("No active device found. Using first available: " + devices[0].getName());
            return devices[0].getId();

        } catch (Exception e) {
            logger.error("Error fetching Spotify devices: " + e.getMessage());
            return null;
        }
    }

    /**
     * The core method for playing a song. It orchestrates finding a device,
     * searching for the track, and sending the play command.
     *
     * @param trackName The name of the track from the user's command.
     * @param artistName The name of the artist (can be null if not specified).
     */
    public void searchAndPlayTrack(String trackName, String artistName) {
        if (trackName == null || trackName.isBlank()) {
            logger.warn("Cannot search for an empty track name.");
            return;
        }

        // Find a device to play on. This is a prerequisite for playback.
        String deviceId = getAvailableDeviceId();
        if (deviceId == null) {
            logger.error("Playback failed: No available Spotify device found. Please open Spotify on a device.");
            return;
        }

        logger.info("Searching for track: '" + trackName + "' by artist: '" + (artistName != null ? artistName : "Any") + "'");

        Track foundTrack = null;

        // --- Search Strategy --- //

        // High-Precision Search.
        // If we have both a track and an artist, we use Spotify's field filters (track:"..." artist:"...")
        // for a very specific search. This is fast and usually returns the exact match.
        if (artistName != null && !artistName.isBlank()) {
            String specificQuery = "track:\"" + trackName + "\" artist:\"" + artistName + "\"";
            logger.info("   - Attempting high-precision search...");
            try {
                Track[] tracks = spotifyApi.searchTracks(specificQuery).limit(1).build().execute().getItems();
                if (tracks.length > 0) {
                    foundTrack = tracks[0];
                }
            } catch (Exception e) { /* Fails silently, the fallback will handle it */ }
        }

        // Smart Fallback Search.
        // This runs if the high-precision search fails or wasn't possible (e.g., no artist specified).
        // It performs a general search and then uses a Levenshtein distance algorithm
        // to find the best match from the results. This is more resilient to typos or slight mismatches.
        if (foundTrack == null) {
            String generalQuery = trackName + (artistName != null ? " " + artistName : "");
            logger.info("   - High-precision search failed. Using smart fallback search...");
            foundTrack = findBestMatch(generalQuery, trackName, artistName);
        }

        // --- Playback --- //

        // If a track was found by either strategy, play it.
        if (foundTrack != null) {
            logger.info("   - Found best match: " + foundTrack.getName() + " by " + foundTrack.getArtists()[0].getName());
            try {
                // The Spotify API requires the track's URI to be in a JSON array.
                JsonArray uris = new JsonArray();
                uris.add(foundTrack.getUri());

                // Send the command to the Spotify API to start playback on the selected device.
                spotifyApi.startResumeUsersPlayback()
                        .device_id(deviceId)
                        .uris(uris)
                        .build()
                        .execute();

                logger.info("Playback command sent successfully!");
            } catch (Exception e) {
                logger.error("Playback failed: Error sending playback command. Do you have the correct scopes?", e);
                logger.error("   (Required scope: user-modify-playback-state)");
            }
        } else {
            logger.warn("No song found for '" + trackName + "'. Please try again.");
        }
    }

    /**
     * Implements the "smart search" functionality.
     * It fetches a list of results for a general query and then scores each result
     * based on its similarity to the original user command.
     *
     * @param fullQuery The combined query string (e.g., "bohemian rhapsody queen") for the API.
     * @param originalTrackName The track name as spoken by the user.
     * @param originalArtistName The artist name as spoken by the user (can be null).
     * @return The best matching Track object, or null if no suitable match is found.
     */
    private Track findBestMatch(String fullQuery, String originalTrackName, String originalArtistName) {
        try {
            // Fetch a list of candidates to compare.
            Track[] tracks = spotifyApi.searchTracks(fullQuery).limit(10).build().execute().getItems();
            if (tracks == null || tracks.length == 0) {
                return null;
            }

            // Levenshtein distance is a string metric for measuring the difference between two sequences.
            // In simple terms, it's the number of edits (insertions, deletions, or substitutions)
            // required to change one word into the other. A lower score means a better match.
            LevenshteinDistance levenshtein = new LevenshteinDistance();

            // Find the track with the minimum "distance" (i.e., the best match) to the original query.
            return Arrays.stream(tracks)
                    .min(Comparator.comparingInt(track -> {
                        // Calculate the similarity of the track title from the API vs. the command.
                        int trackDistance = levenshtein.apply(originalTrackName.toLowerCase(), track.getName().toLowerCase());

                        // If an artist was specified, we factor in the artist's similarity as well.
                        // This helps differentiate between songs with the same title by different artists.
                        if (originalArtistName != null && track.getArtists().length > 0) {
                            int artistDistance = levenshtein.apply(originalArtistName.toLowerCase(), track.getArtists()[0].getName().toLowerCase());
                            // We give more weight to the track title match, as it's usually more important.
                            return (trackDistance * 2) + artistDistance;
                        }
                        return trackDistance;
                    }))
                    .orElse(null); // Return the best match, or null if the stream is empty.

        } catch (Exception e) {
            logger.error("Error during smart search: " + e.getMessage(), e);
            return null;
        }
    }

    public void pause() {
        try {
            String deviceId = getAvailableDeviceId();
            if (deviceId != null) {
                spotifyApi.pauseUsersPlayback().device_id(deviceId).build().execute();
                logger.info("Playback paused.");
            }
        } catch (Exception e) {
            logger.error("Failed to pause playback.", e);
        }
    }

    public void resume() {
        try {
            String deviceId = getAvailableDeviceId();
            if (deviceId != null) {
                spotifyApi.startResumeUsersPlayback().device_id(deviceId).build().execute();
                logger.info("Playback resumed.");
            }
        } catch (Exception e) {
            logger.error("Failed to resume playback.", e);
        }
    }

    public void skip() {
        try {
            String deviceId = getAvailableDeviceId();
            if (deviceId != null) {
                spotifyApi.skipUsersPlaybackToNextTrack().device_id(deviceId).build().execute();
                logger.info("Skipped to next track.");
            }
        } catch (Exception e) {
            logger.error("Failed to skip track.", e);
        }
    }

    public void previous() {
        try {
            String deviceId = getAvailableDeviceId();
            if (deviceId != null) {
                spotifyApi.skipUsersPlaybackToPreviousTrack().device_id(deviceId).build().execute();
                logger.info("Skipped to previous track.");
            }
        } catch (Exception e) {
            logger.error("Failed to skip to previous track.", e);
        }
    }
}