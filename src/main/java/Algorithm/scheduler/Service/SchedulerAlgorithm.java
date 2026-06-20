package Algorithm.scheduler.Service;

import Algorithm.scheduler.DataModel.TaskModel;
import Algorithm.scheduler.DataModel.ScheduledTask;
import Algorithm.scheduler.DataModel.Schedule;
import Algorithm.scheduler.DataModel.SchedulingPolicy;
import java.util.List;

public interface SchedulerAlgorithm {
    /**
     * Generates a schedule based on a list of tasks, global constraints, and user policy.
     * 
     * @param tasks The list of tasks to be scheduled.
     * @param constraints The global constraints (start time, total hours, etc.)
     * @param policy The user-defined prioritization policy.
     * @return A list of scheduled tasks with assigned start and end times.
     */
    List<ScheduledTask> generateSchedule(List<TaskModel> tasks, Schedule constraints, SchedulingPolicy policy);
}
