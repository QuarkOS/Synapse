package org.quarkos.example;

import org.quarkos.hotkey.HotkeyManager;
import org.quarkos.hotkey.PresetHotkeys;

/**
 * Demonstration of using preset hotkeys for the Synapse application.
 * TODO: Implement this feature later
 */
public class SynapseHotkeyDemo {

    private static boolean debugMode = false;

    public static void main(String[] args) {
        try {
            HotkeyManager hotkeyManager = new HotkeyManager();

            // Register all preset hotkeys with our implementation of the actions
            PresetHotkeys.registerAll(hotkeyManager, new SynapseHotkeyActions());

            System.out.println("Synapse Hotkey Demo Initialized");
            System.out.println("====================================");
            System.out.println("The following hotkeys are available:");
            System.out.println("CTRL+SHIFT+S: Toggle Synapse activation");
            System.out.println("CTRL+ALT+C: Force answer generation for current clipboard content");
            System.out.println("CTRL+SHIFT+,: Show settings");
            System.out.println("CTRL+ALT+Q: Exit application");
            System.out.println("CTRL+SHIFT+H: Show clipboard history");
            System.out.println("CTRL+SHIFT+LEFT/RIGHT: Navigate clipboard history");
            System.out.println("CTRL+ALT+O: Copy original question");
            System.out.println("CTRL+ALT+R: Regenerate last answer");
            System.out.println("CTRL+ALT+D: Toggle between detailed and concise answers");
            System.out.println("CTRL+SHIFT+D: Toggle debug mode");
            System.out.println("====================================");
            System.out.println("Press CTRL+ALT+Q to exit");

            // Keep the application running
            while (true) {
                Thread.sleep(1000);
            }

        } catch (HotkeyManager.HotkeyException e) {
            System.err.println("Failed to initialize hotkey manager: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Application interrupted: " + e.getMessage());
        }
    }

    static class SynapseHotkeyActions implements PresetHotkeys.PresetHotkeyActions {
        private boolean synapseActive = true;

        @Override
        public void onActivateSynapse() {
            synapseActive = !synapseActive;
            if (debugMode) {
                System.out.println("Synapse is now " + (synapseActive ? "active" : "inactive"));
            }
        }

        @Override
        public void onSendClipboardPrompt() {
            if (debugMode) {
                System.out.println("Forcing answer generation for current clipboard content");
                System.out.println("(In a real implementation, this would process the current clipboard and generate an answer)");
            }
        }

        @Override
        public void onToggleDebugMode() {
            debugMode = !debugMode;
            System.out.println("Debug mode is now " + (debugMode ? "on" : "off"));
        }
    }
}
