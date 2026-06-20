package Algorithm.scheduler.Service;

import Algorithm.scheduler.DataModel.TaskModel;
import Algorithm.scheduler.DataModel.Schedule;
import Algorithm.scheduler.DataModel.ScheduledTask;
import java.util.List;
import java.util.Queue;

public interface SchedulerManagerService {
    
    void scheduleTasks(List<TaskModel> tasks, Schedule schedule, Queue<TaskModel> schedule1);

    void checkDeadline(ScheduledTask task);
}
