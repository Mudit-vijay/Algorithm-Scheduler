package Algorithm.scheduler.Service.impl;

import Algorithm.scheduler.DataModel.Schedule;
import Algorithm.scheduler.DataModel.ScheduledTask;
import Algorithm.scheduler.DataModel.TaskModel;
import Algorithm.scheduler.DataModel.SchedulingPolicy;
import Algorithm.scheduler.Service.SchedulerAlgorithm;
import Algorithm.scheduler.Service.WeightageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("branchAndBoundScheduler")
public class BranchAndBoundScheduler implements SchedulerAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(BranchAndBoundScheduler.class);

    @Autowired
    private WeightageService weightageService;

    private List<ScheduledTask> bestSchedule;
    private int maxWeight;

    @Override
    public List<ScheduledTask> generateSchedule(List<TaskModel> tasks, Schedule constraints, SchedulingPolicy policy) {
        log.info("========== Branch & Bound Scheduling Started ==========");
        log.info("Total tasks: {} | Total hours constraint: {} | Policy: {}", tasks.size(), constraints.getTotalHours(), policy);

        this.bestSchedule = new ArrayList<>();
        this.maxWeight = -1;
        
        List<ScheduledTask> currentSchedule = new ArrayList<>();
        Set<String> completedTaskIds = new HashSet<>();

        long startTime = System.nanoTime();
        solve(tasks, constraints, policy, 0, currentSchedule, completedTaskIds, 0);
        long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;

        log.info("========== Branch & Bound Scheduling Completed ==========");
        log.info("Optimal weight found: {} | Tasks scheduled: {} / {} | Time taken: {} ms",
                maxWeight, bestSchedule.size(), tasks.size(), elapsedMs);
        bestSchedule.forEach(st -> log.info("  Scheduled: [{}] start={} end={}",
                st.getTask().getTaskId(), st.getStartTime(), st.getEndTime()));

        return bestSchedule;
    }

    private void solve(List<TaskModel> tasks, Schedule constraints, SchedulingPolicy policy, 
                       int currentTime, List<ScheduledTask> currentSchedule, Set<String> completedTaskIds, int currentWeight) {

        int depth = completedTaskIds.size();

        // --- Leaf node: all tasks scheduled ---
        if (depth == tasks.size()) {
            if (currentWeight > maxWeight) {
                log.debug(">>> NEW BEST solution found! weight={} (previous best={}), depth={}", currentWeight, maxWeight, depth);
                maxWeight = currentWeight;
                bestSchedule = new ArrayList<>(currentSchedule);
            }
            return;
        }

        List<TaskModel> availableTasks = getAvailableTasks(tasks, completedTaskIds);
        log.debug("Depth={} | currentTime={} | currentWeight={} | availableTasks={}",
                depth, currentTime, currentWeight, availableTasks.stream().map(TaskModel::getTaskId).collect(Collectors.toList()));

        // --- Bound calculation & pruning ---
        int remainingMax = calculateRemainingMaxWeight(availableTasks, policy);
        int potentialMaxWeight = currentWeight + remainingMax;

        if (potentialMaxWeight <= maxWeight) {
            log.debug("PRUNED at depth={} | potentialMax={} <= bestKnown={}", depth, potentialMaxWeight, maxWeight);
            return;
        }

        // Sort by dynamic weightage to explore high-value branches first
        availableTasks.sort((t1, t2) -> Integer.compare(
            calculateTaskWeight(t2, policy), 
            calculateTaskWeight(t1, policy)));

        for (TaskModel task : availableTasks) {
            int taskEndTime = currentTime + task.getEstimated_duration();

            // --- Feasibility check ---
            if (taskEndTime > task.getDeadline()) {
                log.debug("  SKIPPED task [{}] — misses deadline (endTime={} > deadline={})",
                        task.getTaskId(), taskEndTime, task.getDeadline());
                continue;
            }
            if (taskEndTime > constraints.getTotalHours()) {
                log.debug("  SKIPPED task [{}] — exceeds total hours (endTime={} > totalHours={})",
                        task.getTaskId(), taskEndTime, constraints.getTotalHours());
                continue;
            }

            int taskWeight = calculateTaskWeight(task, policy);
            log.debug("  BRANCHING on task [{}] | weight={} | time=[{} -> {}]",
                    task.getTaskId(), taskWeight, currentTime, taskEndTime);

            ScheduledTask scheduledTask = new ScheduledTask(task, currentTime, taskEndTime);
            currentSchedule.add(scheduledTask);
            completedTaskIds.add(task.getTaskId());

            solve(tasks, constraints, policy, taskEndTime, currentSchedule, completedTaskIds, currentWeight + taskWeight);

            // --- Backtrack ---
            completedTaskIds.remove(task.getTaskId());
            currentSchedule.remove(currentSchedule.size() - 1);
            log.debug("  BACKTRACKED from task [{}] at depth={}", task.getTaskId(), depth);
        }
    }

    private int calculateTaskWeight(TaskModel task, SchedulingPolicy policy) {
        int priorityW = weightageService.getPriorityWeight(task, policy);
        int deadlineW = weightageService.getDeadlineWeight(task, policy);
        int unlockedW = weightageService.getUnLockedTaskWeigt(task, policy);
        int total = priorityW + deadlineW + unlockedW;
        log.trace("Weight for [{}]: priority={} + deadline={} + unlocked={} = {}",
                task.getTaskId(), priorityW, deadlineW, unlockedW, total);
        return total;
    }

    private int calculateRemainingMaxWeight(List<TaskModel> remainingTasks, SchedulingPolicy policy) {
        int total = 0;
        for (TaskModel t : remainingTasks) {
            total += calculateTaskWeight(t, policy);
        }
        return total;
    }

    private List<TaskModel> getAvailableTasks(List<TaskModel> tasks, Set<String> completedTaskIds) {
        return tasks.stream()
                .filter(t -> !completedTaskIds.contains(t.getTaskId()))
                .filter(t -> isDependenciesMet(t, completedTaskIds))
                .collect(Collectors.toList());
    }

    private boolean isDependenciesMet(TaskModel task, Set<String> completedTaskIds) {
        List<TaskModel> dependencies = task.getTaskDependency();
        if (dependencies == null || dependencies.isEmpty()) {
            return true;
        }
        return dependencies.stream().allMatch(d -> completedTaskIds.contains(d.getTaskId()));
    }
}
