package intelligent.systems.fmi.schedule.generator.allocation;

import intelligent.systems.fmi.schedule.generator.Schedule;

import java.time.DayOfWeek;

public record TimeSlot(DayOfWeek dayOfWeek, int hour) implements Comparable<TimeSlot> {
    public TimeSlot {
        if (!Schedule.SCHEDULED_DAYS_OF_WEEK.contains(dayOfWeek)) {
            throw new IllegalArgumentException("Invalid day of week: " + dayOfWeek);
        }

        if (hour < Schedule.START_HOUR || hour >= Schedule.END_HOUR) {
            throw new IllegalArgumentException("Invalid hour: " + hour);
        }
    }

    @Override
    public int compareTo(TimeSlot o) {
        int result = Integer.compare(this.hour, o.hour);
        if (result != 0) {
            return result;
        }
        return Integer.compare(this.dayOfWeek.getValue(), o.dayOfWeek.getValue());
    }
}
