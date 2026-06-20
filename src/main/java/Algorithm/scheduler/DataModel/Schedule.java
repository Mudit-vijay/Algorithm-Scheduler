package Algorithm.scheduler.DataModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * this class is used to get the schedule of the person which is providing task for the schedule
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    private int startTime;
    private int endTime;
    private int totalDays;
    private int totalWeeks;
    private int totalHours;
}
