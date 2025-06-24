package org.quarkos.util;

public class TimerUtil {

    private static long startTime;
    private static long lapTime;

    public static void start() {
        startTime = System.currentTimeMillis();
        lapTime = startTime;
        System.out.println("Timer started.");
    }

    public static void lap(String message) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lapTime;
        System.out.println(message + " - Lap time: " + elapsed + "ms");
        lapTime = currentTime;
    }

    public static long stop() {
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Timer stopped. Total elapsed time: " + totalTime + "ms");
        return totalTime;
    }
}
