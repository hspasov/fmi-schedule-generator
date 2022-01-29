package intelligent.systems.fmi.schedule.generator;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Schedule {
    public static final List<DayOfWeek> SCHEDULED_DAYS_OF_WEEK = List.of(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    );
    public static final int START_HOUR = 8;
    public static final int END_HOUR = 21;

    private Map<Slot, SessionAllocation> sessionAllocations = new HashMap<>();

    public Schedule() {

    }


}
