package Algorithm.scheduler.Controller;

import Algorithm.scheduler.DataModel.ScheduledTask;
import Algorithm.scheduler.DataModel.SchedulingRequest;
import Algorithm.scheduler.Service.SchedulerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scheduler")
public class SchedulerController {

    private static final Logger log = LoggerFactory.getLogger(SchedulerController.class);

    @Autowired
    private SchedulerConfigService schedulerService;

    /**
     * Endpoint to generate a task schedule.
     * Accessible via the API Gateway.
     */
    @PostMapping("/generate")
    public ResponseEntity<List<ScheduledTask>> generateSchedule(@RequestBody SchedulingRequest request) {
        log.info("Received request to generate schedule with algorithm: {}", request.getAlgorithmType());
        log.debug("Number of tasks received: {}", request.getTasks() != null ? request.getTasks().size() : 0);
        
        List<ScheduledTask> schedule = schedulerService.runScheduler(
                request.getTasks(),
                request.getConstraints(),
                request.getPolicy(),
                request.getAlgorithmType()
        );

        if (schedule == null || schedule.isEmpty()) {
            log.warn("Generated schedule is empty or null.");
            return ResponseEntity.noContent().build();
        }

        log.info("Schedule generated successfully with {} tasks.", schedule.size());
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("Health check endpoint hit.");
        return ResponseEntity.ok("Scheduler Service is up and running on port 9001!");
    }
}
