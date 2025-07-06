package org.quarkos.voice;

import org.quarkos.Configuration;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * A helper class to run the OpenAI Whisper CLI tool for transcription.
 *
 * This final version is both performant and robust.
 * 1. It creates a valid WAV file format entirely in-memory to be piped to ffmpeg.
 * 2. It consumes stdout and stderr streams concurrently to prevent deadlocks and
 *    correctly separates the transcript from error messages.
 */
public class WhisperTranscriber {

    public static String transcribe(byte[] rawAudioData, AudioFormat format) throws IOException, InterruptedException {
        // CRUCIAL FIX #1: Wrap the raw audio data in a WAV header in-memory.
        byte[] wavAudioData = createWavInMemory(rawAudioData, format);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "whisper",
                "-", // Read audio from standard input
                "--model", Configuration.WHISPER_MODEL,
                "--device", Configuration.WHISPER_DEVICE
        );

        Map<String, String> environment = processBuilder.environment();
        environment.put("Path", System.getenv("PATH"));
        environment.put("PYTHONIOENCODING", "utf-8");

        System.out.println("Executing command with in-memory WAV audio...");
        Process process = processBuilder.start();

        try (OutputStream stdin = process.getOutputStream()) {
            stdin.write(wavAudioData);
        }

        // CRUCIAL FIX #2: Consume stdout and stderr on separate threads to prevent blocking.
        StreamGobbler stdOutGobbler = new StreamGobbler(process.getInputStream());
        StreamGobbler stdErrGobbler = new StreamGobbler(process.getErrorStream());
        Future<?> stdOutFuture = Executors.newSingleThreadExecutor().submit(stdOutGobbler);
        Future<?> stdErrFuture = Executors.newSingleThreadExecutor().submit(stdErrGobbler);

        int exitCode = process.waitFor();

        // Wait for gobblers to finish reading streams
        try {
            stdOutFuture.get();
            stdErrFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String stdErr = stdErrGobbler.getContent();
        if (exitCode != 0) {
            System.err.println("Whisper process exited with code " + exitCode);
            System.err.println("Stderr: " + stdErr);
            // Return null or throw an exception if transcription failed
            return null;
        }

        // The real transcript is now cleanly separated in stdout
        String transcript = stdOutGobbler.getContent();

        // You may still want to log stderr for warnings, even on success
        if (!stdErr.isBlank()) {
            System.out.println("Whisper warnings (stderr): " + stdErr);
        }

        return transcript.trim();
    }

    /**
     * Wraps raw PCM audio data in a WAV header, creating a complete WAV file format in a byte array.
     */
    private static byte[] createWavInMemory(byte[] rawAudioData, AudioFormat format) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(rawAudioData), format, rawAudioData.length / format.getFrameSize())) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, baos);
            return baos.toByteArray();
        }
    }

    /**
     * A simple class to consume an InputStream on a separate thread.
     */
    private static class StreamGobbler implements Runnable {
        private final InputStream inputStream;
        private final StringBuilder content = new StringBuilder();

        public StreamGobbler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                // Stream might be closed when process terminates, this is often normal.
                System.err.println("Error reading stream: " + e.getMessage());
            }
        }

        public String getContent() {
            return content.toString().trim();
        }
    }
}