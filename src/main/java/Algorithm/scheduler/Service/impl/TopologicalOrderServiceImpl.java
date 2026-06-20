package Algorithm.scheduler.Service.impl;

import Algorithm.scheduler.DataModel.TaskModel;
import Algorithm.scheduler.Service.TopologicalOrder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TopologicalOrderServiceImpl implements TopologicalOrder {

    @Override
    public Map<TaskModel, List<TaskModel>> topologicalSortAlgo(List<TaskModel> tasks) {
        // Return a map of Task -> its dependencies in topological order
        // This is useful for knowing the dependency graph
        Map<TaskModel, List<TaskModel>> graph = new HashMap<>();
        for (TaskModel task : tasks) {
            graph.put(task, task.getTaskDependency());
        }
        return graph;
    }

    /**
     * Helper to get a flat list of tasks in topological order
     */
    public List<TaskModel> getTopologicalOrder(List<TaskModel> tasks) {
        List<TaskModel> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (TaskModel task : tasks) {
            if (!visited.contains(task.getTaskId())) {
                if (dfs(task, visited, recursionStack, result)) {
                    throw new RuntimeException("Circular dependency detected in tasks!");
                }
            }
        }

        Collections.reverse(result);
        return result;
    }

    private boolean dfs(TaskModel task, Set<String> visited, Set<String> recursionStack, List<TaskModel> result) {
        visited.add(task.getTaskId());
        recursionStack.add(task.getTaskId());

        List<TaskModel> dependencies = task.getTaskDependency();
        if (dependencies != null) {
            for (TaskModel dependency : dependencies) {
                if (recursionStack.contains(dependency.getTaskId())) {
                    return true; // Cycle detected
                }
                if (!visited.contains(dependency.getTaskId())) {
                    if (dfs(dependency, visited, recursionStack, result)) {
                        return true;
                    }
                }
            }
        }

        recursionStack.remove(task.getTaskId());
        result.add(task);
        return false;
    }
}
