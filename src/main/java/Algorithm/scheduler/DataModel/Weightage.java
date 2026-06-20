package Algorithm.scheduler.DataModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * this class is used to calculate the overall weight of a list of TaskModels 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Weightage {
    private int priorityWeight;
    private int deadlineWeight;
    private int lockedTasksWeight;
    private int unlockedTasksWeight;

    public int getTotalWeight() {
        return priorityWeight + deadlineWeight + lockedTasksWeight + unlockedTasksWeight;
    }
}
