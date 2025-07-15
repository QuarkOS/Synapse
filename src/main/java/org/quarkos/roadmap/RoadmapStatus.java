package org.quarkos.roadmap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the current status of the Synapse roadmap.
 * This class aggregates information from the GitHub Project.
 */
public class RoadmapStatus {
    private final List<RoadmapItem> items;
    
    public RoadmapStatus() {
        this.items = new ArrayList<>();
    }
    
    public void addItem(RoadmapItem item) {
        items.add(item);
    }
    
    public List<RoadmapItem> getAllItems() {
        return new ArrayList<>(items);
    }
    
    public List<RoadmapItem> getFeatures() {
        return items.stream()
                .filter(RoadmapItem::isFeature)
                .collect(Collectors.toList());
    }
    
    public List<RoadmapItem> getBugs() {
        return items.stream()
                .filter(RoadmapItem::isBug)
                .collect(Collectors.toList());
    }
    
    public List<RoadmapItem> getRoadmapItems() {
        return items.stream()
                .filter(RoadmapItem::isRoadmapItem)
                .collect(Collectors.toList());
    }
    
    public List<RoadmapItem> getCompletedItems() {
        return items.stream()
                .filter(RoadmapItem::isCompleted)
                .collect(Collectors.toList());
    }
    
    public List<RoadmapItem> getInProgressItems() {
        return items.stream()
                .filter(RoadmapItem::isInProgress)
                .collect(Collectors.toList());
    }
    
    public List<RoadmapItem> getTodoItems() {
        return items.stream()
                .filter(item -> !item.isCompleted() && !item.isInProgress())
                .collect(Collectors.toList());
    }
    
    public List<RoadmapItem> getItemsByPriority(RoadmapItem.Priority priority) {
        return items.stream()
                .filter(item -> item.getPriority() == priority)
                .collect(Collectors.toList());
    }
    
    public int getTotalItems() {
        return items.size();
    }
    
    public int getCompletedCount() {
        return getCompletedItems().size();
    }
    
    public int getInProgressCount() {
        return getInProgressItems().size();
    }
    
    public int getTodoCount() {
        return getTodoItems().size();
    }
    
    public double getCompletionPercentage() {
        if (items.isEmpty()) return 0.0;
        return (double) getCompletedCount() / items.size() * 100.0;
    }
    
    /**
     * Generates a summary of the roadmap status for logging or display.
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Roadmap Status Summary:\n");
        summary.append(String.format("  Total Items: %d\n", getTotalItems()));
        summary.append(String.format("  Completed: %d (%.1f%%)\n", getCompletedCount(), getCompletionPercentage()));
        summary.append(String.format("  In Progress: %d\n", getInProgressCount()));
        summary.append(String.format("  To Do: %d\n", getTodoCount()));
        
        summary.append("\nBy Priority:\n");
        for (RoadmapItem.Priority priority : RoadmapItem.Priority.values()) {
            List<RoadmapItem> priorityItems = getItemsByPriority(priority);
            if (!priorityItems.isEmpty()) {
                summary.append(String.format("  %s: %d items\n", priority, priorityItems.size()));
            }
        }
        
        summary.append("\nBy Type:\n");
        summary.append(String.format("  Features: %d\n", getFeatures().size()));
        summary.append(String.format("  Bugs: %d\n", getBugs().size()));
        summary.append(String.format("  Roadmap Items: %d\n", getRoadmapItems().size()));
        
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}