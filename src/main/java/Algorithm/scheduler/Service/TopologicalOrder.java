package Algorithm.scheduler.Service;

import Algorithm.scheduler.DataModel.TaskModel;
import java.util.List;
import java.util.Map;

public interface TopologicalOrder {
    
    Map<TaskModel, List<TaskModel>> topologicalSortAlgo(List<TaskModel> tasks);
}
