package org.quarkos.voice;

import org.quarkos.Configuration;
import org.quarkos.voice.command.CommandParser;
import org.quarkos.voice.command.GeminiCommandParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for actively listening for voice commands from the microphone.
 * It uses a simple but effective Voice Activity Detection (VAD) mechanism based on
 * Root Mean Square (RMS) audio levels to detect when a user starts and stops speaking.
 * It records audio, buffers it, and sends it for transcription on a separate thread.
 */
public class WhisperVoiceListener implements Runnable {

    private final Object commandParser; // Can be CommandParser or GeminiCommandParser
    private volatile boolean isListening = false;

    private ExecutorService transcriptionExecutor;
    private static final Logger logger = LoggerFactory.getLogger(WhisperVoiceListener.class);

    public WhisperVoiceListener(CommandParser commandParser) {
        this.commandParser = commandParser;
    }

    public WhisperVoiceListener(GeminiCommandParser commandParser) {
        this.commandParser = commandParser;
    }

    public void startListening() {
        if (isListening) {
            logger.info("Already listening.");
            return;
        }
        isListening = true;
        transcriptionExecutor = Executors.newSingleThreadExecutor();
        new Thread(this).start();
    }

    public void stopListening() {
        isListening = false;
        if (transcriptionExecutor != null && !transcriptionExecutor.isShutdown()) {
            logger.info("Shutting down transcription service...");
            transcriptionExecutor.shutdownNow();
            try {
                if (!transcriptionExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.error("Transcription service did not terminate in time.");
                }
            } catch (InterruptedException ie) {
                transcriptionExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * The main loop for the voice listener thread.
     * It continuously reads audio from the microphone, decides if it constitutes speech,
     * and captures it until a period of silence is detected.
     */
    @Override
    public void run() {
        // Set up the audio format for capturing from the microphone.
        AudioFormat format = new AudioFormat(Configuration.SAMPLE_RATE, Configuration.AUDIO_FORMAT_BITS, Configuration.AUDIO_FORMAT_CHANNELS, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            logger.error("Microphone line not supported.");
            return;
        }

        try (TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info)) {
            microphone.open(format);
            microphone.start();
            logger.info("Voice listener started. Waiting for command...");

            ByteArrayOutputStream commandAudioBuffer = new ByteArrayOutputStream();

            // This buffer holds a few seconds of audio *before* speech is detected.
            // This is crucial to not miss the beginning of a command (e.g., the word "Play").
            LinkedList<byte[]> preSpeechBuffer = new LinkedList<>();
            final int preSpeechBufferSize = (Configuration.SAMPLE_RATE * (Configuration.AUDIO_FORMAT_BITS / 8) * Configuration.PRE_SPEECH_BUFFER_MS) / (1000 * Configuration.AUDIO_BUFFER_SIZE);

            long commandStartTime = 0;
            long silenceStartTime = -1;
            boolean isSpeaking = false;

            while (isListening) {
                byte[] data = new byte[Configuration.AUDIO_BUFFER_SIZE];
                int bytesRead = microphone.read(data, 0, data.length);
                if (bytesRead <= 0) continue;

                // Calculate the volume of the current audio chunk.
                long rms = calculateRMS(data);

                if (isSpeaking) {
                    // If we are in "speaking" mode, append the audio to our command buffer.
                    commandAudioBuffer.write(data, 0, bytesRead);

                    // Check if the user has stopped talking.
                    if (rms < Configuration.STOP_SPEAKING_THRESHOLD) {
                        if (silenceStartTime == -1) silenceStartTime = System.currentTimeMillis();

                        // If silence persists for long enough, we consider the command finished.
                        if ((System.currentTimeMillis() - silenceStartTime) > Configuration.SILENCE_DURATION_MS_TO_STOP) {
                            processCapturedAudio(commandAudioBuffer.toByteArray(), format, System.currentTimeMillis() - commandStartTime);
                            isSpeaking = false;
                            commandAudioBuffer.reset();
                        }
                    } else {
                        // If the user starts speaking again, reset the silence timer.
                        silenceStartTime = -1;
                    }

                    // Also, stop recording if the command gets too long to prevent runaway recordings.
                    if ((System.currentTimeMillis() - commandStartTime) > Configuration.MAX_COMMAND_DURATION_MS) {
                        processCapturedAudio(commandAudioBuffer.toByteArray(), format, System.currentTimeMillis() - commandStartTime);
                        isSpeaking = false;
                        commandAudioBuffer.reset();
                    }
                } else {
                    // If we are not in "speaking" mode, we are just listening for a command to start.
                    // We continuously add audio to the pre-speech buffer.
                    preSpeechBuffer.add(data);
                    if (preSpeechBuffer.size() > preSpeechBufferSize) {
                        preSpeechBuffer.removeFirst(); // Keep the buffer at a fixed size.
                    }

                    // If the audio volume exceeds the start threshold, begin recording the command.
                    if (rms > Configuration.START_SPEAKING_THRESHOLD) {
                        logger.info("   (Speaking detected, recording...)");
                        isSpeaking = true;
                        commandStartTime = System.currentTimeMillis();
                        silenceStartTime = -1;

                        // Prepend the buffered audio to the start of the command.
                        for (byte[] preSpeechData : preSpeechBuffer) {
                            commandAudioBuffer.write(preSpeechData);
                        }
                        preSpeechBuffer.clear();
                    }
                }
            }
            microphone.stop();
            logger.info("Voice listener stopped.");
        } catch (Exception e) {
            logger.error("Exception in voice listener loop", e);
        } finally {
            if (isListening) {
                stopListening();
            }
        }
    }

    /**
     * Called when a command is considered complete (due to silence or timeout).
     * It checks if the captured audio is long enough to be a valid command (not just noise)
     * and then submits it for transcription in a separate thread.
     */
    private void processCapturedAudio(byte[] audioData, AudioFormat format, long commandDuration) {
        logger.info("   (Command duration: " + commandDuration + "ms)");

        // Avoid processing very short sounds, which are likely just background noise.
        if (commandDuration > Configuration.MIN_COMMAND_DURATION_MS) {
            // Submit the transcription task to a separate thread to keep the listener responsive.
            transcriptionExecutor.submit(() -> transcribeAndParse(audioData, format));
        } else {
            logger.info("   (Command too short, ignoring as noise)");
        }
        logger.info("\nVoice listener is ready for the next command...");
    }

    /**
     * This method runs on a background thread to prevent blocking the main voice listener.
     * It sends the captured audio to the WhisperTranscriber and, upon receiving the text,
     * passes it to the CommandParser.
     */
    private void transcribeAndParse(byte[] audioData, AudioFormat format) {
        if (audioData.length == 0) return;
        try {
            if (commandParser instanceof GeminiCommandParser) {
                // If using the AI parser, convert the raw PCM data to a valid WAV format in memory first.
                logger.info("Converting raw audio to WAV format for Gemini...");
                byte[] wavAudioData = WhisperTranscriber.createWavInMemory(audioData, format);
                logger.info("WAV data created ({} bytes), sending to Gemini parser.", wavAudioData.length);
                ((GeminiCommandParser) commandParser).parse(wavAudioData);
            } else if (commandParser instanceof CommandParser) {
                // If using the text-based parser, transcribe first.
                String transcript = WhisperTranscriber.transcribe(audioData, format);
                logger.info("TRANSCRIBE START\n" + transcript + "\nTRANSCRIBE END");

                if (transcript != null && !transcript.isBlank()) {
                    logger.info("\nUnderstood: \"" + transcript.trim() + "\"");
                    ((CommandParser) commandParser).parse(transcript);
                } else {
                    logger.warn("\nCould not understand audio.");
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process audio command", e);
        }
    }

    /**
     * Calculates the Root Mean Square (RMS) of an audio buffer.
     * This is a simple and effective way to measure the "loudness" or "energy" of the audio.
     * We use it as the basis for our Voice Activity Detection (VAD).
     * @param audioData The raw audio data (in PCM format).
     * @return The calculated RMS value.
     */
    private long calculateRMS(byte[] audioData) {
        long sum = 0;
        if (audioData.length == 0) return 0;
        for (int i = 0; i < audioData.length - 1; i += 2) {
            short sample = (short) (((audioData[i + 1] & 0xFF) << 8) | (audioData[i] & 0xFF));
            sum += sample * sample;
        }
        double mean = (double) sum / (audioData.length / 2.0);
        return (long) Math.sqrt(mean);
    }
}
