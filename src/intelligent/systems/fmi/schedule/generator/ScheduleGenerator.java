package intelligent.systems.fmi.schedule.generator;

import intelligent.systems.fmi.schedule.generator.allocation.CourseAllocationCandidate;
import intelligent.systems.fmi.schedule.generator.allocation.HallTimeSlot;
import intelligent.systems.fmi.schedule.generator.allocation.TimeSlot;
import intelligent.systems.fmi.schedule.generator.courses.ElectiveCourse;
import intelligent.systems.fmi.schedule.generator.courses.MandatoryCourse;
import intelligent.systems.fmi.schedule.generator.halls.Hall;
import intelligent.systems.fmi.schedule.generator.students.Student;
import intelligent.systems.fmi.schedule.generator.students.StudentsGroup;
import intelligent.systems.fmi.schedule.generator.students.StudentsStream;
import intelligent.systems.fmi.schedule.generator.teachers.Teacher;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static intelligent.systems.fmi.schedule.generator.ScheduleGeneratorInputReader.readElectiveCoursesFile;
import static intelligent.systems.fmi.schedule.generator.ScheduleGeneratorInputReader.readHallsFile;
import static intelligent.systems.fmi.schedule.generator.ScheduleGeneratorInputReader.readMandatoryCoursesFile;
import static intelligent.systems.fmi.schedule.generator.ScheduleGeneratorInputReader.readStudentsFile;
import static intelligent.systems.fmi.schedule.generator.ScheduleGeneratorInputReader.readStudentsStreamsFile;
import static intelligent.systems.fmi.schedule.generator.ScheduleGeneratorInputReader.readTeachersFile;

public class ScheduleGenerator {
    final private Map<String, StudentsStream> studentsStreams;
    final private Set<Hall> halls;
    final private Map<String, Teacher> teachers;
    final private Set<Student> students;
    final private Set<MandatoryCourse> mandatoryCourses;
    final private Set<ElectiveCourse> electiveCourses;
    final private Set<CourseAllocationCandidate> allocationCandidates;
    final private Schedule schedule = new Schedule();
    private int count = 0;

    public ScheduleGenerator(
        Map<String, StudentsStream> studentsStreams,
        Set<Hall> halls,
        Map<String, Teacher> teachers,
        Set<Student> students,
        Set<MandatoryCourse> mandatoryCourses,
        Set<ElectiveCourse> electiveCourses
    ) {
        this.studentsStreams = studentsStreams;
        this.halls = halls;
        this.teachers = teachers;
        this.students = students;
        this.mandatoryCourses = mandatoryCourses;
        this.electiveCourses = electiveCourses;

        this.allocationCandidates = new HashSet<>();
        Set<HallTimeSlot> slots = new HashSet<>();

        for (DayOfWeek dayOfWeek : Schedule.SCHEDULED_DAYS_OF_WEEK) {
            for (int hour = Schedule.START_HOUR; hour < Schedule.END_HOUR; hour++) {
                for (Hall hall : halls) {
                    slots.add(new HallTimeSlot(hall, new TimeSlot(dayOfWeek, hour)));
                }
            }
        }

        for (MandatoryCourse course : this.mandatoryCourses) {
            this.allocationCandidates.add(new CourseAllocationCandidate(course, new HashSet<>(slots)));
        }
    }

    private void enforceHallMaxCapacity(CourseAllocationCandidate candidate) {
        long attendingStudentsCount = this.students.stream()
            .filter(student -> student.studentsStream().equals(candidate.course().getStudentsStream()))
            .filter(student -> (
                candidate.course().getGroupNumber() == null ||
                    student.groupNumber() == candidate.course().getGroupNumber()
            )).count();

        Set<HallTimeSlot> availableStartTimeSlots = candidate.availableStartTimeSlots();
        availableStartTimeSlots.removeIf(slot -> slot.hall().availableSeats() < attendingStudentsCount);
    }

    private void enforceComputerLabRequirement(CourseAllocationCandidate candidate) {
        Set<HallTimeSlot> availableStartTimeSlots = candidate.availableStartTimeSlots();
        availableStartTimeSlots.removeIf(
            slot -> candidate.course().areComputersRequired() && !slot.hall().isComputerLab()
        );
    }

