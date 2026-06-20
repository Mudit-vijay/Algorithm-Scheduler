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

@Service("backtrackingScheduler")
public class BacktrackingScheduler implements SchedulerAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(BacktrackingScheduler.class);

    @Autowired
    private WeightageService weightageService;

    @Override
    public List<ScheduledTask> generateSchedule(List<TaskModel> tasks, Schedule constraints, SchedulingPolicy policy) {
        log.info("========== Backtracking Scheduling Started ==========");
        log.info("Total tasks: {} | Total hours constraint: {} | Policy: {}", tasks.size(), constraints.getTotalHours(), policy);

        List<ScheduledTask> currentSchedule = new ArrayList<>();
        Set<String> completedTaskIds = new HashSet<>();
        
        long startTime = System.nanoTime();
        boolean found = solve(tasks, constraints, policy, 0, currentSchedule, completedTaskIds);
        long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;

        log.info("========== Backtracking Scheduling Completed ==========");
        if (found) {
            log.info("Valid schedule found! Tasks scheduled: {} / {} | Time taken: {} ms",
                    currentSchedule.size(), tasks.size(), elapsedMs);
            currentSchedule.forEach(st -> log.info("  Scheduled: [{}] start={} end={}",
                    st.getTask().getTaskId(), st.getStartTime(), st.getEndTime()));
            return currentSchedule;
        }
        
        log.warn("No valid schedule could be found! Time taken: {} ms", elapsedMs);
        return Collections.emptyList();
    }

    private boolean solve(List<TaskModel> tasks, Schedule constraints, SchedulingPolicy policy, 
                          int currentTime, List<ScheduledTask> currentSchedule, Set<String> completedTaskIds) {
        
        int depth = completedTaskIds.size();

        if (depth == tasks.size()) {
            log.debug(">>> VALID complete schedule found at depth={}", depth);
            return true;
        }

        List<TaskModel> availableTasks = getAvailableTasks(tasks, completedTaskIds);
        log.debug("Depth={} | currentTime={} | availableTasks={}",
                depth, currentTime, availableTasks.stream().map(TaskModel::getTaskId).collect(Collectors.toList()));
        
        // Dynamic Heuristic Sorting based on Policy
        sortAvailableTasks(availableTasks, policy);

        for (TaskModel task : availableTasks) {
            int taskEndTime = currentTime + task.getEstimated_duration();
            
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

            log.debug("  BRANCHING on task [{}] | time=[{} -> {}]", task.getTaskId(), currentTime, taskEndTime);

            ScheduledTask scheduledTask = new ScheduledTask(task, currentTime, taskEndTime);
            currentSchedule.add(scheduledTask);
            completedTaskIds.add(task.getTaskId());

            if (solve(tasks, constraints, policy, taskEndTime, currentSchedule, completedTaskIds)) {
                return true;
            }

            // Backtrack
            completedTaskIds.remove(task.getTaskId());
            currentSchedule.remove(currentSchedule.size() - 1);
            log.debug("  BACKTRACKED from task [{}] at depth={}", task.getTaskId(), depth);
        }

        return false;
    }

    private void sortAvailableTasks(List<TaskModel> availableTasks, SchedulingPolicy policy) {
        String goal = policy.getOptimizationGoal() != null ? policy.getOptimizationGoal() : "BALANCED";
        
        switch (goal.toUpperCase()) {
            case "DEADLINE_FIRST":
                availableTasks.sort(Comparator.comparingInt(TaskModel::getDeadline));
                break;
            case "PRIORITY_FIRST":
                availableTasks.sort((t1, t2) -> Integer.compare(
                    weightageService.getPriorityWeight(t2, policy), 
                    weightageService.getPriorityWeight(t1, policy)));
                break;
            case "BALANCED":
            default:
                // Sort by total weightage score
                availableTasks.sort((t1, t2) -> {
                    int w1 = weightageService.getPriorityWeight(t1, policy) + weightageService.getDeadlineWeight(t1, policy);
                    int w2 = weightageService.getPriorityWeight(t2, policy) + weightageService.getDeadlineWeight(t2, policy);
                    return Integer.compare(w2, w1);
                });
                break;
        }
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
