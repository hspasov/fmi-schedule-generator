package intelligent.systems.fmi.schedule.generator.students;

import java.util.Objects;

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

    @Override
    public String toString() {
        return major() + " (year: " + year() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentsStream that = (StudentsStream) o;
        return year == that.year && major.equals(that.major);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, year);
    }
}
