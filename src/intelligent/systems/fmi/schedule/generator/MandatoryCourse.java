package intelligent.systems.fmi.schedule.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MandatoryCourse {
    private final StudentsStream studentsStream;
    private final Integer groupNumber;
    private final SessionType sessionType;
    private final String name;
    private final Teacher teacher;
    private final int sessionLengthHours;
    private final boolean areComputersRequired;
    private Set<Slot> availableStartTimeSlots = new HashSet<>();

    public static MandatoryCourse fromString(
        String input,
        Map<String, Teacher> teachers,
        Map<String, StudentsStream> studentsStreams
    ) {
        final int expectedParts = 7;
        final int studentsStreamIdIdx = 0;
        final int groupNumberIdx = 1;
        final int sessionTypeIdx = 2;
        final int courseNameIdx = 3;
        final int sessionLengthIdx = 4;
        final int teacherIdIdx = 5;
        final int areComputersRequiredIdx = 6;

        String[] inputParts = input.split(",");
        if (inputParts.length != expectedParts) {
            throw new IllegalArgumentException(
                "Expected "
                    + expectedParts
                    + " parts, but got "
                    + inputParts.length
            );
        }

        StudentsStream studentsStream = studentsStreams.get(inputParts[studentsStreamIdIdx]);
        if (studentsStream == null) {
            throw new IllegalArgumentException("Invalid studentsStreamId: students stream does not exist!");
        }

        Teacher teacher = teachers.get(inputParts[teacherIdIdx]);
        if (teacher == null) {
            throw new IllegalArgumentException("Invalid teacherId: teacher does not exist!");
        }

        boolean areComputersRequired = switch (inputParts[areComputersRequiredIdx]) {
            case "t" -> true;
            case "f" -> false;
            default -> throw new IllegalArgumentException("Invalid flag `areComputersRequired`");
        };

        return new MandatoryCourse(
            studentsStream,
            inputParts[groupNumberIdx].length() == 0 ? null : Integer.parseInt(inputParts[groupNumberIdx]),
            SessionType.valueOf(inputParts[sessionTypeIdx]),
            inputParts[courseNameIdx],
            Integer.parseInt(inputParts[sessionLengthIdx]),
            teacher,
            areComputersRequired
        );
    }

    public MandatoryCourse(
        StudentsStream studentsStream,
        Integer groupNumber,
        SessionType sessionType,
        String name,
        int sessionLengthHours,
        Teacher teacher,
        boolean areComputersRequired
    ) {
        this.studentsStream = studentsStream;
        this.groupNumber = groupNumber;
        this.sessionType = sessionType;
        this.name = name;
        this.sessionLengthHours = sessionLengthHours;
        this.teacher = teacher;
        this.areComputersRequired = areComputersRequired;
    }

    public StudentsStream getStudentsStream() {
        return studentsStream;
    }

    public Integer getGroupNumber() {
        return groupNumber;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public String getName() {
        return name;
    }

    public int getSessionLengthHours() {
        return sessionLengthHours;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public boolean isAreComputersRequired() {
        return areComputersRequired;
    }

    public Set<Slot> getAvailableStartTimeSlots() {
        return this.availableStartTimeSlots;
    }

    public void setAvailableStartTimeSlots(Set<Slot> slots) {
        this.availableStartTimeSlots = new HashSet<>(slots);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MandatoryCourse that = (MandatoryCourse) o;
        return studentsStream.equals(that.studentsStream) && Objects.equals(groupNumber, that.groupNumber) &&
            sessionType == that.sessionType && name.equals(that.name) && teacher.equals(that.teacher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentsStream, groupNumber, sessionType, name, teacher);
    }
}
