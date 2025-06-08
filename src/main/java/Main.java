public class Main {
    public static void main(String[] args) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        Gemini gemini = new Gemini();

        while (true) {
            System.out.println("\\nMain Menu:");
            System.out.println("1. Generate Text Response");
            System.out.println("2. Generate Text Response with Image Data");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter your text prompt: ");
                    String textPrompt = scanner.nextLine();
                    try {
                        String response = Gemini.generateStructuredResponse(textPrompt);
                        System.out.println("Gemini Response: " + response);
                    } catch (Exception e) {
                        System.err.println("Error generating response: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case "2":
                    System.out.print("Enter your text prompt for image context: ");
                    String imagePrompt = scanner.nextLine();
                    try {
                        String response = Gemini.generateStructuredResponseWithImageData(imagePrompt);
                        System.out.println("Gemini Response with Image Data: " + response);
                    } catch (Exception e) {
                        System.err.println("Error generating response with image data: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case "3":
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}