    // TODO arc consistency must be applied for all enforce functions that take schedule
    private Set<HallTimeSlot> enforceNonOverlappingScheduleRequirement(CourseAllocationCandidate candidate) {
        Set<HallTimeSlot> availableStartTimeSlots = candidate.availableStartTimeSlots();
        Set<HallTimeSlot> removedStartTimeSlots = new HashSet<>();

        for (Iterator<HallTimeSlot> it = availableStartTimeSlots.iterator(); it.hasNext();) {
            HallTimeSlot startTimeSlot = it.next();

            if (startTimeSlot.timeSlot().hour() + candidate.course().getSessionLengthHours() > Schedule.END_HOUR) {
                removedStartTimeSlots.add(startTimeSlot);
                it.remove();
                continue;
            }

            List<HallTimeSlot> sessionSlots = IntStream.range(0, candidate.course().getSessionLengthHours())
                .mapToObj(hourOffset -> new HallTimeSlot(
                    startTimeSlot.hall(),
                    new TimeSlot(
                        startTimeSlot.timeSlot().dayOfWeek(),
                        startTimeSlot.timeSlot().hour() + hourOffset
                    )))
                .toList();

            for (HallTimeSlot sessionSlot : sessionSlots) {
                if (
                    this.schedule.isHallTimeSlotScheduled(sessionSlot) ||
                    this.schedule.isTeacherScheduledAt(candidate.course().getTeacher(), sessionSlot.timeSlot()) ||
                        (
                            candidate.course().getGroupNumber() == null &&
                                this.schedule.areStudentsScheduledAt(
                                    candidate.course().getStudentsStream(),
                                    sessionSlot.timeSlot()
                                )
                        ) ||
                        (
                            candidate.course().getGroupNumber() != null &&
                                this.schedule.areStudentsScheduledAt(
                                    new StudentsGroup(
                                        candidate.course().getStudentsStream(),
                                        candidate.course().getGroupNumber()
                                    ),
                                    sessionSlot.timeSlot()
                                )
                        )
                ) {
                    removedStartTimeSlots.add(startTimeSlot);
                    it.remove();
                    break;
                }
            }
        }

        return removedStartTimeSlots;
    }

    private void enforceConstraints(Set<MandatoryCourse> coursesToSchedule, Schedule schedule) {
        // TODO ElectiveCourses
    }

    private CourseAllocationCandidate getMinimumAvailableSlotsCandidate() {
        return this.allocationCandidates.stream()
            .min(Comparator.comparingInt(candidate -> candidate.availableStartTimeSlots().size()))
            .orElseThrow();
    }

    private boolean searchForSolution(int depth) {
        this.count++;
        System.out.println("depth: " + depth + ", count: " + this.count);

        if (this.allocationCandidates.isEmpty()) {
            return true;
        }

        CourseAllocationCandidate courseToSchedule = this.getMinimumAvailableSlotsCandidate();
        this.allocationCandidates.remove(courseToSchedule);
        Set<HallTimeSlot> removedStartTimeSlots = this.enforceNonOverlappingScheduleRequirement(courseToSchedule);

        for (HallTimeSlot slot : courseToSchedule.availableStartTimeSlots()) {
            this.schedule.markSlotAsAllocated(courseToSchedule.course(), slot);
            if (searchForSolution(depth + 1)) {
                return true;
            }
            this.schedule.markSlotAsUnallocated(slot);
        }

        courseToSchedule.availableStartTimeSlots().addAll(removedStartTimeSlots);
        this.allocationCandidates.add(courseToSchedule);

        return false;
    }

    public Schedule generate() {
        for (CourseAllocationCandidate candidate : this.allocationCandidates) {
            this.enforceHallMaxCapacity(candidate);
            this.enforceComputerLabRequirement(candidate);
        }
        if (!this.searchForSolution(0)) {
            // NO SOLUTION
            return null;
        }
        return this.schedule;
    }

    public static void main(String[] args) {
        final int studentsStreamsArgIdx = 0;
        final int hallsArgIdx = 1;
        final int teachersArgIdx = 2;
        final int studentsArgIdx = 3;
        final int mandatoryCoursesArgIdx = 4;
        final int electiveCoursesArgIdx = 5;

        Map<String, StudentsStream> studentsStreams = readStudentsStreamsFile(args[studentsStreamsArgIdx]);
        Set<Hall> halls = readHallsFile(args[hallsArgIdx]);
        Map<String, Teacher> teachers = readTeachersFile(args[teachersArgIdx]);
        Set<Student> students = readStudentsFile(args[studentsArgIdx], studentsStreams);
        Set<MandatoryCourse> mandatoryCourses = readMandatoryCoursesFile(
            args[mandatoryCoursesArgIdx],
            teachers,
            studentsStreams
        );
        Set<ElectiveCourse> electiveCourses = readElectiveCoursesFile(args[electiveCoursesArgIdx], teachers);

        ScheduleGenerator generator = new ScheduleGenerator(
            studentsStreams,
            halls,
            teachers,
            students,
            mandatoryCourses,
            electiveCourses
        );
        Schedule schedule = generator.generate();

        StudentsStream ss = new StudentsStream("Software Engineering", 1, 6);

        for (int groupNumber = 1; groupNumber <= ss.groups(); groupNumber++) {
            schedule.printStudentsStreamSchedule(new StudentsGroup(ss, groupNumber));
            System.out.println("------------------------");
        }

        System.out.println("Finished");
    }
}
