package intelligent.systems.fmi.schedule.generator;

public record Hall(String faculty, String roomNumber, int availableSeats, boolean isComputerLab) {
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
}