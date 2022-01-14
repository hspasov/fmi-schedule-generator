package intelligent.systems.fmi.schedule.generator;

import java.util.Map;

public record ElectiveCourse(
    SessionType sessionType,
    String name,
    int sessionLengthHours,
    Teacher teacher,
    boolean areComputersRequired) {

    public static ElectiveCourse fromString(String input, Map<String, Teacher> teachers) {
        final int expectedParts = 5;
        final int sessionTypeIdx = 0;
        final int courseNameIdx = 1;
        final int sessionLengthIdx = 2;
        final int teacherIdIdx = 3;
        final int areComputersRequiredIdx = 4;

        String[] inputParts = input.split(",");

        if (inputParts.length != expectedParts) {
            throw new IllegalArgumentException(
                "Expected "
                + expectedParts
                + " parts, but got "
                + inputParts.length
            );
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

        return new ElectiveCourse(
            SessionType.valueOf(inputParts[sessionTypeIdx]),
            inputParts[courseNameIdx],
            Integer.parseInt(inputParts[sessionLengthIdx]),
            teacher,
            areComputersRequired
        );
    }
//
//    @Override
//    public String toString() {
//        String[] parts = {
//            sessionType().toString(),
//            name(),
//            String.valueOf(sessionLengthHours),
//            teacher().id(),
//            areComputersRequired ? "t" : "f"
//        };
//
//        return String.join(",", parts);
//    }
}
