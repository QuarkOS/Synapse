package org.quarkos.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ClipboardUtil utility.
 * Note: These tests interact with the system clipboard and may be disabled in headless environments.
 */
class ClipboardUtilTest {

    @BeforeEach
    void setUp() {
        // Clear clipboard or set to known state if possible
    }

    @Test
    @DisabledOnOs(value = OS.LINUX, disabledReason = "May fail in headless Linux environments")
    void testCopyToClipboard_ValidText() {
        String testText = "Test clipboard content";
        
        boolean result = ClipboardUtil.copyToClipboard(testText);
        assertTrue(result, "Copy operation should succeed");
        
        // Verify the content was actually copied
        String clipboardContent = ClipboardUtil.getClipboardContent();
        assertEquals(testText, clipboardContent, "Clipboard should contain the copied text");
    }

    @Test
    void testCopyToClipboard_NullText() {
        boolean result = ClipboardUtil.copyToClipboard(null);
        assertFalse(result, "Copy operation should fail for null text");
    }

    @Test
    @DisabledOnOs(value = OS.LINUX, disabledReason = "May fail in headless Linux environments")
    void testCopyToClipboard_EmptyText() {
        String emptyText = "";
        
        boolean result = ClipboardUtil.copyToClipboard(emptyText);
        assertTrue(result, "Copy operation should succeed for empty text");
        
        String clipboardContent = ClipboardUtil.getClipboardContent();
        assertEquals(emptyText, clipboardContent, "Clipboard should contain empty text");
    }

    @Test
    @DisabledOnOs(value = OS.LINUX, disabledReason = "May fail in headless Linux environments")
    void testGetClipboardContent_WithContent() {
        String testText = "Another test string";
        
        // First copy some content
        ClipboardUtil.copyToClipboard(testText);
        
        // Then retrieve it
        String content = ClipboardUtil.getClipboardContent();
        assertEquals(testText, content, "Retrieved content should match copied content");
    }

    @Test
    @DisabledOnOs(value = OS.LINUX, disabledReason = "May fail in headless Linux environments")
    void testHasTextContent() {
        // Copy some text first
        ClipboardUtil.copyToClipboard("Test content");
        
        boolean hasText = ClipboardUtil.hasTextContent();
        assertTrue(hasText, "Should detect text content in clipboard");
    }

    @Test
    @DisabledOnOs(value = OS.LINUX, disabledReason = "May fail in headless Linux environments")
    void testGetClipboardContent_WithMaxLength() {
        String longText = "This is a very long text that exceeds the maximum length limit";
        int maxLength = 20;
        
        ClipboardUtil.copyToClipboard(longText);
        
        String truncated = ClipboardUtil.getClipboardContent(maxLength);
        assertNotNull(truncated, "Should return truncated content");
        assertEquals(maxLength, truncated.length(), "Content should be truncated to max length");
        assertEquals(longText.substring(0, maxLength), truncated, 
                    "Truncated content should match beginning of original");
    }

    @Test
    @DisabledOnOs(value = OS.LINUX, disabledReason = "May fail in headless Linux environments")
    void testGetClipboardContent_WithMaxLength_ShorterContent() {
        String shortText = "Short";
        int maxLength = 20;
        
        ClipboardUtil.copyToClipboard(shortText);
        
        String content = ClipboardUtil.getClipboardContent(maxLength);
        assertEquals(shortText, content, "Should return full content when shorter than max length");
    }

    @Test
    @DisabledOnOs(value = OS.LINUX, disabledReason = "May fail in headless Linux environments")
    void testCopyAndRetrieve_LargeText() {
        // Test with larger text to ensure no issues with size
        StringBuilder largeText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeText.append("Line ").append(i).append(" of test content.\n");
        }
        
        String testText = largeText.toString();
        boolean copyResult = ClipboardUtil.copyToClipboard(testText);
        assertTrue(copyResult, "Should be able to copy large text");
        
        String retrieved = ClipboardUtil.getClipboardContent();
        assertEquals(testText, retrieved, "Large text should be copied and retrieved correctly");
    }

    @Test
    @DisabledOnOs(value = OS.LINUX, disabledReason = "May fail in headless Linux environments")
    void testCopyAndRetrieve_SpecialCharacters() {
        String specialText = "Test with special chars: 🧠✨ éñ中文 🚀";
        
        boolean copyResult = ClipboardUtil.copyToClipboard(specialText);
        assertTrue(copyResult, "Should be able to copy text with special characters");
        
        String retrieved = ClipboardUtil.getClipboardContent();
        assertEquals(specialText, retrieved, "Special characters should be preserved");
    }
}