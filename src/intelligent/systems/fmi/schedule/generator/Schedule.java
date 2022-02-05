package intelligent.systems.fmi.schedule.generator;

import intelligent.systems.fmi.schedule.generator.allocation.HallTimeSlot;
import intelligent.systems.fmi.schedule.generator.allocation.SessionAllocation;
import intelligent.systems.fmi.schedule.generator.allocation.TimeSlot;
import intelligent.systems.fmi.schedule.generator.courses.MandatoryCourse;
import intelligent.systems.fmi.schedule.generator.halls.Hall;
import intelligent.systems.fmi.schedule.generator.students.Student;
import intelligent.systems.fmi.schedule.generator.students.StudentsGroup;
import intelligent.systems.fmi.schedule.generator.students.StudentsStream;
import intelligent.systems.fmi.schedule.generator.teachers.Teacher;

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
    private Map<Teacher, Set<HallTimeSlot>> teachersAllocations = new HashMap<>();
    private Map<StudentsGroup, Set<HallTimeSlot>> studentsGroupsAllocations = new HashMap<>();
    private Map<Student, Set<TimeSlot>> studentsTimeSlotAllocations = new HashMap<>();

    public Schedule() {

    }

    public boolean isTeacherScheduledAt(Teacher teacher, TimeSlot timeSlot) {
        Set<HallTimeSlot> teacherTimeSlots = this.teachersAllocations.get(teacher);
        if (teacherTimeSlots == null) {
            return false;
        }
        return teacherTimeSlots.stream().anyMatch(hallTimeSlot -> timeSlot.equals(hallTimeSlot.timeSlot()));
    }

    public boolean areStudentsScheduledAt(StudentsGroup studentsGroup, TimeSlot timeSlot) {
        Set<HallTimeSlot> studentsGroupTimeSlots = this.studentsGroupsAllocations.get(studentsGroup);
        if (studentsGroupTimeSlots == null) {
            return false;
        }
        return studentsGroupTimeSlots.stream().anyMatch(hallTimeSlot -> timeSlot.equals(hallTimeSlot.timeSlot()));
    }

    public boolean areStudentsScheduledAt(StudentsStream studentsStream, TimeSlot timeSlot) {
        for (int groupNumber = 1; groupNumber <= studentsStream.groups(); groupNumber++) {
            StudentsGroup studentsGroup = new StudentsGroup(studentsStream, groupNumber);
            if (this.areStudentsScheduledAt(studentsGroup, timeSlot)) {
                return true;
            }
        }
        return false;
    }

    public boolean isStudentAvailableAt(Student student, TimeSlot timeSlot) {
        Set<TimeSlot> studentTimeSlots = this.studentsTimeSlotAllocations.get(student);
        if (studentTimeSlots == null) {
            return true;
        }
        return !studentTimeSlots.contains(timeSlot);
    }

    public boolean isHallTimeSlotScheduled(HallTimeSlot slot) {
        return this.sessionAllocations.containsKey(slot);
    }

    public void markSlotAsAllocated(MandatoryCourse courseToSchedule, HallTimeSlot slot) {
        this.teachersAllocations.putIfAbsent(courseToSchedule.getTeacher(), new HashSet<>());

        for (int hourOffset = 0; hourOffset < courseToSchedule.getSessionLengthHours(); hourOffset++) {
            HallTimeSlot hallTimeSlot = new HallTimeSlot(
                slot.hall(),
                new TimeSlot(
                    slot.timeSlot().dayOfWeek(),
                    slot.timeSlot().hour() + hourOffset
                )
            );
            this.sessionAllocations.put(hallTimeSlot, new SessionAllocation(hallTimeSlot, courseToSchedule));
            this.teachersAllocations.get(courseToSchedule.getTeacher()).add(hallTimeSlot);

            if (courseToSchedule.getGroupNumber() == null) {
                for (int groupNumber = 1; groupNumber <= courseToSchedule.getStudentsStream().groups(); groupNumber++) {
                    StudentsGroup studentsGroup = new StudentsGroup(courseToSchedule.getStudentsStream(), groupNumber);
                    this.studentsGroupsAllocations.putIfAbsent(studentsGroup, new HashSet<>());
                    this.studentsGroupsAllocations.get(studentsGroup).add(hallTimeSlot);
                }
            } else {
                StudentsGroup studentsGroup = new StudentsGroup(
                    courseToSchedule.getStudentsStream(),
                    courseToSchedule.getGroupNumber()
                );
                this.studentsGroupsAllocations.putIfAbsent(studentsGroup, new HashSet<>());
                this.studentsGroupsAllocations.get(studentsGroup).add(hallTimeSlot);
            }
        }

        // TODO students
    }

    public void markSlotAsUnallocated(HallTimeSlot slot) {
        SessionAllocation allocation = this.sessionAllocations.get(slot);

        for (int hourOffset = 0; hourOffset < allocation.course().getSessionLengthHours(); hourOffset++) {
            HallTimeSlot hallTimeSlot = new HallTimeSlot(
                slot.hall(),
                new TimeSlot(
                    slot.timeSlot().dayOfWeek(),
                    slot.timeSlot().hour() + hourOffset
                )
            );
            this.sessionAllocations.remove(hallTimeSlot);
            this.teachersAllocations.get(allocation.course().getTeacher()).remove(hallTimeSlot);

            if (allocation.course().getGroupNumber() == null) {
                int groupsCount = allocation.course().getStudentsStream().groups();
                for (int groupNumber = 1; groupNumber <= groupsCount; groupNumber++) {
                    this.studentsGroupsAllocations.get(
                        new StudentsGroup(allocation.course().getStudentsStream(), groupNumber)
                    ).remove(hallTimeSlot);
                }
            } else {
                this.studentsGroupsAllocations.get(
                    new StudentsGroup(allocation.course().getStudentsStream(), allocation.course().getGroupNumber())
                ).remove(hallTimeSlot);
            }
        }

        // TODO students
    }

    public void printHallSchedule(Hall hall) {
        for (DayOfWeek dayOfWeek : Schedule.SCHEDULED_DAYS_OF_WEEK) {
            System.out.println(dayOfWeek + ":");

            for (int hour = Schedule.START_HOUR; hour < Schedule.END_HOUR; hour++) {
                TimeSlot timeSlot = new TimeSlot(dayOfWeek, hour);
                HallTimeSlot hallTimeSlot = new HallTimeSlot(hall, timeSlot);
                SessionAllocation allocation = this.sessionAllocations.get(hallTimeSlot);

                if (allocation == null) {
                    System.out.println(hour + ": -");
                } else {
                    System.out.println(hour + ": " + allocation);
                }
            }
            System.out.println();
        }
    }

    public void printStudentsStreamSchedule(StudentsGroup studentsGroup) {
        System.out.println("Schedule for " + studentsGroup);

        Set<HallTimeSlot> studentsGroupAllocations = this.studentsGroupsAllocations.get(studentsGroup);
        Map<TimeSlot, Hall> allocationHalls = new HashMap<>();

        for (HallTimeSlot slot : studentsGroupAllocations) {
            allocationHalls.put(slot.timeSlot(), slot.hall());
        }

        for (DayOfWeek dayOfWeek : Schedule.SCHEDULED_DAYS_OF_WEEK) {
            System.out.println(dayOfWeek + ":");

            for (int hour = Schedule.START_HOUR; hour < Schedule.END_HOUR; hour++) {
                TimeSlot timeSlot = new TimeSlot(dayOfWeek, hour);
                Hall hall = allocationHalls.get(timeSlot);

                if (hall == null) {
                    System.out.println(hour + ": -");
                } else {
                    SessionAllocation allocation = this.sessionAllocations.get(new HallTimeSlot(hall, timeSlot));
                    System.out.println(hour + ": " + allocation);
                }
            }

            System.out.println();
        }
    }

    public void printTeacherSchedule(Teacher teacher) {
        System.out.println("Schedule of " + teacher.name());

        Set<HallTimeSlot> teacherAllocations = this.teachersAllocations.get(teacher);
        Map<TimeSlot, Hall> allocationHalls = new HashMap<>();

        for (HallTimeSlot slot : teacherAllocations) {
            allocationHalls.put(slot.timeSlot(), slot.hall());
        }

        for (DayOfWeek dayOfWeek : Schedule.SCHEDULED_DAYS_OF_WEEK) {
            System.out.println(dayOfWeek + ":");

            for (int hour = Schedule.START_HOUR; hour < Schedule.END_HOUR; hour++) {
                TimeSlot timeSlot = new TimeSlot(dayOfWeek, hour);
                Hall hall = allocationHalls.get(timeSlot);

                if (hall == null) {
                    System.out.println(hour + ": -");
                } else {
                    SessionAllocation allocation = this.sessionAllocations.get(new HallTimeSlot(hall, timeSlot));
                    System.out.println(hour + ": " + allocation);
                }
            }

            System.out.println();
        }

        System.out.println();
    }

    // TODO printStudentSchedule
}
