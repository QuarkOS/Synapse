package org.quarkos.voice;

import com.microsoft.cognitiveservices.speech.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class AzureTextToSpeech {

    private static final Logger logger = LoggerFactory.getLogger(AzureTextToSpeech.class);

    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    private static final String AZURE_SUBSCRIPTION_KEY = dotenv.get("AZURE_SUBSCRIPTION_KEY");
    private static final String AZURE_REGION = dotenv.get("AZURE_REGION");

    public static void speak(String textToSpeak) {
        byte[] audioData = generateAudioBytes(textToSpeak);
        if (audioData != null && audioData.length > 0) {
            playWavBytes(audioData);
        } else {
            logger.error("Audio synthesis failed or produced no data. Cannot play sound.");
        }
    }

    private static byte[] generateAudioBytes(String textToSpeak) {
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(AZURE_SUBSCRIPTION_KEY, AZURE_REGION);

        speechConfig.setSpeechSynthesisVoiceName("en-US-NovaTurboMultilingualNeural");

        try (SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, null)) {

            logger.info("Synthesizing text with voice 'en-US-NovaTurboMultilingualNeural': '{}'", textToSpeak);
            SpeechSynthesisResult result = synthesizer.SpeakTextAsync(textToSpeak).get();

            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                logger.info("Synthesis completed successfully.");
                return result.getAudioData();
            } else if (result.getReason() == ResultReason.Canceled) {
                SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(result);
                logger.error("Speech synthesis canceled. Reason: {}", cancellation.getReason());
                if (cancellation.getReason() == CancellationReason.Error) {
                    logger.error("Error Code: {}. Details: {}", cancellation.getErrorCode(), cancellation.getErrorDetails());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("An error occurred during speech synthesis.", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    private static void playWavBytes(byte[] audioData) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(byteArrayInputStream);
             Clip clip = AudioSystem.getClip()) {

            clip.open(audioInputStream);
            logger.info("Starting playback...");
            clip.start();
            clip.drain();
            logger.info("Playback finished.");

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            logger.error("Error during audio playback", e);
        }
    }
}