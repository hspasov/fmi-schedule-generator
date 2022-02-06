package intelligent.systems.fmi.schedule.generator.allocation;

import intelligent.systems.fmi.schedule.generator.halls.Hall;

import java.util.Comparator;

public record HallTimeSlot(Hall hall, TimeSlot timeSlot) implements Comparable<HallTimeSlot> {
    public HallTimeSlot {
        if (hall == null) {
            throw new IllegalArgumentException("Hall cannot be null!");
        }

        if (timeSlot == null) {
            throw new IllegalArgumentException("Time slot cannot be null!");
        }
    }

    @Override
    public int compareTo(HallTimeSlot o) {
        int result = this.timeSlot.compareTo(o.timeSlot);
        if (result != 0) {
            return result;
        }
        return this.hall.compareTo(o.hall);
    }
}
