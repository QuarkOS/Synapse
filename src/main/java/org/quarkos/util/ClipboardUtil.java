package org.quarkos.util;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

public class ClipboardUtil {
    private final static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text); // text as StringSelection
        clipboard.setContents(stringSelection, null);
    }

    public static String getClipboardContent() {
        try {
            return (String) clipboard.getData(DataFlavor.stringFlavor); // get clipboard content as String
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
