package intelligent.systems.fmi.schedule.generator.allocation;

import intelligent.systems.fmi.schedule.generator.Schedule;

import java.time.DayOfWeek;

public record TimeSlot(DayOfWeek dayOfWeek, int hour) {
    public TimeSlot {
        if (!Schedule.SCHEDULED_DAYS_OF_WEEK.contains(dayOfWeek)) {
            throw new IllegalArgumentException("Invalid day of week: " + dayOfWeek);
        }

        if (hour < Schedule.START_HOUR || hour >= Schedule.END_HOUR) {
            throw new IllegalArgumentException("Invalid hour: " + hour);
        }
    }
}
