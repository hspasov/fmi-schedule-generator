package intelligent.systems.fmi.schedule.generator.halls;

import java.util.Objects;

public record Hall(String faculty, String roomNumber, int availableSeats, boolean isComputerLab)
    implements Comparable<Hall> {
    public static Hall fromString(String input) {
        final int expectedParts = 4;
        final int facultyIdx = 0;
        final int roomNumberIdx = 1;
        final int availableSeatsIdx = 2;
        final int isComputerLabIdx = 3;

        String[] inputParts = input.split(",");
        if (inputParts.length != expectedParts) {
            throw new IllegalArgumentException(
                "Expected "
                    + expectedParts
                    + " parts, but got "
                    + inputParts.length
            );
        }

        boolean isComputerLab = switch (inputParts[isComputerLabIdx]) {
            case "t" -> true;
            case "f" -> false;
            default -> throw new IllegalArgumentException("Invalid flag `isComputerLab`");
        };

        return new Hall(
            inputParts[facultyIdx],
            inputParts[roomNumberIdx],
            Integer.parseInt(inputParts[availableSeatsIdx]),
            isComputerLab
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hall hall = (Hall) o;
        return faculty.equals(hall.faculty) && roomNumber.equals(hall.roomNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(faculty, roomNumber);
    }

    @Override
    public int compareTo(Hall o) {
        int result = this.faculty.compareTo(o.faculty);
        if (result != 0) {
            return result;
        }
        return this.roomNumber.compareTo(o.roomNumber);
    }
}