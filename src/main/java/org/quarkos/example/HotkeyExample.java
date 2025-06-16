package org.quarkos.example;

/**
 * Example class demonstrating the use of the HotkeyManager.
 * TODO: Implement this feature later
 */
public class HotkeyExample {

    /*
    public static void main(String[] args) {
        try {
            HotkeyManager hotkeyManager = new HotkeyManager();

            // Register CTRL+SPACE to print a message
            hotkeyManager.registerHotkey(
                HotkeyCombination.ctrlKey(NativeKeyEvent.VC_SPACE),
                () -> System.out.println(\"CTRL+SPACE was pressed!\")
            );

            // Register CTRL+ALT+G to trigger Gemini text generation
            hotkeyManager.registerHotkey(
                HotkeyCombination.ctrlAltKey(NativeKeyEvent.VC_G),
                () -> System.out.println(\"Gemini text generation triggered!\")
            );

            // Register CTRL+ALT+I to trigger Gemini image generation
            hotkeyManager.registerHotkey(
                HotkeyCombination.ctrlAltKey(NativeKeyEvent.VC_I),
                () -> System.out.println(\"Gemini image generation triggered!\")
            );

            // Register ESC key to exit the application
            hotkeyManager.registerSingleKey(
                NativeKeyEvent.VC_ESCAPE,
                () -> {
                    System.out.println(\"ESC pressed. Shutting down...\");
                    hotkeyManager.shutdown();
                    System.exit(0);
                }
            );

            System.out.println(\"Hotkey manager initialized. Press CTRL+SPACE, CTRL+ALT+G, CTRL+ALT+I, or ESC.\");
            System.out.println(\"The hotkeys will work even when this window is not focused.\");

            // Keep the application running
            while (true) {
                Thread.sleep(1000);
            }

        } catch (HotkeyException e) {
            System.err.println(\"Failed to initialize hotkey manager: \" + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(\"Application interrupted: \" + e.getMessage());
        }
    }
    */
}
