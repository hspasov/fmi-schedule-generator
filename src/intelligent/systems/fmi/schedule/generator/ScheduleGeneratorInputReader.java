package intelligent.systems.fmi.schedule.generator;

import intelligent.systems.fmi.schedule.generator.courses.ElectiveCourse;
import intelligent.systems.fmi.schedule.generator.courses.CompulsoryCourse;
import intelligent.systems.fmi.schedule.generator.halls.Hall;
import intelligent.systems.fmi.schedule.generator.students.Student;
import intelligent.systems.fmi.schedule.generator.students.StudentsStream;
import intelligent.systems.fmi.schedule.generator.teachers.Teacher;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ScheduleGeneratorInputReader {
    public static List<String> readCSVFileLines(String filePath) {
        List<String> lines = new ArrayList<>();
        boolean isHeaderSkipped = false;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!isHeaderSkipped) {
                    isHeaderSkipped = true;
                    continue;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            throw new IllegalStateException("An error has occurred during reading file: " + filePath, e);
        }
        return lines;
    }

    public static Map<String, StudentsStream> readStudentsStreamsFile(String studentsStreamsFilePath) {
        return readCSVFileLines(studentsStreamsFilePath).stream()
            .map(StudentsStream::fromString)
            .collect(Collectors.toMap(StudentsStream::toString, s -> s));
    }

    public static Set<Hall> readHallsFile(String hallsFilePath) {
        return readCSVFileLines(hallsFilePath).stream()
            .map(Hall::fromString)
            .collect(Collectors.toSet());
    }

    public static Map<String, Teacher> readTeachersFile(String teachersFilePath) {
        return readCSVFileLines(teachersFilePath).stream()
            .map(Teacher::fromString)
            .collect(Collectors.toMap(Teacher::id, t -> t));
    }

    public static Set<Student> readStudentsFile(String studentsFilePath, Map<String, StudentsStream> studentsStreams) {
        return readCSVFileLines(studentsFilePath).stream()
            .map(line -> Student.fromString(line, studentsStreams))
            .collect(Collectors.toSet());
    }

    public static Set<CompulsoryCourse> readMandatoryCoursesFile(
        String mandatoryCoursesFilePath,
        Map<String, Teacher> teachers,
        Map<String, StudentsStream> studentsStreams
    ) {
        return readCSVFileLines(mandatoryCoursesFilePath).stream()
            .map(line -> CompulsoryCourse.fromString(line, teachers, studentsStreams))
            .collect(Collectors.toSet());
    }

    public static Set<ElectiveCourse> readElectiveCoursesFile(
        String electiveCoursesFilePath,
        Map<String, Teacher> teachers
    ) {
        return readCSVFileLines(electiveCoursesFilePath).stream()
            .map(line -> ElectiveCourse.fromString(line, teachers))
            .collect(Collectors.toSet());
    }
}
