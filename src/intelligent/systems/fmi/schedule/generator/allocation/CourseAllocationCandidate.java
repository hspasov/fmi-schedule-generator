package intelligent.systems.fmi.schedule.generator.allocation;

import intelligent.systems.fmi.schedule.generator.courses.MandatoryCourse;

import java.util.Objects;
import java.util.Set;

public record CourseAllocationCandidate(MandatoryCourse course, Set<HallTimeSlot> availableStartTimeSlots) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseAllocationCandidate candidate = (CourseAllocationCandidate) o;
        return course.equals(candidate.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(course);
    }
}
