package intelligent.systems.fmi.schedule.generator;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return id.equals(teacher.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
