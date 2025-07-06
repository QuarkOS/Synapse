package org.quarkos;

import java.util.Arrays;
import java.util.List;

public class Configuration {

    // CommandParser settings
    public static final List<String> PLAY_TRIGGERS = Arrays.asList(
            "play", "spiele", "spiel", "search for", "find", "listen to", "please play"
    );
    public static final List<String> ARTIST_SEPARATORS = Arrays.asList(
            "by", "von", "from", "bei"
    );
    public static final List<String> JUNK_WORDS = Arrays.asList(
            "the song", "the track", "the artist", "a song", "song", "track", "artist",
            "can you", "could you", "please", "the trick"
    );

    // WhisperVoiceListener settings
    public static final int SAMPLE_RATE = 16000;
    public static final int AUDIO_BUFFER_SIZE = 1024;
    public static final int AUDIO_FORMAT_CHANNELS = 1;
    public static final int AUDIO_FORMAT_BITS = 16;
    public static final long SILENCE_DURATION_MS_TO_STOP = 1200;
    public static final long MIN_COMMAND_DURATION_MS = 400;
    public static final long MAX_COMMAND_DURATION_MS = 15000;
    public static final int PRE_SPEECH_BUFFER_MS = 500;
    public static final int START_SPEAKING_THRESHOLD = 600;
    public static final int STOP_SPEAKING_THRESHOLD = 450;

    // WhisperTranscriber settings
    public static final String WHISPER_MODEL = "tiny";
    public static final String WHISPER_DEVICE = "cpu"; // "cuda" for nvidia gpu
}

