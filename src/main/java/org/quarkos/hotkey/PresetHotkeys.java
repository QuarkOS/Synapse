package org.quarkos.hotkey;

// import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

/**
 * Defines preset hotkey combinations for common application actions.
 * TODO: Implement this feature later
 */
public class PresetHotkeys {

    /*
    // This is an example of how a HotkeyCombination could be made:
    // public static final HotkeyCombination EXAMPLE_HOTKEY =
    //        HotkeyCombination.ctrlAltKey(NativeKeyEvent.VC_X); // CTRL+ALT+X
    // For Contribution, please define your own hotkeys in a similar manner.

    // Application control
    // TODO: Define hotkeys for application control

    // Clipboard management
    // TODO: Define hotkeys for clipboard management

    // Answer management
    // TODO: Define hotkeys for answer management
    */

    public static final HotkeyCombination ACTIVATE_SYNAPSE_HOTKEY =
            HotkeyCombination.ctrlAltShiftKey(NativeKeyEvent.VC_S);

    public static final HotkeyCombination SEND_CLIPBOARD_PROMPT_HOTKEY =
            HotkeyCombination.ctrlShiftKey(NativeKeyEvent.VC_C);

    public static final HotkeyCombination TOGGLE_DEBUG_MODE_HOTKEY =
            HotkeyCombination.ctrlShiftKey(NativeKeyEvent.VC_D);

    /**
     * Registers all preset hotkeys with the provided HotkeyManager and associated actions.
     *
     * @param manager The HotkeyManager to register the hotkeys with
     * @param actions Implementation of the PresetHotkeyActions interface that provides actions for each hotkey
     */
    public static void registerAll(HotkeyManager manager, PresetHotkeyActions actions) {
        // When hotkeys are defined, they would be registered here. For example:
        // manager.registerHotkey(EXAMPLE_HOTKEY, actions::onExampleHotkey);

        manager.registerHotkey(ACTIVATE_SYNAPSE_HOTKEY, actions::onActivateSynapse);
        manager.registerHotkey(SEND_CLIPBOARD_PROMPT_HOTKEY, actions::onSendClipboardPrompt);
        manager.registerHotkey(TOGGLE_DEBUG_MODE_HOTKEY, actions::onToggleDebugMode);
    }

    /**
     * Unregisters all preset hotkeys from the provided HotkeyManager.
     *
     * @param manager The HotkeyManager to unregister the hotkeys from
     */
    public static void unregisterAll(HotkeyManager manager) {
        // When hotkeys are defined, they would be unregistered here. For example:
        // manager.unregisterHotkey(EXAMPLE_HOTKEY);
        manager.unregisterHotkey(ACTIVATE_SYNAPSE_HOTKEY);
        manager.unregisterHotkey(SEND_CLIPBOARD_PROMPT_HOTKEY);
        manager.unregisterHotkey(TOGGLE_DEBUG_MODE_HOTKEY);
    }

    /**
     * Interface for actions to be performed when preset hotkeys are triggered.
     * TODO: Define methods in this interface corresponding to the hotkeys, when they are implemented.
     */
    public interface PresetHotkeyActions {
        // TODO: Define action methods here when hotkeys are implemented. For example:
        // void onExampleHotkey();

        void onActivateSynapse();
        void onSendClipboardPrompt();
        void onToggleDebugMode();
    }
}
