package org.quarkos.roadmap;

import org.quarkos.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for integrating Synapse application with GitHub Projects roadmap.
 * This service allows the application to automatically contribute to roadmap planning
 * by reporting issues, suggesting features, and providing usage insights.
 */
public class RoadmapIntegrationService {
    private static final Logger logger = LoggerFactory.getLogger(RoadmapIntegrationService.class);
    
    private final GitHubProjectsManager projectsManager;
    private final ExecutorService executorService;
    private final boolean enabledInConfig;
    private RoadmapStatus lastKnownStatus;
    
    public RoadmapIntegrationService() {
        this.enabledInConfig = isRoadmapIntegrationEnabled();
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "roadmap-integration");
            t.setDaemon(true);
            return t;
        });
        
        if (enabledInConfig) {
            String githubToken = getGitHubToken();
            if (githubToken != null && !githubToken.trim().isEmpty()) {
                this.projectsManager = new GitHubProjectsManager(githubToken, "QuarkOS", 1);
                logger.info("Roadmap integration service initialized successfully");
            } else {
                this.projectsManager = null;
                logger.warn("GitHub token not configured, roadmap integration disabled");
            }
        } else {
            this.projectsManager = null;
            logger.info("Roadmap integration disabled in configuration");
        }
    }
    
    /**
     * Suggests a new feature based on user behavior or application insights.
     * This will create a feature request issue in the GitHub repository.
     */
    public CompletableFuture<Void> suggestFeature(String title, String description, 
                                                 GitHubProjectsManager.Priority priority) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                String userAgent = buildUserAgent();
                projectsManager.createFeatureRequest(title, description, priority, userAgent);
                logger.info("Feature suggestion submitted: {}", title);
            } catch (Exception e) {
                logger.error("Failed to suggest feature: {}", title, e);
            }
        }, executorService);
    }
    
    /**
     * Reports a bug that occurred in the application.
     * This will create a bug report issue in the GitHub repository.
     */
    public CompletableFuture<Void> reportBug(String title, String description, 
                                           GitHubProjectsManager.Severity severity, 
                                           Throwable exception) {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                String stackTrace = exception != null ? getStackTrace(exception) : null;
                String systemInfo = getSystemInfo();
                projectsManager.reportBug(title, description, severity, stackTrace, systemInfo);
                logger.info("Bug report submitted: {}", title);
            } catch (Exception e) {
                logger.error("Failed to report bug: {}", title, e);
            }
        }, executorService);
    }
    
    /**
     * Fetches the current roadmap status from GitHub Projects.
     */
    public CompletableFuture<RoadmapStatus> getRoadmapStatus() {
        if (!isEnabled()) {
            return CompletableFuture.completedFuture(new RoadmapStatus());
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                RoadmapStatus status = projectsManager.getRoadmapStatus();
                lastKnownStatus = status;
                logger.debug("Roadmap status updated: {} total items", status.getTotalItems());
                return status;
            } catch (Exception e) {
                logger.error("Failed to fetch roadmap status", e);
                return lastKnownStatus != null ? lastKnownStatus : new RoadmapStatus();
            }
        }, executorService);
    }
    
    /**
     * Gets the last known roadmap status (cached, no network call).
     */
    public RoadmapStatus getCachedRoadmapStatus() {
        return lastKnownStatus != null ? lastKnownStatus : new RoadmapStatus();
    }
    
    /**
     * Reports usage analytics that might inform roadmap decisions.
     * For example: which features are used most, what errors occur frequently, etc.
     */
    public void reportUsageInsight(String feature, String insight, String data) {
        if (!isEnabled()) {
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            logger.info("Usage insight for {}: {} (data: {})", feature, insight, data);
            // This could be enhanced to aggregate insights and periodically create
            // roadmap discussion issues with usage analytics
        }, executorService);
    }
    
    /**
     * Suggests improvements based on application performance or user experience.
     */
    public void suggestImprovement(String area, String suggestion, String justification) {
        if (!isEnabled()) {
            return;
        }
        
        suggestFeature(
            "Improvement: " + area,
            "**Suggestion:** " + suggestion + "\n\n**Justification:** " + justification,
            GitHubProjectsManager.Priority.MEDIUM
        );
    }
    
    public boolean isEnabled() {
        return enabledInConfig && projectsManager != null;
    }
    
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
        if (projectsManager != null) {
            try {
                projectsManager.close();
            } catch (IOException e) {
                logger.error("Error closing projects manager", e);
            }
        }
    }
    
    private boolean isRoadmapIntegrationEnabled() {
        // Check if roadmap integration is enabled in configuration
        // For now, default to enabled if GitHub token is available
        return getGitHubToken() != null;
    }
    
    private String getGitHubToken() {
        // Try to get GitHub token from environment or configuration
        String token = System.getenv("GITHUB_TOKEN");
        if (token == null || token.trim().isEmpty()) {
            token = System.getenv("PROJECT_TOKEN");
        }
        if (token == null || token.trim().isEmpty()) {
            // Could also check a config file here
            token = System.getProperty("github.token");
        }
        return token;
    }
    
    private String buildUserAgent() {
        String version = getClass().getPackage().getImplementationVersion();
        if (version == null) version = "dev";
        
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        
        return String.format("Synapse/%s (Java/%s; %s %s)", version, javaVersion, osName, osVersion);
    }
    
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) return null;
        
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getSimpleName()).append(": ").append(throwable.getMessage()).append("\n");
        
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("  at ").append(element.toString()).append("\n");
        }
        
        if (throwable.getCause() != null) {
            sb.append("Caused by: ").append(getStackTrace(throwable.getCause()));
        }
        
        return sb.toString();
    }
    
    private String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("Java Vendor: ").append(System.getProperty("java.vendor")).append("\n");
        info.append("OS Name: ").append(System.getProperty("os.name")).append("\n");
        info.append("OS Version: ").append(System.getProperty("os.version")).append("\n");
        info.append("OS Architecture: ").append(System.getProperty("os.arch")).append("\n");
        info.append("Available Processors: ").append(Runtime.getRuntime().availableProcessors()).append("\n");
        info.append("Max Memory: ").append(Runtime.getRuntime().maxMemory() / 1024 / 1024).append(" MB\n");
        info.append("Total Memory: ").append(Runtime.getRuntime().totalMemory() / 1024 / 1024).append(" MB\n");
        info.append("Free Memory: ").append(Runtime.getRuntime().freeMemory() / 1024 / 1024).append(" MB\n");
        
        // Add Synapse-specific info
        String version = getClass().getPackage().getImplementationVersion();
        if (version != null) {
            info.append("Synapse Version: ").append(version).append("\n");
        }
        
        return info.toString();
    }
}