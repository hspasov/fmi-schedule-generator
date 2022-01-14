package intelligent.systems.fmi.schedule.generator;

public record StudentsStream(String major, int year, int groups)  {
    public static StudentsStream fromString(String input) {
        final int expectedParts = 3;
        final int majorIdx = 0;
        final int yearIdx = 1;
        final int groupsIdx = 2;

        String[] inputParts = input.split(",");
        if (inputParts.length != expectedParts) {
            throw new IllegalArgumentException(
                "Expected "
                    + expectedParts
                    + " parts, but got "
                    + inputParts.length
            );
        }
        return new StudentsStream(
            inputParts[majorIdx],
            Integer.parseInt(inputParts[yearIdx]),
            Integer.parseInt(inputParts[groupsIdx])
        );
    }
}
