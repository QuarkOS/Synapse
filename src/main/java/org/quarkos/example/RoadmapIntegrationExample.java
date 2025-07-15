package org.quarkos.example;

import org.quarkos.roadmap.RoadmapIntegrationService;
import org.quarkos.roadmap.GitHubProjectsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example demonstrating how to integrate GitHub Projects roadmap functionality 
 * into the Synapse application.
 */
public class RoadmapIntegrationExample {
    private static final Logger logger = LoggerFactory.getLogger(RoadmapIntegrationExample.class);
    
    public static void main(String[] args) {
        // Initialize the roadmap integration service
        RoadmapIntegrationService roadmapService = new RoadmapIntegrationService();
        
        if (!roadmapService.isEnabled()) {
            logger.info("Roadmap integration is disabled. Set GITHUB_TOKEN environment variable to enable.");
            return;
        }
        
        logger.info("Roadmap integration is enabled!");
        
        // Example 1: Suggest a feature based on usage analysis
        roadmapService.suggestFeature(
            "Enhanced Multi-Language Support", 
            "Users are frequently copying text in languages other than English. " +
            "Adding better language detection and context-aware AI responses would improve accuracy.",
            GitHubProjectsManager.Priority.MEDIUM
        );
        
        // Example 2: Report a hypothetical bug
        try {
            // Simulate an error condition
            throw new RuntimeException("Clipboard detection failed on Windows 11 with certain applications");
        } catch (Exception e) {
            roadmapService.reportBug(
                "Clipboard Detection Failure",
                "Failed to detect clipboard changes when copying from certain applications on Windows 11",
                GitHubProjectsManager.Severity.HIGH,
                e
            );
        }
        
        // Example 3: Get current roadmap status
        roadmapService.getRoadmapStatus().thenAccept(status -> {
            logger.info("Current roadmap status:\n{}", status.getSummary());
        });
        
        // Example 4: Report usage insights
        roadmapService.reportUsageInsight(
            "AI Provider Usage",
            "Gemini API usage increased 300% this month",
            "Consider optimizing Gemini-specific features and adding advanced configuration options"
        );
        
        // Example 5: Suggest an improvement
        roadmapService.suggestImprovement(
            "Response Time",
            "Implement response caching for frequently asked questions",
            "Users often ask similar questions repeatedly, caching could reduce API calls and improve response time"
        );
        
        // Wait a bit for async operations to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Shutdown the service
        roadmapService.shutdown();
        
        logger.info("Roadmap integration example completed!");
    }
}