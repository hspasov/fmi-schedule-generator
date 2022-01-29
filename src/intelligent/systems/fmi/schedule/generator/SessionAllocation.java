package intelligent.systems.fmi.schedule.generator;

import java.time.DayOfWeek;

public record SessionAllocation(
    StudentsStream studentsStream,
    DayOfWeek dayOfWeek,
    Integer groupNumber,
    int startHour,
    MandatoryCourse course,
    Hall hall,
    Teacher teacher
    ) {

    @Override
    public String toString() {
        String[] parts = {
            studentsStream.major(),
            String.valueOf(studentsStream.year()),
            dayOfWeek.toString(),
            groupNumber == null ? "" : String.valueOf(groupNumber),
            String.valueOf(startHour),
            course.getName(),
            hall.faculty(),
            hall.roomNumber(),
            teacher.name()
        };
        return String.join(",", parts);
    }
}
