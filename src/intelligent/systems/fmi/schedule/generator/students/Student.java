package intelligent.systems.fmi.schedule.generator.students;

import java.util.Map;

public record Student(StudentsStream studentsStream, int groupNumber, String facultyNumber) {
    public static Student fromString(String input, Map<String, StudentsStream> studentsStreams) {
        final int expectedParts = 3;
        final int studentsStreamIdIdx = 0;
        final int groupNumberIdx = 1;
        final int facultyNumberIdx = 2;

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

        return new Student(
            studentsStream,
            Integer.parseInt(inputParts[groupNumberIdx]),
            inputParts[facultyNumberIdx]
        );
    }
}
