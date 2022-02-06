package intelligent.systems.fmi.schedule.generator.allocation;

import intelligent.systems.fmi.schedule.generator.courses.CompulsoryCourse;

public record SessionAllocation(HallTimeSlot slot, CompulsoryCourse course) {
    @Override
    public String toString() {
        String[] parts = {
            course.getStudentsStream().major(),
            String.valueOf(course.getStudentsStream().year()),
            slot.timeSlot().dayOfWeek().toString(),
            course.getGroupNumber() == null ? "" : String.valueOf(course.getGroupNumber()),
            String.valueOf(slot.timeSlot().hour()),
            course.getName(),
            slot.hall().faculty(),
            slot.hall().roomNumber(),
            course.getTeacher().name()
        };
        return String.join(",", parts);
    }
}
