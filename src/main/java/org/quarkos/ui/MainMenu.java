package org.quarkos.ui;

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

    private static final String APPLICATION_OPTIONS_MENU_TEMPLATE = """
            Application Options
            ====================
            1. Spotify Voice Agent
            2. Copy & Answer
            3. Close Application
            4. Back to Main Menu
            Please select an option (1-4):
            """;

    public static void displayMainMenu() {
        System.out.print(MENU_TEMPLATE);
    }

    public void readUserInput() {
        Scanner scanner = new Scanner(System.in);
        int userInput = scanner.nextInt();

        switch (menuSelection) {
            case 2:
                switch (userInput) {
                    case 1 -> {
                        System.out.println("Starting Spotify Voice Agent...");
                        // Here you would start the Spotify Voice Agent application
                    }
                    case 2 -> {
                        System.out.println("Starting Copy & Answer application...");
                        // Here you would start the Copy & Answer application
                    }
                    case 3 -> {
                        System.out.println("Closing application...");
                        // Here you would close the application
                    }
                    case 4 -> {
                        displayMainMenu();
                        readUserInput(); // Read again for main menu options
                    }
                    default -> System.out.println("Invalid selection. Please try again.");
                }
                break;
            case 1:
                switch (userInput) {
                    case 1:
                        System.out.println("Starting Application Options...");
                        System.out.print(APPLICATION_OPTIONS_MENU_TEMPLATE);
                        menuSelection = 2; // Switch to application options menu
                        readUserInput(); // Read again for application options
                        break;
                    case 2:
                        System.out.println("Opening Settings...");
                        // Here you would open the settings menu
                        break;
                    case 3:
                        System.out.println("Displaying Help...");
                        // Here you would display help information
                        break;
                    case 4:
                        System.out.println("Exiting application...");
                        System.exit(0); // Exit the application
                        break;
                    default:
                        System.out.println("Invalid selection. Please try again.");
                        displayMainMenu(); // Redisplay the main menu
                        readUserInput(); // Read again for main menu options
                        break;
                }
                break;
        }
    }

    public static void main(String[] args) {
        MainMenu mainMenu = new MainMenu();
        mainMenu.displayMainMenu();
        mainMenu.readUserInput(); // Start reading user input
    }

}
