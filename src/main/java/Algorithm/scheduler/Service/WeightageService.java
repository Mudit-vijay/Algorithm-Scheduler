package Algorithm.scheduler.Service;

import Algorithm.scheduler.DataModel.TaskModel;
import Algorithm.scheduler.DataModel.Weightage;
import Algorithm.scheduler.DataModel.SchedulingPolicy;
import java.util.List;

public interface WeightageService {

    int getPriorityWeight(TaskModel task, SchedulingPolicy policy);
    int getDeadlineWeight(TaskModel task, SchedulingPolicy policy);
    int getLockedTaskWeigt(TaskModel task, SchedulingPolicy policy);
    int getUnLockedTaskWeigt(TaskModel task, SchedulingPolicy policy);
    Weightage getWeightage(List<TaskModel> tasks, SchedulingPolicy policy);
}
