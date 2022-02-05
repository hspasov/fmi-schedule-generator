package intelligent.systems.fmi.schedule.generator.courses;

import intelligent.systems.fmi.schedule.generator.teachers.Teacher;

public abstract class Course {
    private final SessionType sessionType;
    private final String name;
    private final Teacher teacher;
    private final int sessionLengthHours;
    private final boolean computersRequired;

    public Course(
        SessionType sessionType,
        String name,
        int sessionLengthHours,
        Teacher teacher,
        boolean computersRequired
    ) {
        this.sessionType = sessionType;
        this.name = name;
        this.sessionLengthHours = sessionLengthHours;
        this.teacher = teacher;
        this.computersRequired = computersRequired;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public String getName() {
        return name;
    }

    public int getSessionLengthHours() {
        return sessionLengthHours;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public boolean areComputersRequired() {
        return computersRequired;
    }
}
