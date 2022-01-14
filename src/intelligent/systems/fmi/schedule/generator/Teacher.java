package intelligent.systems.fmi.schedule.generator;

public record Teacher(String id, String name) {
    public static Teacher fromString(String input) {
        final int expectedParts = 2;
        final int idIdx = 0;
        final int nameIdx = 1;

        String[] inputParts = input.split(",");
        if (inputParts.length != expectedParts) {
            throw new IllegalArgumentException(
                "Expected "
                    + expectedParts
                    + " parts, but got "
                    + inputParts.length
            );
        }
        return new Teacher(inputParts[idIdx], inputParts[nameIdx]);
    }
}
