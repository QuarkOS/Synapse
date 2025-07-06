package org.quarkos.voice.command;

import org.quarkos.spotify.SpotifyController;

/**
 * Command to play a song. This is the correct version that accepts a separate
 * track and artist, as required by the SpotifyController.
 */
public class PlaySongCommand implements Command {

    private final SpotifyController spotifyController;
    private final String trackName;
    private final String artistName; // Artist can be null if not specified in the command

    /**
     * This constructor now correctly expects TWO string arguments.
     *
     * @param spotifyController The controller instance.
     * @param trackName The cleaned track name.
     * @param artistName The cleaned artist name (or null).
     */
    public PlaySongCommand(SpotifyController spotifyController, String trackName, String artistName) {
        this.spotifyController = spotifyController;
        this.trackName = trackName;
        this.artistName = artistName;
    }

    @Override
    public void execute() {
        // This passes both arguments to your working Spotify method.
        spotifyController.searchAndPlayTrack(this.trackName, this.artistName);
    }
}