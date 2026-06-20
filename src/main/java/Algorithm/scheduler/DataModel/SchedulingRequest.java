package Algorithm.scheduler.DataModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingRequest {
    private List<TaskModel> tasks;
    private Schedule constraints;
    private SchedulingPolicy policy;
    private String algorithmType; // "backtracking" or "branchAndBound"
}
