package org.quarkos.util;

public class FileUtil {
    public String[] getFileNamesFromDirectory(String directoryPath) {
        java.io.File dir = new java.io.File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }
        return dir.list();
    }

    public String[] sortFileWithSpecificEndingFromStringArray(String[] fileNames, String ending) {
        if (fileNames.length == 0) {
            return new String[0];
        }
        String[] sortedFileNames = new String[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            if (fileNames[i].endsWith(ending)) {
                sortedFileNames[i] = fileNames[i];
            }
        }
        return sortedFileNames;
    }
}
