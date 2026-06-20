package Algorithm.scheduler.Service;

import Algorithm.scheduler.DataModel.TaskModel;
import Algorithm.scheduler.DataModel.Schedule;
import Algorithm.scheduler.DataModel.ScheduledTask;
import Algorithm.scheduler.DataModel.SchedulingPolicy;
import java.util.List;

public interface SchedulerConfigService {
    
    List<TaskModel> findLockedTasks(List<TaskModel> tasks);

    List<TaskModel> findUnlockedTasks(List<TaskModel> tasks);

    List<TaskModel> getTopologicalOrder(List<TaskModel> tasks);

    /**
     * Run the scheduler with dynamic parameters.
     * 
     * @param tasks List of tasks
     * @param constraints Global time constraints
     * @param policy User prioritization policy (What-If analysis)
     * @param algorithmType "backtracking" or "branchAndBound"
     * @return Gantt-ready scheduled tasks
     */
    List<ScheduledTask> runScheduler(List<TaskModel> tasks, Schedule constraints, SchedulingPolicy policy, String algorithmType);
}