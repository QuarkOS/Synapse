package org.quarkos.roadmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single item in the Synapse roadmap.
 */
public class RoadmapItem {
    private final String title;
    private final String state;
    private final List<String> labels;
    
    public RoadmapItem(String title, String state) {
        this.title = title;
        this.state = state;
        this.labels = new ArrayList<>();
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getState() {
        return state;
    }
    
    public List<String> getLabels() {
        return new ArrayList<>(labels);
    }
    
    public void addLabel(String label) {
        if (!labels.contains(label)) {
            labels.add(label);
        }
    }
    
    public boolean hasLabel(String label) {
        return labels.contains(label);
    }
    
    public boolean isFeature() {
        return hasLabel("enhancement") || hasLabel("feature");
    }
    
    public boolean isBug() {
        return hasLabel("bug");
    }
    
    public boolean isRoadmapItem() {
        return hasLabel("roadmap");
    }
    
    public Priority getPriority() {
        if (hasLabel("critical")) return Priority.CRITICAL;
        if (hasLabel("high")) return Priority.HIGH;
        if (hasLabel("medium")) return Priority.MEDIUM;
        return Priority.LOW;
    }
    
    public boolean isCompleted() {
        return "CLOSED".equalsIgnoreCase(state);
    }
    
    public boolean isInProgress() {
        return hasLabel("in-progress") || hasLabel("in progress");
    }
    
    @Override
    public String toString() {
        return String.format("RoadmapItem{title='%s', state='%s', labels=%s}", title, state, labels);
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}