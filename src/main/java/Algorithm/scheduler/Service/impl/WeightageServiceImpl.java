package Algorithm.scheduler.Service.impl;

import Algorithm.scheduler.DataModel.TaskModel;
import Algorithm.scheduler.DataModel.Weightage;
import Algorithm.scheduler.DataModel.SchedulingPolicy;
import Algorithm.scheduler.Service.WeightageService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Service
public class WeightageServiceImpl implements WeightageService {

    @Override
    public int getPriorityWeight(TaskModel task, SchedulingPolicy policy) {
        if (task.getPriority() == null) return 0;
        
        String priorityType = task.getPriority().toLowerCase();
        
        // Use custom weights from policy if provided, otherwise use defaults
        int baseWeight = 0;
        Map<String, Integer> customWeights = policy.getCustomPriorityWeights();
        
        if (customWeights != null && customWeights.containsKey(priorityType)) {
            baseWeight = customWeights.get(priorityType);
        } else {
            switch (priorityType) {
                case "low": baseWeight = 5; break;
                case "medium": baseWeight = 10; break;
                case "high": baseWeight = 20; break;
                case "crucial": baseWeight = 30; break;
            }
        }
        
        return (int) (baseWeight * 5 * policy.getPriorityMultiplier());
    }

    @Override
    public int getDeadlineWeight(TaskModel task, SchedulingPolicy policy) {
        int duration = task.getEstimated_duration();
        int basePoints = 0;
        
        if (duration <= 3) basePoints = 30 * duration;
        else if (duration <= 7) basePoints = 20 * duration;
        else if (duration <= 13) basePoints = 10 * duration;
        else if (duration <= 24) basePoints = 5 * duration;
        
        return (int) (basePoints * policy.getDeadlineMultiplier());
    }

    @Override
    public int getLockedTaskWeigt(TaskModel task, SchedulingPolicy policy) {
        int baseWeight = 4;
        int points = CollectionUtils.isEmpty(task.getTaskDependency()) ? (baseWeight * 3) : (baseWeight * 7);
        return (int) (points * policy.getDependencyMultiplier());
    }

    @Override
    public int getUnLockedTaskWeigt(TaskModel task, SchedulingPolicy policy) {
        if (CollectionUtils.isEmpty(task.getTaskDependency())) {
            return (int) (50 * policy.getDependencyMultiplier());
        }
        return 0;
    }

    @Override
    public Weightage getWeightage(List<TaskModel> tasks, SchedulingPolicy policy) {
        Weightage weightage = new Weightage();
        int priorityPoints = 0;
        int deadlinePoints = 0;
        int lockedTaskPoints = 0;
        int unlockedTaskPoints = 0;

        for (TaskModel task : tasks) {
            priorityPoints += getPriorityWeight(task, policy);
            deadlinePoints += getDeadlineWeight(task, policy);
            lockedTaskPoints += getLockedTaskWeigt(task, policy);
            unlockedTaskPoints += getUnLockedTaskWeigt(task, policy);
        }

        weightage.setPriorityWeight(priorityPoints);
        weightage.setDeadlineWeight(deadlinePoints);
        weightage.setLockedTasksWeight(lockedTaskPoints);
        weightage.setUnlockedTasksWeight(unlockedTaskPoints);

        return weightage;
    }
}
