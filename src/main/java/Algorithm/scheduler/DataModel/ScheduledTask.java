package Algorithm.scheduler.DataModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced ScheduledTask to be "Gantt-Ready" for frontend visualization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledTask {
    
    // Core timing data
    private TaskModel task;
    private int startTime;
    private int endTime;

    // Frontend Visualization Data (Gantt Specific)
    private String id;        // taskId as string
    private String text;      // taskName
    private String color;     // Based on priority or status
    private int duration;     // endTime - startTime
    private List<String> links; // List of dependent task IDs for drawing arrows

    public ScheduledTask(TaskModel task, int startTime, int endTime) {
        this.task = task;
        this.startTime = startTime;
        this.endTime = endTime;
        
        // Auto-populate frontend fields
        this.id = String.valueOf(task.getTaskId());
        this.text = task.getName();
        this.duration = endTime - startTime;
        this.color = determineColor(task.getPriority());
        
        if (task.getTaskDependency() != null) {
            this.links = task.getTaskDependency().stream()
                    .map(TaskModel::getTaskId)
                    .collect(Collectors.toList());
        }
    }

    private String determineColor(String priority) {
        if (priority == null) return "#3db9d3"; // Default blue
        
        switch (priority.toLowerCase()) {
            case "crucial": return "#ff4d4d"; // Red
            case "high": return "#ffa64d";    // Orange
            case "medium": return "#ffff4d";  // Yellow
            case "low": return "#4dff4d";     // Green
            default: return "#3db9d3";        // Blue
        }
    }
}
