package Algorithm.scheduler.DataModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * This class allows the user to dynamically tune the scheduling algorithm.
 * It follows the "What-If" analysis requirement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulingPolicy {

    // Global multipliers to shift focus (e.g., focus more on deadlines than priority)
    @Builder.Default
    private double priorityMultiplier = 1.0;
    @Builder.Default
    private double deadlineMultiplier = 1.0;
    @Builder.Default
    private double dependencyMultiplier = 1.0;

    // Custom weight values for priority levels
    private Map<String, Integer> customPriorityWeights;

    // Heuristic preference: EARLIEST_DEADLINE, HIGHEST_PRIORITY, MINIMIZE_DEPENDENCY_CHAIN
    @Builder.Default
    private String optimizationGoal = "BALANCED";

    /**
     * Default policy values if user doesn't provide them.
     */
    public static SchedulingPolicy defaultPolicy() {
        return SchedulingPolicy.builder()
                .priorityMultiplier(1.0)
                .deadlineMultiplier(1.0)
                .dependencyMultiplier(1.0)
                .optimizationGoal("BALANCED")
                .build();
    }
}
