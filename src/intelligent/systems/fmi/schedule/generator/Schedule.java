package intelligent.systems.fmi.schedule.generator;

import java.sql.Time;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private Map<HallTimeSlot, SessionAllocation> sessionAllocations = new HashMap<>();
    private Map<Teacher, Set<TimeSlot>> teachersTimeSlotAllocations = new HashMap<>();
    private Map<Student, Set<TimeSlot>> studentsTimeSlotAllocations = new HashMap<>();

    public Schedule() {

    }

    // TODO refactor Teacher and Student to have a common parent

    public boolean isTeacherAvailableAt (Teacher teacher, TimeSlot timeSlot) {
        Set<TimeSlot> teacherTimeSlots = this.teachersTimeSlotAllocations.get(teacher);
        if (teacherTimeSlots == null) {
            return true;
        }
        return !teacherTimeSlots.contains(timeSlot);
    }

    public boolean isStudentAvailableAt (Student student, TimeSlot timeSlot) {
        Set<TimeSlot> studentTimeSlots = this.studentsTimeSlotAllocations.get(student);
        if (studentTimeSlots == null) {
            return true;
        }
        return !studentTimeSlots.contains(timeSlot);
    }

    public boolean isHallTimeSlotAvailable(HallTimeSlot slot) {
        return !this.sessionAllocations.containsKey(slot);
    }

    public void markSlotAsAllocated (MandatoryCourse courseToSchedule, HallTimeSlot slot) {
        this.sessionAllocations.put(slot, new SessionAllocation(
            courseToSchedule.getStudentsStream(),
            slot.timeSlot().dayOfWeek(),
            courseToSchedule.getGroupNumber(),
            slot.timeSlot().hour(),
            courseToSchedule,
            slot.hall(),
            courseToSchedule.getTeacher()
        ));
        this.teachersTimeSlotAllocations.putIfAbsent(courseToSchedule.getTeacher(), new HashSet<>());
        this.teachersTimeSlotAllocations.get(courseToSchedule.getTeacher()).add(slot.timeSlot());
        // TODO students
    }

    public void markSlotAsUnallocated (HallTimeSlot slot) {
        SessionAllocation allocation = this.sessionAllocations.remove(slot);
        this.teachersTimeSlotAllocations.get(allocation.teacher()).remove(slot.timeSlot());
        // TODO students
    }
}
