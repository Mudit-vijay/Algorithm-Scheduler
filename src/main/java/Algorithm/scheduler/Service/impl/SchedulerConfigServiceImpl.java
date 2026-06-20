package Algorithm.scheduler.Service.impl;

import Algorithm.scheduler.DataModel.Schedule;
import Algorithm.scheduler.DataModel.ScheduledTask;
import Algorithm.scheduler.DataModel.TaskModel;
import Algorithm.scheduler.DataModel.SchedulingPolicy;
import Algorithm.scheduler.Service.SchedulerAlgorithm;
import Algorithm.scheduler.Service.SchedulerConfigService;
import Algorithm.scheduler.Service.TopologicalOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchedulerConfigServiceImpl implements SchedulerConfigService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfigServiceImpl.class);

    @Autowired
    @Qualifier("backtrackingScheduler")
    private SchedulerAlgorithm backtrackingScheduler;

    @Autowired
    @Qualifier("branchAndBoundScheduler")
    private SchedulerAlgorithm branchAndBoundScheduler;

    @Autowired
    private TopologicalOrder topologicalOrderService;

    @Override
    public List<TaskModel> findLockedTasks(List<TaskModel> tasks) {
        return tasks.stream()
                .filter(t -> t.getTaskDependency() != null && !t.getTaskDependency().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskModel> findUnlockedTasks(List<TaskModel> tasks) {
        return tasks.stream()
                .filter(t -> t.getTaskDependency() == null || t.getTaskDependency().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskModel> getTopologicalOrder(List<TaskModel> tasks) {
        if (topologicalOrderService instanceof TopologicalOrderServiceImpl) {
            return ((TopologicalOrderServiceImpl) topologicalOrderService).getTopologicalOrder(tasks);
        }
        return tasks;
    }

    @Override
    public List<ScheduledTask> runScheduler(List<TaskModel> tasks, Schedule constraints, SchedulingPolicy policy, String algorithmType) {
        log.info("runScheduler called with algorithm: {}", algorithmType);
        // Use default policy if none provided
        SchedulingPolicy activePolicy = (policy != null) ? policy : SchedulingPolicy.defaultPolicy();
        
        SchedulerAlgorithm algorithm;
        if ("branchAndBound".equalsIgnoreCase(algorithmType)) {
            log.debug("Selected Branch and Bound algorithm.");
            algorithm = branchAndBoundScheduler;
        } else {
            log.debug("Selected Backtracking algorithm.");
            algorithm = backtrackingScheduler;
        }

        return algorithm.generateSchedule(tasks, constraints, activePolicy);
    }
}
