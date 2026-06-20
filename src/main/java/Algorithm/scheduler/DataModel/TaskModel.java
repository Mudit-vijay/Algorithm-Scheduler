package Algorithm.scheduler.DataModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * this class is used to store the information of a task
 * and the dependencies of a task 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskModel implements Cloneable {

    private String taskId;
    private String name;
    private String description;
    private String priority;
    private int estimated_duration;
    private List<TaskModel> taskDependency;
    private int userId;
    private String userName;
    private String groupId;
    private Boolean completed;
    private int deadline;

    @Override
    public TaskModel clone() {
        try {
            TaskModel clone = (TaskModel) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
