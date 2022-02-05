package intelligent.systems.fmi.schedule.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ScheduleGenerator {
    final private Map<String, StudentsStream> studentsStreams;
    final private Set<Hall> halls;
    final private Map<String, Teacher> teachers;
    final private Set<Student> students;
    final private Set<MandatoryCourse> mandatoryCourses;
    final private Set<ElectiveCourse> electiveCourses;
    final private Schedule schedule = new Schedule();
    private int count = 0;

    private static List<String> readCSVFileLines(String filePath) {
        List<String> lines = new ArrayList<>();
        boolean isHeaderSkipped = false;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while((line = reader.readLine()) != null) {
                if (!isHeaderSkipped) {
                    isHeaderSkipped = true;
                    continue;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            throw new IllegalStateException("An error has occurred during reading file: " + filePath, e);
        }
        return lines;
    }

    private static Map<String, StudentsStream> readStudentsStreamsFile(String studentsStreamsFilePath) {
        return readCSVFileLines(studentsStreamsFilePath).stream()
            .map(StudentsStream::fromString)
            .collect(Collectors.toMap(StudentsStream::toString, s -> s));
    }

    private static Set<Hall> readHallsFile(String hallsFilePath) {
        return readCSVFileLines(hallsFilePath).stream()
            .map(Hall::fromString)
            .collect(Collectors.toSet());
    }

    private static Map<String, Teacher> readTeachersFile(String teachersFilePath) {
        return readCSVFileLines(teachersFilePath).stream()
            .map(Teacher::fromString)
            .collect(Collectors.toMap(Teacher::id, t -> t));
    }

    private static Set<Student> readStudentsFile(String studentsFilePath, Map<String, StudentsStream> studentsStreams) {
        return readCSVFileLines(studentsFilePath).stream()
            .map(line -> Student.fromString(line, studentsStreams))
            .collect(Collectors.toSet());
    }

    private static Set<MandatoryCourse> readMandatoryCoursesFile(
        String mandatoryCoursesFilePath,
        Map<String, Teacher> teachers,
        Map<String, StudentsStream> studentsStreams
    ) {
        return readCSVFileLines(mandatoryCoursesFilePath).stream()
            .map(line -> MandatoryCourse.fromString(line, teachers, studentsStreams))
            .collect(Collectors.toSet());
    }

    private static Set<ElectiveCourse> readElectiveCoursesFile(
        String electiveCoursesFilePath,
        Map<String, Teacher> teachers
    ) {
        return readCSVFileLines(electiveCoursesFilePath).stream()
            .map(line -> ElectiveCourse.fromString(line, teachers))
            .collect(Collectors.toSet());
    }

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

        Set<HallTimeSlot> slots = new HashSet<>();

        for (DayOfWeek dayOfWeek : Schedule.SCHEDULED_DAYS_OF_WEEK) {
            for (int hour = Schedule.START_HOUR; hour < Schedule.END_HOUR; hour++) {
                for (Hall hall : halls) {
                    slots.add(new HallTimeSlot(hall, new TimeSlot(dayOfWeek, hour)));
                }
            }
        }

        for (MandatoryCourse course : this.mandatoryCourses) {
            course.setAvailableStartTimeSlots(slots);
        }
    }

    private void enforceHallMaxCapacity (MandatoryCourse courseToSchedule) {
        long attendingStudentsCount = this.students.stream()
            .filter(student -> student.studentsStream().equals(courseToSchedule.getStudentsStream()))
            .filter(student -> (
                courseToSchedule.getGroupNumber() == null ||
                    student.groupNumber() == courseToSchedule.getGroupNumber()
            )).count();

        Set<HallTimeSlot> availableStartTimeSlots = courseToSchedule.getAvailableStartTimeSlots();
        availableStartTimeSlots.removeIf(slot -> slot.hall().availableSeats() < attendingStudentsCount);

        if (availableStartTimeSlots.size() == 0) {
            System.out.println("all removed");
        }
    }

    private void enforceComputerLabRequirement (MandatoryCourse courseToSchedule) {
        Set<HallTimeSlot> availableStartTimeSlots = courseToSchedule.getAvailableStartTimeSlots();
        availableStartTimeSlots.removeIf(
            slot -> courseToSchedule.areComputersRequired() && !slot.hall().isComputerLab()
        );
    }

    // TODO arc consistency must be applied for all enforce functions that take schedule
    private Set<HallTimeSlot> enforceNonOverlappingScheduleRequirement(MandatoryCourse courseToSchedule) {
        Set<HallTimeSlot> availableStartTimeSlots = courseToSchedule.getAvailableStartTimeSlots();
        Set<HallTimeSlot> removedStartTimeSlots = new HashSet<>();

        for (Iterator<HallTimeSlot> it = availableStartTimeSlots.iterator(); it.hasNext();) {
            HallTimeSlot startTimeSlot = it.next();

            if (startTimeSlot.timeSlot().hour() + courseToSchedule.getSessionLengthHours() > Schedule.END_HOUR) {
                removedStartTimeSlots.add(startTimeSlot);
                it.remove();
                continue;
            }

            List<HallTimeSlot> sessionSlots = IntStream.range(0, courseToSchedule.getSessionLengthHours())
                .mapToObj(hourOffset -> new HallTimeSlot(
                    startTimeSlot.hall(),
                    new TimeSlot(
                        startTimeSlot.timeSlot().dayOfWeek(),
                        startTimeSlot.timeSlot().hour() + hourOffset
                    )))
                .toList();

            for (HallTimeSlot sessionSlot : sessionSlots) {
                // TODO refactor this! very bad code repetition
                if (
                    this.schedule.isHallTimeSlotScheduled(sessionSlot) ||
                    this.schedule.isTeacherScheduledAt(courseToSchedule.getTeacher(), sessionSlot.timeSlot())
                ) {
                    removedStartTimeSlots.add(startTimeSlot);
                    it.remove();
                    break;
                }

                if (courseToSchedule.getGroupNumber() == null) {
                    if (this.schedule.areStudentsScheduledAt(courseToSchedule.getStudentsStream(), sessionSlot.timeSlot())) {
                        removedStartTimeSlots.add(startTimeSlot);
                        it.remove();
                        break;
                    }
                } else {
                    StudentsGroup studentsGroup = new StudentsGroup(
                        courseToSchedule.getStudentsStream(),
                        courseToSchedule.getGroupNumber()
                    );
                    if (this.schedule.areStudentsScheduledAt(studentsGroup, sessionSlot.timeSlot())) {
                        removedStartTimeSlots.add(startTimeSlot);
                        it.remove();
                        break;
                    }
                }
            }
        }

        return removedStartTimeSlots;
    }

    private void enforceConstraints(Set<MandatoryCourse> coursesToSchedule, Schedule schedule) {
        // TODO create new class - MandatoryCourseAllocationCandidate
        // TODO create an abstract class Course
        // TODO ElectiveCourses

    }

    private MandatoryCourse getMinimumAvailableSlotsCourse(Set<MandatoryCourse> coursesToSchedule) {
        // TODO optimize
        return coursesToSchedule.stream()
            .min(Comparator.comparingInt(course -> course.getAvailableStartTimeSlots().size()))
            .orElseThrow();
    }

    private boolean searchForSolution(Set<MandatoryCourse> coursesToSchedule, int depth) {
        this.count++;
        System.out.println("depth: " + depth + ", count: " + this.count);

        if (coursesToSchedule.isEmpty()) {
            return true;
        }

        MandatoryCourse courseToSchedule = this.getMinimumAvailableSlotsCourse(coursesToSchedule);
        coursesToSchedule.remove(courseToSchedule);
        Set<HallTimeSlot> removedStartTimeSlots = this.enforceNonOverlappingScheduleRequirement(courseToSchedule);
        Set<HallTimeSlot> availableStartTimeSlots = courseToSchedule.getAvailableStartTimeSlots();

        for (HallTimeSlot slot : availableStartTimeSlots) {
            this.schedule.markSlotAsAllocated(courseToSchedule, slot);
            if (searchForSolution(coursesToSchedule, depth + 1)) {
                return true;
            }
            this.schedule.markSlotAsUnallocated(slot);
        }

        courseToSchedule.getAvailableStartTimeSlots().addAll(removedStartTimeSlots);
        coursesToSchedule.add(courseToSchedule);

        return false;
    }

    public Schedule generate() {
        Set<MandatoryCourse> coursesToSchedule = new HashSet<>(this.mandatoryCourses);
        for (MandatoryCourse course : coursesToSchedule) {
            this.enforceHallMaxCapacity(course);
            this.enforceComputerLabRequirement(course);
        }
        if (!this.searchForSolution(coursesToSchedule, 0)) {
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
        Set<MandatoryCourse> mandatoryCourses = readMandatoryCoursesFile(args[mandatoryCoursesArgIdx], teachers, studentsStreams);
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
