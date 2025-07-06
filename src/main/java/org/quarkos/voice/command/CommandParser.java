package org.quarkos.voice.command;

import org.quarkos.Configuration;
import org.quarkos.spotify.SpotifyController;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * FINAL intelligent command parser.
 * It understands both simple commands and multi-line transcription formats.
 * It finds triggers, strips junk words, separates the track from the artist,
 * and passes both pieces of information to the PlaySongCommand.
 */
public class CommandParser {

    private final SpotifyController spotifyController;

    public CommandParser(SpotifyController spotifyController) {
        this.spotifyController = spotifyController;
    }

    /**
     * Parses the user's spoken command, whether it's a simple string or
     * a multi-line transcription output.
     *
     * @param text The raw input from the user or transcription service.
     */
    public void parse(String text) {
        if (text == null || text.isBlank()) return;

        // First, try to extract a command from a transcription format.
        String commandToParse = extractCommandFromTranscription(text);

        // If no command was extracted, assume the original text is the command.
        // This maintains compatibility with simple, single-line commands.
        if (commandToParse.isEmpty()) {
            commandToParse = text;
        }

        String lowerCaseText = commandToParse.toLowerCase();
        Optional<String> foundTrigger = findFirstWord(lowerCaseText, Configuration.PLAY_TRIGGERS);

        if (foundTrigger.isPresent()) {
            String query = lowerCaseText.substring(lowerCaseText.indexOf(foundTrigger.get()) + foundTrigger.get().length()).trim();

            // Clean the query by removing all junk words
            for (String junk : Configuration.JUNK_WORDS) {
                query = query.replaceAll("\b" + junk + "\b", " ").trim();
            }
            query = query.replaceAll("\s+", " "); // Condense multiple spaces

            Optional<String> foundSeparator = findFirstWord(query, Configuration.ARTIST_SEPARATORS);
            String trackName;
            String artistName = null; // Default to null if no artist is found

            if (foundSeparator.isPresent()) {
                // If a separator like "by" or "von" was found, split the string
                int separatorIndex = query.indexOf(foundSeparator.get());
                trackName = query.substring(0, separatorIndex).trim();
                artistName = query.substring(separatorIndex + foundSeparator.get().length()).trim();
            } else {
                // If no separator, the whole query is the track name
                trackName = query;
            }

            // More robust punctuation cleaning
            trackName = trackName.replaceAll("^[.,?¿!¡\\s]+|[.,?¿!¡\\s]+$", "").trim();
            if (artistName != null) {
                artistName = artistName.replaceAll("^[.,?¿!¡\\s]+|[.,?¿!¡\\s]+$", "").trim();
            }

            if (!trackName.isEmpty()) {
                // This now correctly calls the constructor with two arguments.
                new PlaySongCommand(spotifyController, trackName, artistName).execute();
            } else {
                System.out.println("Could not extract a song title from the command: '" + text + "'");
            }
        } else {
            System.out.println("Unknown command: '" + commandToParse + "'");
        }
    }

    /**
     * Extracts the actual spoken text from a multi-line transcription output.
     * It looks for lines with timestamps like "[00:00.000 --> 00:02.000]"
     * and concatenates the text following them.
     *
     * @param rawInput The raw, potentially multi-line input from a transcription service.
     * @return The combined command string, or an empty string if no transcription lines are found.
     */
    private String extractCommandFromTranscription(String rawInput) {
        return Arrays.stream(rawInput.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.startsWith("Detecting language"))
                .filter(line -> !line.startsWith("Detected language"))
                .filter(line -> !line.matches("\\[\\d{2}:\\d{2}\\.\\d{3} --> \\d{2}:\\d{2}\\.\\d{3}\\]"))
                .collect(Collectors.joining(" "))
                .trim();
    }

    /**
     * Finds the first occurrence of any word from a given list in a text string.
     *
     * @param text The text to search within.
     * @param words The list of words to find.
     * @return An Optional containing the first found word, or empty if none are found.
     */
    private Optional<String> findFirstWord(String text, List<String> words) {
        return words.stream()
                .map(word -> {
                    Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b");
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) return new MatchResult(word, matcher.start());
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .min(Comparator.comparingInt(MatchResult::getIndex))
                .map(MatchResult::getWord);
    }

    private record MatchResult(String word, int index) {
        public String getWord() { return word; }
        public int getIndex() { return index; }
    }
}