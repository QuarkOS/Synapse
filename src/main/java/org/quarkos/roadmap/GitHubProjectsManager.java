package org.quarkos.roadmap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * GitHub Projects API integration for roadmap management.
 * This class provides utilities to interact with GitHub Projects v2 API
 * to manage roadmap items programmatically from within Synapse.
 */
public class GitHubProjectsManager {
    private static final Logger logger = LoggerFactory.getLogger(GitHubProjectsManager.class);
    private static final String GITHUB_GRAPHQL_API = "https://api.github.com/graphql";
    
    private final String githubToken;
    private final String projectOwner;
    private final int projectNumber;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    
    public GitHubProjectsManager(String githubToken, String projectOwner, int projectNumber) {
        this.githubToken = githubToken;
        this.projectOwner = projectOwner;
        this.projectNumber = projectNumber;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClients.createDefault();
    }
    
    /**
     * Creates a feature request issue that automatically gets added to the roadmap project.
     * This can be used by Synapse to automatically suggest new features based on user behavior.
     */
    public void createFeatureRequest(String title, String description, Priority priority, String userAgent) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("repositoryOwner", projectOwner);
            variables.put("repositoryName", "Synapse");
            variables.put("title", "[AUTO-GENERATED] " + title);
            variables.put("body", buildFeatureRequestBody(description, priority, userAgent));
            variables.put("labelIds", new String[]{"enhancement", "roadmap", "auto-generated"});
            
            String mutation = """
                mutation($repositoryOwner: String!, $repositoryName: String!, $title: String!, $body: String!, $labelIds: [String!]) {
                  createIssue(input: {
                    repositoryId: $repositoryId
                    title: $title
                    body: $body
                    labelIds: $labelIds
                  }) {
                    issue {
                      id
                      number
                      url
                    }
                  }
                }
                """;
            
