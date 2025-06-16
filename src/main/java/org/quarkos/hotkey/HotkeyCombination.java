package org.quarkos.hotkey;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import java.util.Arrays;
import java.util.Map;

/**
 * Represents a combination of keys that can be pressed together to trigger a hotkey action.
 * The combination can include modifier keys (CTRL, ALT, SHIFT) plus a main key.
 */
public class HotkeyCombination {
    private final int[] keyCodes;

    /**
     * Creates a new hotkey combination with the specified key codes.
     * @param keyCodes The key codes (from NativeKeyEvent) that make up this combination
     */
    public HotkeyCombination(int... keyCodes) {
        this.keyCodes = Arrays.copyOf(keyCodes, keyCodes.length);
        Arrays.sort(this.keyCodes); // Sort for consistent equals/hashCode
    }

    /**
     * Creates a hotkey combination with a main key and optional modifiers.
     * @param mainKey The main key code
     * @param modifiers Optional modifier key codes (CTRL, ALT, SHIFT, etc.)
     * @return A new HotkeyCombination
     */
    public static HotkeyCombination of(int mainKey, int... modifiers) {
        int[] keys = new int[modifiers.length + 1];
        System.arraycopy(modifiers, 0, keys, 0, modifiers.length);
        keys[modifiers.length] = mainKey;
        return new HotkeyCombination(keys);
    }

    /**
     * Checks if this hotkey combination is currently pressed based on the given pressed keys map.
     * @param pressedKeys Map of key codes to their pressed state
     * @return true if all keys in this combination are pressed, false otherwise
     */
    public boolean isPressed(Map<Integer, Boolean> pressedKeys) {
        for (int keyCode : keyCodes) {
            Boolean isPressed = pressedKeys.get(keyCode);
            if (isPressed == null || !isPressed) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convenience method to create a CTRL+key combination.
     * @param key The main key code
     * @return A new HotkeyCombination with CTRL and the specified key
     */
    public static HotkeyCombination ctrlKey(int key) {
        return of(key, NativeKeyEvent.VC_CONTROL);
    }

    /**
     * Convenience method to create an ALT+key combination.
     * @param key The main key code
     * @return A new HotkeyCombination with ALT and the specified key
     */
    public static HotkeyCombination altKey(int key) {
        return of(key, NativeKeyEvent.VC_ALT);
    }

    /**
     * Convenience method to create a SHIFT+key combination.
     * @param key The main key code
     * @return A new HotkeyCombination with SHIFT and the specified key
     */
    public static HotkeyCombination shiftKey(int key) {
        return of(key, NativeKeyEvent.VC_SHIFT);
    }

    /**
     * Convenience method to create a CTRL+ALT+key combination.
     * @param key The main key code
     * @return A new HotkeyCombination with CTRL, ALT and the specified key
     */
    public static HotkeyCombination ctrlAltKey(int key) {
        return of(key, NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_ALT);
    }

    /**
     * Convenience method to create a CTRL+SHIFT+key combination.
     * @param key The main key code
     * @return A new HotkeyCombination with CTRL, SHIFT and the specified key
     */
    public static HotkeyCombination ctrlShiftKey(int key) {
        return of(key, NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_SHIFT);
    }

    /**
     * Convenience method to create an ALT+SHIFT+key combination.
     * @param key The main key code
     * @return A new HotkeyCombination with ALT, SHIFT and the specified key
     */
    public static HotkeyCombination altShiftKey(int key) {
        return of(key, NativeKeyEvent.VC_ALT, NativeKeyEvent.VC_SHIFT);
    }

    /**
     * Convenience method to create a CTRL+ALT+SHIFT+key combination.
     * @param key The main key code
     * @return A new HotkeyCombination with CTRL, ALT, SHIFT and the specified key
     */
    public static HotkeyCombination ctrlAltShiftKey(int key) {
        return of(key, NativeKeyEvent.VC_CONTROL, NativeKeyEvent.VC_ALT, NativeKeyEvent.VC_SHIFT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HotkeyCombination that = (HotkeyCombination) o;
        return Arrays.equals(keyCodes, that.keyCodes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(keyCodes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("HotkeyCombination[");
        for (int i = 0; i < keyCodes.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(NativeKeyEvent.getKeyText(keyCodes[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}
