package intelligent.systems.fmi.schedule.generator.allocation;

import intelligent.systems.fmi.schedule.generator.courses.CompulsoryCourse;

import java.util.Objects;
import java.util.TreeSet;

public record CourseAllocationCandidate(CompulsoryCourse course, TreeSet<HallTimeSlot> availableStartTimeSlots) {
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
