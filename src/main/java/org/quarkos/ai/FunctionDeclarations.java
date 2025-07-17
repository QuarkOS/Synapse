package org.quarkos.ai;

import com.google.common.collect.ImmutableList;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;

import java.util.List;

/**
 * A centralized repository for all function declarations available to the Gemini model.
 */
public class FunctionDeclarations {

    private static final List<FunctionDeclaration> declarations = ImmutableList.of(
            FunctionDeclaration.builder()
                    .name("getWeather")
                    .description("gets the weather for a requested city")
                    .parameters(Schema.fromJson("""
                        { "type": "object", "properties": { "city": { "type": "string" } } }
                        """))
                    .build(),

            FunctionDeclaration.builder()
                    .name("playMusic")
                    .description("Plays a song on Spotify. Can specify artist to improve search.")
                    .parameters(Schema.fromJson("""
                        {
                          "type": "object",
                          "properties": {
                            "songName": { "type": "string" },
                            "artist": { "type": "string" }
                          }
                        }
                        """))
                    .build(),

            FunctionDeclaration.builder()
                    .name("controlPlayback")
                    .description("Controls music playback on Spotify.")
                    .parameters(Schema.fromJson("""
                        {
                          "type": "object",
                          "properties": {
                            "action": { "type": "string", "enum": ["pause", "resume", "skip", "previous"] }
                          }
                        }
                        """))
                    .build(),

            FunctionDeclaration.builder()
                    .name("setTimer")
                    .description("Sets a timer for a specified duration.")
                    .parameters(Schema.fromJson("""
                        {
                          "type": "object",
                          "properties": {
                            "duration": { "type": "string", "description": "e.g., '10 minutes', '1 hour 30 seconds'" },
                            "timerName": { "type": "string" }
                          }
                        }
                        """))
                    .build(),

            FunctionDeclaration.builder()
                    .name("setAlarm")
                    .description("Sets an alarm for a specific time.")
                    .parameters(Schema.fromJson("""
                        {
                          "type": "object",
                          "properties": {
                            "time": { "type": "string", "description": "e.g., '7 AM', 'tomorrow at 8:30 PM'" },
                            "label": { "type": "string" }
                          }
                        }
                        """))
                    .build(),

            FunctionDeclaration.builder()
                    .name("createReminder")
                    .description("Creates a reminder for the user at a specific time.")
                    .parameters(Schema.fromJson("""
                        {
                          "type": "object",
                          "properties": {
                            "reminderText": { "type": "string", "description": "The text of the reminder" },
                            "timeString": { "type": "string", "description": "The time for the reminder, e.g., 'in 10 minutes', 'at 8pm'" }
                          }
                        }
                        """))
                    .build()
    );

    /**
     * Returns a complete list of all defined function declarations.
     * This list can be directly passed to the Gemini model's tool configuration.
     *
     * @return An immutable list of {@link FunctionDeclaration} objects.
     */
    public static List<FunctionDeclaration> getDeclarations() {
        return declarations;
    }
}
