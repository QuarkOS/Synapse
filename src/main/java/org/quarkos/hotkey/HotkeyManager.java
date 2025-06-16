package org.quarkos.hotkey;

/**
 * A manager for global hotkeys using JNativeHook library.
 * This class allows registering callbacks for specific key combinations
 * that will trigger even when the application is not in focus.
 * TODO: Implement this feature later
 */
public class HotkeyManager /* implements NativeKeyListener */ {

    /**
     * Exception thrown when there\'s an issue with the hotkey functionality.
     * TODO: Implement this feature later
     */
    public static class HotkeyException extends Exception {
        /*
        public HotkeyException(String message, Throwable cause) {
            super(message, cause);
        }
        */
    }

    // TODO: Implement the following methods later
    public HotkeyManager() throws HotkeyException {}
    public void registerHotkey(HotkeyCombination combination, Runnable action) {}
    public void registerSingleKey(int keyCode, Runnable action) {}
    public void unregisterHotkey(HotkeyCombination combination) {}
    public void unregisterSingleKey(int keyCode) {}
    public void shutdown() {}
}
