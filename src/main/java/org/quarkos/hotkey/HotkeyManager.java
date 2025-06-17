package org.quarkos.hotkey;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A manager for global hotkeys using JNativeHook library.
 * This class allows registering callbacks for specific key combinations
 * that will trigger even when the application is not in focus.
 */
public class HotkeyManager implements NativeKeyListener {

    private static final Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
    private final Map<HotkeyCombination, Runnable> registeredHotkeys;
    private final Map<Integer, Boolean> pressedKeys;

    /**
     * Exception thrown when there's an issue with the hotkey functionality.
     */
    public static class HotkeyException extends Exception {
        public HotkeyException(String message, Throwable cause) {
            super(message, cause);
        }
        public HotkeyException(String message) {
            super(message);
        }
    }

    public HotkeyManager() throws HotkeyException {
        registeredHotkeys = new HashMap<>();
        pressedKeys = new HashMap<>();

        // Disable JNativeHook logging to avoid console spam
        logger.setLevel(Level.OFF);
        // Optionally, disable individual logger messages (more granular control)
        // Logger.getLogger(GlobalScreen.class.getPackage().getName()).setUseParentHandlers(false);


        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(e.getMessage());
            // e.printStackTrace(); // Consider logging this to a file or a more robust logging framework
            throw new HotkeyException("Failed to register native hook", e);
        }
        GlobalScreen.addNativeKeyListener(this);
    }

    public void registerHotkey(HotkeyCombination combination, Runnable action) {
        if (combination == null || action == null) {
            // Or throw an IllegalArgumentException
            System.err.println("HotkeyCombination or Runnable action cannot be null.");
            return;
        }
        registeredHotkeys.put(combination, action);
    }

    public void unregisterHotkey(HotkeyCombination combination) {
        if (combination == null) {
            return;
        }
        registeredHotkeys.remove(combination);
    }

    // TODO: Implement single key registration if needed
    public void registerSingleKey(int keyCode, Runnable action) {
        System.out.println("registerSingleKey is not yet implemented.");
    }

    public void unregisterSingleKey(int keyCode) {
        System.out.println("unregisterSingleKey is not yet implemented.");
    }

    public void shutdown() {
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            // Log this error appropriately
            System.err.println("There was a problem unregistering the native hook: " + e.getMessage());
            // e.printStackTrace();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        pressedKeys.put(e.getKeyCode(), true);
         System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
         printPressedKeys(); // For debugging

        for (Map.Entry<HotkeyCombination, Runnable> entry : registeredHotkeys.entrySet()) {
            if (entry.getKey().isPressed(pressedKeys)) {
                 System.out.println("Hotkey combination pressed: " + entry.getKey());
                entry.getValue().run(); // Execute the action
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        pressedKeys.put(e.getKeyCode(), false);
        // Or pressedKeys.remove(e.getKeyCode()); if you prefer to only store currently pressed keys
         System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
         printPressedKeys(); // For debugging
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Not used for hotkey combinations
    }

    // Optional: For debugging purposes

    private void printPressedKeys() {
        System.out.print("Currently pressed: ");
        for (Map.Entry<Integer, Boolean> entry : pressedKeys.entrySet()) {
            if (entry.getValue()) {
                System.out.print(NativeKeyEvent.getKeyText(entry.getKey()) + " ");
            }
        }
        System.out.println();
    }

}
