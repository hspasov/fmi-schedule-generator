package intelligent.systems.fmi.schedule.generator;

public record HallTimeSlot(Hall hall, TimeSlot timeSlot) {
    public HallTimeSlot {
        if (hall == null) {
            throw new IllegalArgumentException("Hall cannot be null!");
        }

        if (timeSlot == null) {
            throw new IllegalArgumentException("Time slot cannot be null!");
        }
    }
}
