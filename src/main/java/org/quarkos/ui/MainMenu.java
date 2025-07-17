package org.quarkos.ui;

import org.quarkos.Model;
import org.quarkos.ai.Gemini;
import org.quarkos.example.VoiceControlledSpotifyExample;

import java.util.Map;
import java.util.Scanner;

public class MainMenu {

    private int menuSelection = 1;

    private static final String MENU_TEMPLATE = """
            Welcome to QuarkOS!
            ====================
            1. Start Application
            2. Settings
            3. Help
            4. Exit
            Please select an option (1-4):
            """;

    private static final String SETTINGS_MENU_TEMPLATE = """
            Settings
            ====================
            1. Select Model
            2. Back to Main Menu
            Please select an option (1-2):
            """;

    private static final String APPLICATION_OPTIONS_MENU_TEMPLATE = """
            Application Options
            ====================
            1. Spotify Voice Agent
            2. Copy & Answer
            3. Minimal Gemini Test
            4. Close Application
            5. Back to Main Menu
            Please select an option (1-5):
            """;

    public static void displayMainMenu() {
        System.out.print(MENU_TEMPLATE);
    }

    public void readUserInput() {
        Scanner scanner = new Scanner(System.in);
        int userInput = scanner.nextInt();

        switch (menuSelection) {
            case 3:
                switch (userInput) {
                    case 1 -> {
                        displayModelSelection();
                        readModelSelection();
                    }
                    case 2 -> {
                        displayMainMenu();
                        menuSelection = 1;
                        readUserInput();
                    }
                    default -> {
                        System.out.println("Invalid selection. Please try again.");
                        System.out.print(SETTINGS_MENU_TEMPLATE);
                        readUserInput();
                    }
                }
                break;
            case 2:
                switch (userInput) {
                    case 1 -> {
                        System.out.println("Starting Spotify Voice Agent...");
                        VoiceControlledSpotifyExample.main(new String[]{});
                    }
                    case 2 -> {
                        System.out.println("Starting Copy & Answer application...");
                    }
                    case 3 -> {
                        System.out.println("Enter your prompt for Gemini:");
                        Scanner promptScanner = new Scanner(System.in);
                        String prompt = promptScanner.nextLine();
                        Map.Entry<String, Long> result = Gemini.generateStructuredResponse(prompt);
                        System.out.println("Gemini Response: " + result.getKey());
                        System.out.println("Time taken: " + result.getValue() + "ms");
                        System.out.print(APPLICATION_OPTIONS_MENU_TEMPLATE);
                        readUserInput();
                    }
                    case 4 -> {
                        System.out.println("Closing application...");
                    }
                    case 5 -> {
                        displayMainMenu();
                        readUserInput();
                    }
                    default -> System.out.println("Invalid selection. Please try again.");
                }
                break;
            case 1:
                switch (userInput) {
                    case 1:
                        System.out.println("Starting Application Options...");
                        System.out.print(APPLICATION_OPTIONS_MENU_TEMPLATE);
                        menuSelection = 2;
                        readUserInput();
                        break;
                    case 2:
                        System.out.println("Opening Settings...");
                        System.out.print(SETTINGS_MENU_TEMPLATE);
                        menuSelection = 3;
                        readUserInput();
                        break;
                    case 3:
                        System.out.println("Displaying Help...");
                        break;
                    case 4:
                        System.out.println("Exiting application...");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid selection. Please try again.");
                        displayMainMenu();
                        readUserInput();
                        break;
                }
                break;
        }
    }

    private void displayModelSelection() {
        System.out.println("Select a Model:");
        System.out.println("===============");
        Model[] models = Model.values();
        for (int i = 0; i < models.length; i++) {
            System.out.printf("%n %d. %s", i + 1, models[i].getDisplayName());
            if (models[i] == Model.GEMINI_2_5_FLASH_LITE_PREVIEW_06_17) {
                System.out.print(" - RECOMMENDED");
            }
        }
        System.out.println((models.length + 1) + ". Back to Settings");
        System.out.print("Please select an option: ");
    }

    private void readModelSelection() {
        Scanner scanner = new Scanner(System.in);
        int userInput = scanner.nextInt();
        Model[] models = Model.values();

        if (userInput > 0 && userInput <= models.length) {
            Model selectedModel = models[userInput - 1];
            Gemini.currentModel = selectedModel;
            System.out.println("Model set to: " + selectedModel.getDisplayName());
        } else if (userInput == models.length + 1) {
        } else {
            System.out.println("Invalid selection.");
        }

        System.out.print(SETTINGS_MENU_TEMPLATE);
        menuSelection = 3;
        readUserInput();
    }

    public static void main(String[] args) {
        MainMenu mainMenu = new MainMenu();
        mainMenu.displayMainMenu();
        mainMenu.readUserInput();
    }

}