            JsonNode response = executeGraphQLQuery(mutation, variables);
            if (response != null && response.has("data")) {
                JsonNode issue = response.get("data").get("createIssue").get("issue");
                logger.info("Created feature request: {} (#{}) at {}", 
                    title, issue.get("number").asInt(), issue.get("url").asText());
            }
            
        } catch (Exception e) {
            logger.error("Failed to create feature request: {}", title, e);
        }
    }
    
    /**
     * Reports a bug that gets automatically triaged and added to the project.
     */
    public void reportBug(String title, String description, Severity severity, String stackTrace, String systemInfo) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("repositoryOwner", projectOwner);
            variables.put("repositoryName", "Synapse");
            variables.put("title", "[AUTO-BUG] " + title);
            variables.put("body", buildBugReportBody(description, severity, stackTrace, systemInfo));
            variables.put("labelIds", new String[]{"bug", "auto-generated"});
            
            String mutation = """
                mutation($repositoryOwner: String!, $repositoryName: String!, $title: String!, $body: String!, $labelIds: [String!]) {
                  createIssue(input: {
                    repositoryId: $repositoryId
                    title: $title
                    body: $body
                    labelIds: $labelIds
                  }) {
                    issue {
                      id
                      number
                      url
                    }
                  }
                }
                """;
            
            JsonNode response = executeGraphQLQuery(mutation, variables);
            if (response != null && response.has("data")) {
                JsonNode issue = response.get("data").get("createIssue").get("issue");
                logger.info("Created bug report: {} (#{}) at {}", 
                    title, issue.get("number").asInt(), issue.get("url").asText());
            }
            
        } catch (Exception e) {
            logger.error("Failed to create bug report: {}", title, e);
        }
    }
    
    /**
     * Gets the current roadmap status from the GitHub Project.
     */
    public RoadmapStatus getRoadmapStatus() {
        try {
            String query = """
                query($owner: String!, $number: Int!) {
                  organization(login: $owner) {
                    projectV2(number: $number) {
                      id
                      title
                      items(first: 100) {
                        nodes {
                          id
                          content {
                            ... on Issue {
                              id
                              title
                              state
                              labels(first: 10) {
                                nodes {
                                  name
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """;
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("owner", projectOwner);
            variables.put("number", projectNumber);
            
            JsonNode response = executeGraphQLQuery(query, variables);
            if (response != null && response.has("data")) {
                return parseRoadmapStatus(response.get("data"));
            }
            
        } catch (Exception e) {
            logger.error("Failed to get roadmap status", e);
        }
        
        return new RoadmapStatus(); // Return empty status on error
    }
    
    private String buildFeatureRequestBody(String description, Priority priority, String userAgent) {
        StringBuilder body = new StringBuilder();
        body.append("## Auto-Generated Feature Request\n\n");
        body.append("**Generated by:** Synapse Application\n");
        body.append("**User Agent:** ").append(userAgent != null ? userAgent : "Unknown").append("\n");
        body.append("**Priority:** ").append(priority.toString()).append("\n");
        body.append("**Timestamp:** ").append(java.time.Instant.now()).append("\n\n");
        body.append("## Description\n\n");
        body.append(description).append("\n\n");
        body.append("## Additional Notes\n\n");
        body.append("This feature request was automatically generated based on application usage patterns. ");
        body.append("Please review and adjust priority/details as needed.\n\n");
        body.append("---\n");
        body.append("*This issue was created automatically by Synapse's roadmap integration.*");
        return body.toString();
    }
    
    private String buildBugReportBody(String description, Severity severity, String stackTrace, String systemInfo) {
        StringBuilder body = new StringBuilder();
        body.append("## Auto-Generated Bug Report\n\n");
        body.append("**Generated by:** Synapse Application\n");
        body.append("**Severity:** ").append(severity.toString()).append("\n");
        body.append("**Timestamp:** ").append(java.time.Instant.now()).append("\n\n");
        body.append("## Description\n\n");
        body.append(description).append("\n\n");
        
        if (systemInfo != null && !systemInfo.trim().isEmpty()) {
            body.append("## System Information\n\n");
            body.append("```\n").append(systemInfo).append("\n```\n\n");
        }
        
        if (stackTrace != null && !stackTrace.trim().isEmpty()) {
            body.append("## Stack Trace\n\n");
            body.append("```\n").append(stackTrace).append("\n```\n\n");
        }
        
        body.append("---\n");
        body.append("*This bug report was created automatically by Synapse's error reporting system.*");
        return body.toString();
    }
    
    private RoadmapStatus parseRoadmapStatus(JsonNode data) {
        RoadmapStatus status = new RoadmapStatus();
        
        JsonNode project = data.get("organization").get("projectV2");
        if (project != null && project.has("items")) {
            JsonNode items = project.get("items").get("nodes");
            
            for (JsonNode item : items) {
                if (item.has("content") && item.get("content").has("title")) {
                    JsonNode content = item.get("content");
                    String title = content.get("title").asText();
                    String state = content.get("state").asText();
                    
                    RoadmapItem roadmapItem = new RoadmapItem(title, state);
                    
                    // Parse labels
                    if (content.has("labels")) {
                        JsonNode labels = content.get("labels").get("nodes");
                        for (JsonNode label : labels) {
                            roadmapItem.addLabel(label.get("name").asText());
                        }
                    }
                    
                    status.addItem(roadmapItem);
                }
            }
        }
        
        return status;
    }
    
    private JsonNode executeGraphQLQuery(String query, Map<String, Object> variables) throws IOException {
        HttpPost post = new HttpPost(GITHUB_GRAPHQL_API);
        post.setHeader("Authorization", "Bearer " + githubToken);
        post.setHeader("Content-Type", "application/json");
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("query", query);
        payload.put("variables", variables);
        
        String jsonPayload = objectMapper.writeValueAsString(payload);
        post.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON, StandardCharsets.UTF_8.toString(), false));
        
        return httpClient.execute(post, response -> {
            if (response.getCode() == 200) {
                return objectMapper.readTree(response.getEntity().getContent());
            } else {
                logger.error("GitHub API request failed with status: {}", response.getCode());
                return null;
            }
        });
    }
    
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }
    
    public enum Priority {
        LOW("Low - Nice to have"),
        MEDIUM("Medium - Would improve user experience"), 
        HIGH("High - Important for core functionality"),
        CRITICAL("Critical - Blocking or security related");
        
        private final String description;
        
        Priority(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
    
    public enum Severity {
        LOW("Low - Minor inconvenience"),
        MEDIUM("Medium - Affects functionality but has workaround"),
        HIGH("High - Major functionality broken"),
        CRITICAL("Critical - Application unusable or security issue");
        
        private final String description;
        
        Severity(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
}