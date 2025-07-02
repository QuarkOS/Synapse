package org.quarkos.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.quarkos.hotkey.HotkeyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static String[] getFileNamesFromDirectory(String directoryPath) {
        File folder = new File(directoryPath);
        return folder.list();
    }

    public static byte[] readFileAsByteArray(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            logger.error("Error reading file " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    public static String extractTextFromPdf(String filePath) {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } catch (IOException e) {
            logger.error("Error extracting text from PDF " + filePath + ": " + e.getMessage());
        }
        return "";
    }
}