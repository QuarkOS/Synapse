package org.quarkos.hotkey;

import org.quarkos.ai.Gemini;
import org.quarkos.util.ClipboardUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class UniversalHotkeyLogic implements PresetHotkeys.PresetHotkeyActions {

    private static Logger logger = LoggerFactory.getLogger(UniversalHotkeyLogic.class);

    private boolean synapseActive = false;
    private boolean debugMode = false;

    @Override
    public void onActivateSynapse() {
        this.synapseActive = !this.synapseActive;
        if (this.synapseActive) {
            logger.info("Synapse activated. You can now use the hotkeys.");
        } else {
            logger.info("Synapse deactivated. Hotkeys are now disabled.");
        }
    }

    @Override
    public void onSendClipboardPrompt() {
        logger.debug("Attempting to send clipboard content as prompt via hotkey...");
        String prompt = ClipboardUtil.getClipboardContent();
        if (prompt != null && !prompt.trim().isEmpty()) {
            try {
                logger.debug("Prompt from clipboard: \"" + prompt + "\"");
                Map.Entry<String, Long> response = Gemini.generateStructuredResponse(prompt, Gemini.DEFAULT_MODEL);
                logger.debug("Gemini Response: " + response);
                // Optionally, copy the response back to the clipboard or show a notification
                // ClipboardUtil.copyToClipboard(response); // Example
            } catch (Exception e) {
                logger.error("Error generating response from clipboard content: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            logger.debug("Clipboard is empty or contains no text.");
        }
    }

    @Override
    public void onToggleDebugMode() {
        this.debugMode = !this.debugMode;
        if (this.debugMode) {
            logger.info("Debug mode activated. Additional logging will be displayed.");
        } else {
            logger.info("Debug mode deactivated. Returning to normal logging level.");
        }
    }

    // Add other hotkey action implementations here if you define more in PresetHotkeyActions
}
