package org.quarkos.hotkey;

import org.quarkos.ai.Gemini;
import org.quarkos.util.ClipboardUtil;

public class UniversalHotkeyLogic implements PresetHotkeys.PresetHotkeyActions {

    private boolean synapseActive = false;

    @Override
    public void onActivateSynapse() {
        this.synapseActive = !this.synapseActive;
        if (this.synapseActive) {
            System.out.println("Synapse activated. You can now use the hotkeys.");
        } else {
            System.out.println("Synapse deactivated. Hotkeys are now disabled.");
        }
    }

    @Override
    public void onSendClipboardPrompt() {
        System.out.println("Attempting to send clipboard content as prompt via hotkey...");
        String prompt = ClipboardUtil.getClipboardContent();
        if (prompt != null && !prompt.trim().isEmpty()) {
            try {
                System.out.println("Prompt from clipboard: \"" + prompt + "\"");
                String response = Gemini.generateStructuredResponse(prompt);
                System.out.println("Gemini Response: " + response);
                // Optionally, copy the response back to the clipboard or show a notification
                // ClipboardUtil.copyToClipboard(response); // Example
            } catch (Exception e) {
                System.err.println("Error generating response from clipboard content: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Clipboard is empty or contains no text.");
        }
    }

    // Add other hotkey action implementations here if you define more in PresetHotkeyActions
}
