# Synapse 🧠✨

![GitHub stars](https://img.shields.io/github/stars/QuarkOS/synapse?style=flat-square)
[![Project Roadmap](https://img.shields.io/badge/Project-Roadmap-7B68EE?style=flat-square)](https://github.com/users/QuarkOS/projects/1/views/1)
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

## 🗺️ Interactive Roadmap

Our roadmap is now fully managed through **GitHub Projects** - no more static markdown files! 🎉

👉 **[View the Interactive Roadmap](https://github.com/users/QuarkOS/projects/1/views/1)**

### ✨ New Roadmap Features

- **🎯 Interactive Planning**: Drag-and-drop prioritization and real-time status updates
- **🤖 Automated Integration**: Issues and PRs automatically added and categorized
- **📊 Live Analytics**: Track progress, velocity, and completion rates
- **🚀 Smart Suggestions**: The app itself can suggest features based on usage patterns
- **💬 Community Collaboration**: Easy for anyone to propose features and join discussions

### 🎯 How to Contribute to the Roadmap

1. **📝 Suggest Features**: Use our [Feature Request template](https://github.com/QuarkOS/Synapse/issues/new?template=feature_request.yml)
2. **🐛 Report Bugs**: Use our [Bug Report template](https://github.com/QuarkOS/Synapse/issues/new?template=bug_report.yml)  
3. **💭 Join Planning**: Participate in [Roadmap Discussions](https://github.com/QuarkOS/Synapse/issues/new?template=roadmap_discussion.yml)
4. **📋 Pick Up Tasks**: Look for [help wanted](https://github.com/QuarkOS/Synapse/labels/help%20wanted) issues

All contributions are automatically integrated into our project board with proper categorization and priority assignment!

📚 **[Learn more about our GitHub Projects integration →](docs/GITHUB_PROJECTS_INTEGRATION.md)**

## Who is this for?

Synapse is built for anyone who values focus and speed. It's the ultimate productivity hack for **students**, **researchers**, **developers**, and curious minds who want answers without the friction.

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

We use our [GitHub Project Board](https://github.com/users/QuarkOS/projects/1/views/1) to track tasks and bugs. This is the best place to start if you're looking for something to work on.

### 🚀 Quick Start for Contributors

1. **Check the [Interactive Roadmap](https://github.com/users/QuarkOS/projects/1/views/1)** to see what's planned
2. **Use our issue templates** to ensure proper categorization:
   - [🚀 Feature Request](https://github.com/QuarkOS/Synapse/issues/new?template=feature_request.yml) - Automatically added to roadmap
   - [🐛 Bug Report](https://github.com/QuarkOS/Synapse/issues/new?template=bug_report.yml) - Auto-triaged by severity
   - [📋 Roadmap Discussion](https://github.com/QuarkOS/Synapse/issues/new?template=roadmap_discussion.yml) - Plan project direction
3. **Look for [help wanted](https://github.com/QuarkOS/Synapse/labels/help%20wanted) issues** on the project board

### 📋 Contribution Process

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.

