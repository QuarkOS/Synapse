package org.quarkos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Utility class for clipboard operations with robust error handling.
 * Provides methods to safely read from and write to the system clipboard.
 */
public class ClipboardUtil {
    private static final Logger logger = LoggerFactory.getLogger(ClipboardUtil.class);
    private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    /**
     * Copies the specified text to the system clipboard.
     * 
     * @param text the text to copy to clipboard
     * @return true if the operation was successful, false otherwise
     */
    public static boolean copyToClipboard(String text) {
        if (text == null) {
            logger.warn("Attempted to copy null text to clipboard");
            return false;
        }
        
        try {
            StringSelection stringSelection = new StringSelection(text);
            clipboard.setContents(stringSelection, null);
            logger.debug("Successfully copied {} characters to clipboard", text.length());
            return true;
        } catch (IllegalStateException e) {
            logger.error("Clipboard is unavailable: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error copying to clipboard: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Retrieves the current text content from the system clipboard.
     * 
     * @return the clipboard content as a string, or null if unavailable or not text
     */
    public static String getClipboardContent() {
        try {
            if (!clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                logger.debug("Clipboard does not contain text data");
                return null;
            }
            
            String content = (String) clipboard.getData(DataFlavor.stringFlavor);
            logger.debug("Successfully retrieved {} characters from clipboard", 
                        content != null ? content.length() : 0);
            return content;
            
        } catch (UnsupportedFlavorException e) {
            logger.debug("Clipboard content is not text: {}", e.getMessage());
            return null;
        } catch (IOException e) {
            logger.warn("IO error reading clipboard: {}", e.getMessage());
            return null;
        } catch (IllegalStateException e) {
            logger.error("Clipboard is unavailable: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error reading clipboard: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Checks if the clipboard contains text data.
     * 
     * @return true if clipboard contains text, false otherwise
     */
    public static boolean hasTextContent() {
        try {
            return clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
        } catch (IllegalStateException e) {
            logger.warn("Cannot check clipboard availability: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error checking clipboard: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Safely retrieves clipboard content with a maximum length limit.
     * This prevents potential memory issues with very large clipboard content.
     * 
     * @param maxLength maximum number of characters to retrieve
     * @return the clipboard content truncated to maxLength, or null if unavailable
     */
    public static String getClipboardContent(int maxLength) {
        String content = getClipboardContent();
        if (content == null) {
            return null;
        }
        
        if (content.length() > maxLength) {
            logger.debug("Truncating clipboard content from {} to {} characters", 
                        content.length(), maxLength);
            return content.substring(0, maxLength);
        }
        
        return content;
    }
}
