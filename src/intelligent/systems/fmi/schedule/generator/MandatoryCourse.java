package intelligent.systems.fmi.schedule.generator;

import java.util.Map;

public record MandatoryCourse(
    StudentsStream studentsStream,
    Integer groupNumber,
    SessionType sessionType,
    String name,
    int sessionLengthHours,
    Teacher teacher,
    boolean areComputersRequired) {

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
}
