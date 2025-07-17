# Synapse 🧠✨

![GitHub stars](https://img.shields.io/github/stars/QuarkOS/synapse?style=flat-square)
[![Project Roadmap](https://img.shields.io/badge/Project-Backlog-7B68EE?style=flat-square)](https://github.com/users/QuarkOS/projects/1/views/1)
![License](https://img.shields.io/github/license/QuarkOS/synapse?style=flat-square)

**Copy a question, paste an answer.**

Synapse redefines information retrieval by turning your standard clipboard into an instant answer engine. It's a lightweight background assistant, built with Java, that works with the one command you use constantly: **Copy**.

Just find a question anywhere on your screen, copy it, and Synapse instantly replaces it with a concise, AI-generated answer in your clipboard. No hotkeys to learn, no windows to switch—it feels like magic.



## The Magic Workflow

It's designed to be so seamless, it feels like a native OS feature:

1.  **Highlight** any question on your screen (in a browser, PDF, code editor, etc.).
2.  **Copy It** using the universal `Ctrl+C` (or `Cmd+C`) command.
3.  **Wait a Moment.** Synapse intelligently detects that you've copied a question and fetches the answer in the background.
4.  **Paste the Answer.** The AI-generated response is now in your clipboard, ready for you to paste with `Ctrl+V` (or `Cmd+V`).

## Key Features

*   ✨ **Truly Seamless Workflow:** No new hotkeys to learn! Synapse piggybacks on your natural `Ctrl+C` muscle memory.
*   🧠 **Intelligent Detection:** Automatically identifies if your copied text is a question to avoid interfering with your normal copy-paste actions.
*   ⚡ **Instant Answers:** Get AI-powered answers without ever leaving your current window or train of thought.
*   📋 **Direct Clipboard Replacement:** The answer replaces the question in your clipboard, eliminating all extra steps.
*   ☕ **Java Powered:** A robust and cross-platform core that runs on the JVM.
*   🤫 **Minimalist & Unobtrusive:** Runs quietly in the background. No UI, no pop-ups, no distractions.

## Getting Started

Follow these steps to get Synapse up and running on your machine.

### Prerequisites

*   **Java Development Kit (JDK) 17** or higher.
*   **Apache Maven** or **Gradle** to build the project.
*   An **API key** from your AI provider (e.g., Google Gemini).

### Installation & Setup

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/QuarkOS/synapse.git
    cd synapse
    ```

2.  **Configure your API key:**
    Create a configuration file named `.env` in the `src/main/resources` directory. 
    
    > [!NOTE]
    > Currently, Synapse is configured to use the Google Gemini API. Support for other providers like OpenAI is planned for a future release.

    Add your API key to this file:
    ```properties
    GOOGLE_API_KEY=your_api_key_here
    ```

3.  **Build the project:**
    This will compile the code and package it into a runnable `.jar` file.

    *   **Using Maven:**
        ```sh
        mvn clean package
        ```
    *   **Using Gradle:**
        ```sh
        ./gradlew build
        ```

### Usage

After building, you can run the application from your terminal.

1.  **Navigate to the executable JAR file.**
    *   For **Maven**, it will be in the `target/` directory.
    *   For **Gradle**, it will be in the `build/libs/` directory.

2.  **Run the application:**
    ```sh
    java -jar target/synapse-1.0-SNAPSHOT.jar
    ```

Synapse will now be running in the background. Simply copy a question from any application to see it in action! To stop the application, return to the terminal and press `Ctrl+C`.

## 🗺️ Roadmap

Want to see what's planned, what's in progress, or suggest a new feature? Check out our interactive project board!

👉 **[View the Project Roadmap](https://github.com/users/QuarkOS/projects/1/views/1)**

## Who is this for?

Synapse is built for anyone who values focus and speed. It's the ultimate productivity hack for **students**, **researchers**, **developers**, and curious minds who want answers without the friction.

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

We use our [GitHub Project Board](https://github.com/users/QuarkOS/projects/1/views/1) to track tasks and bugs. This is the best place to start if you're looking for something to work on.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.

