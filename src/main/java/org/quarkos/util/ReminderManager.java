package org.quarkos.util;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import org.quarkos.voice.AzureTextToSpeech;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderManager {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Parser nattyParser = new Parser();

    /**
     * Sets a reminder for a future time.
     *
     * @param reminderText The text of the reminder message.
     * @param timeString   The natural language string representing the time (e.g., "in 5 minutes", "tomorrow at 9am").
     */
    public static void setReminder(String reminderText, String timeString) {
        if (reminderText == null || reminderText.isBlank()) {
            AzureTextToSpeech.speak("What should I remind you about?");
            return;
        }
        if (timeString == null || timeString.isBlank()) {
            AzureTextToSpeech.speak("When should I remind you?");
            return;
        }

        List<DateGroup> groups = nattyParser.parse(timeString);
        if (groups.isEmpty()) {
            AzureTextToSpeech.speak("Sorry, I didn't understand the time for the reminder.");
            return;
        }

        Date reminderTime = groups.get(0).getDates().get(0);
        long delay = reminderTime.getTime() - System.currentTimeMillis();

        if (delay < 0) {
            AzureTextToSpeech.speak("I can't set a reminder in the past.");
            return;
        }

        scheduler.schedule(() -> {
            AzureTextToSpeech.speak("Here is your reminder: " + reminderText);
        }, delay, TimeUnit.MILLISECONDS);

        AzureTextToSpeech.speak("Okay, I will remind you to " + reminderText + " at " + reminderTime);
    }
}

