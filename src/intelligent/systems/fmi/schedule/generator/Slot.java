package intelligent.systems.fmi.schedule.generator;

import java.time.DayOfWeek;

public record Slot(DayOfWeek dayOfWeek, int hour, Hall hall) {
    public Slot {
        if (!Schedule.SCHEDULED_DAYS_OF_WEEK.contains(dayOfWeek)) {
            throw new IllegalArgumentException("Invalid day of week: " + dayOfWeek);
        }

        if (hour < Schedule.START_HOUR || hour >= Schedule.END_HOUR) {
            throw new IllegalArgumentException("Invalid hour: " + hour);
        }

        if (hall == null) {
            throw new IllegalArgumentException("Hall cannot be null!");
        }
    }
}
