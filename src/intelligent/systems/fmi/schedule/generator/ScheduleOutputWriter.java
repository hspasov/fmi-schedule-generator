package intelligent.systems.fmi.schedule.generator;

import intelligent.systems.fmi.schedule.generator.allocation.HallTimeSlot;
import intelligent.systems.fmi.schedule.generator.allocation.SessionAllocation;
import intelligent.systems.fmi.schedule.generator.allocation.TimeSlot;
import intelligent.systems.fmi.schedule.generator.courses.CompulsoryCourse;
import intelligent.systems.fmi.schedule.generator.halls.Hall;
import intelligent.systems.fmi.schedule.generator.students.StudentsGroup;
import intelligent.systems.fmi.schedule.generator.students.StudentsStream;
import intelligent.systems.fmi.schedule.generator.teachers.Teacher;
import org.apache.poi.hssf.record.formula.functions.Row;
import org.apache.poi.hssf.record.formula.functions.Time;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;

import java.io.File;
import java.io.FileOutputStream;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScheduleOutputWriter {
    private Schedule schedule;

    public ScheduleOutputWriter(Schedule schedule) {
        this.schedule = schedule;
    }

    private static void setHoursLegend(HSSFSheet sheet) {
        int rowNumber = 0;
        short hourCellOffset = 2;
        HSSFRow row = sheet.createRow(rowNumber);

        for (int hour = Schedule.START_HOUR; hour < Schedule.END_HOUR; hour++) {
            HSSFCell cell = row.createCell(hourCellOffset);
            hourCellOffset++;
            cell.setCellValue(hour);
        }
    }

    public void writeTeacherSchedule(Teacher teacher) {
        Set<HallTimeSlot> teacherAllocations = this.schedule.getTeachersAllocations().get(teacher);
        Map<TimeSlot, Hall> allocationHalls = new HashMap<>();

        for (HallTimeSlot slot : teacherAllocations) {
            allocationHalls.put(slot.timeSlot(), slot.hall());
        }

        Map<HallTimeSlot, SessionAllocation> sessionAllocations = this.schedule.getSessionAllocations();

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("schedule");

        ScheduleOutputWriter.setHoursLegend(sheet);

        int rowNumber = 1;

        for (DayOfWeek dayOfWeek : Schedule.SCHEDULED_DAYS_OF_WEEK) {
            HSSFRow row = sheet.createRow(rowNumber);
            row.setHeight((short)1000);
            rowNumber++;

            short cellOffset = 0;

            HSSFCell dayOfWeekCell = row.createCell(cellOffset);
            cellOffset++;

            dayOfWeekCell.setCellValue(new HSSFRichTextString(dayOfWeek.name()));

            int allocationGroupStartCell = cellOffset;
            CompulsoryCourse allocationGroupCourse = null;

            for (int hour = Schedule.START_HOUR; hour < Schedule.END_HOUR; hour++) {
                HSSFCell cell = row.createCell(cellOffset);
                cellOffset++;

                HSSFCellStyle style = workbook.createCellStyle();
                style.setWrapText(true);
                cell.setCellStyle(style);

                TimeSlot timeSlot = new TimeSlot(dayOfWeek, hour);
                Hall hall = allocationHalls.get(timeSlot);

                if (hall != null) {
                    SessionAllocation allocation = sessionAllocations.get(new HallTimeSlot(hall, timeSlot));
                    String[] parts = {
                        allocation.course().getStudentsStream().toString(),
                        allocation.course().getGroupNumber() == null ? "" : allocation.course().getGroupNumber().toString(),
                        allocation.course().getName(),
                        allocation.course().getSessionType().name(),
                        allocation.slot().hall().faculty(),
                        allocation.slot().hall().roomNumber()
                    };
                    HSSFRichTextString value = new HSSFRichTextString(String.join(", ", parts));
                    cell.setCellValue(value);

                    if (allocationGroupCourse != null && !allocationGroupCourse.equals(allocation.course())) {
                        sheet.addMergedRegion(new Region(
                            rowNumber - 1,
                            (short)allocationGroupStartCell,
                            rowNumber - 1,
                            (short)(cellOffset - 2)
                        ));
                        allocationGroupStartCell = cellOffset - 1;
                    }
                    allocationGroupCourse = allocation.course();
                } else {
                    if (allocationGroupCourse != null) {
                        sheet.addMergedRegion(new Region(
                            rowNumber - 1,
                            (short)allocationGroupStartCell,
                            rowNumber - 1,
                            (short)(cellOffset - 2)
                        ));
                    }
                    allocationGroupStartCell = cellOffset;
                    allocationGroupCourse = null;
                }
            }
        }

        String path = "../output/teachers/" + teacher.name() + ".xls";

        try (FileOutputStream out = new FileOutputStream(path)) {
            workbook.write(out);
            System.out.println(path + " written successfully on disk.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeHallSchedule(Hall hall) {
        Map<HallTimeSlot, SessionAllocation> sessionAllocations = this.schedule.getSessionAllocations();

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("schedule");

        ScheduleOutputWriter.setHoursLegend(sheet);

        int rowNumber = 1;

        for (DayOfWeek dayOfWeek : Schedule.SCHEDULED_DAYS_OF_WEEK) {
            HSSFRow row = sheet.createRow(rowNumber);
            row.setHeight((short)1000);
            rowNumber++;

            short cellOffset = 0;

            HSSFCell dayOfWeekCell = row.createCell(cellOffset);
            cellOffset++;

            dayOfWeekCell.setCellValue(new HSSFRichTextString(dayOfWeek.name()));

            int allocationGroupStartCell = cellOffset;
            CompulsoryCourse allocationGroupCourse = null;

            for (int hour = Schedule.START_HOUR; hour < Schedule.END_HOUR; hour++) {
                HSSFCell cell = row.createCell(cellOffset);
                cellOffset++;

                HSSFCellStyle style = workbook.createCellStyle();
                style.setWrapText(true);
                cell.setCellStyle(style);

                TimeSlot timeSlot = new TimeSlot(dayOfWeek, hour);
                HallTimeSlot hallTimeSlot = new HallTimeSlot(hall, timeSlot);
                SessionAllocation allocation = sessionAllocations.get(hallTimeSlot);

                if (allocation != null) {
                    String[] parts = {
                        allocation.course().getStudentsStream().toString(),
                        allocation.course().getGroupNumber() == null ? "" : allocation.course().getGroupNumber().toString(),
                        allocation.course().getName(),
                        allocation.course().getSessionType().name(),
                        allocation.course().getTeacher().name()
                    };
                    HSSFRichTextString value = new HSSFRichTextString(String.join(", ", parts));
                    cell.setCellValue(value);

                    if (allocationGroupCourse != null && !allocationGroupCourse.equals(allocation.course())) {
                        sheet.addMergedRegion(new Region(
                            rowNumber - 1,
                            (short)allocationGroupStartCell,
                            rowNumber - 1,
                            (short)(cellOffset - 2)
                        ));
                        allocationGroupStartCell = cellOffset - 1;
                    }
                    allocationGroupCourse = allocation.course();
                } else {
                    if (allocationGroupCourse != null) {
                        sheet.addMergedRegion(new Region(
                            rowNumber - 1,
                            (short)allocationGroupStartCell,
                            rowNumber - 1,
                            (short)(cellOffset - 2)
                        ));
                    }
                    allocationGroupStartCell = cellOffset;
                    allocationGroupCourse = null;
                }
            }
        }

        String path = "../output/halls/" + hall.faculty() + " " + hall.roomNumber() + ".xls";

        try (FileOutputStream out = new FileOutputStream(path)) {
            workbook.write(out);
            System.out.println(path + " written successfully on disk.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeStudentsStreamSchedule(StudentsStream studentsStream) {
        Map<HallTimeSlot, SessionAllocation> sessionAllocations = this.schedule.getSessionAllocations();
        Map<Integer, Map<TimeSlot, Hall>> groupsAllocationHalls = new HashMap<>();

        for (int groupNumber = 1; groupNumber <= studentsStream.groups(); groupNumber++) {
            StudentsGroup studentsGroup = new StudentsGroup(studentsStream, groupNumber);
            Set<HallTimeSlot> studentsGroupAllocations = this.schedule.getStudentsGroupsAllocations().get(studentsGroup);
            Map<TimeSlot, Hall> allocationHalls = new HashMap<>();
            for (HallTimeSlot slot : studentsGroupAllocations) {
                allocationHalls.put(slot.timeSlot(), slot.hall());
            }
            groupsAllocationHalls.put(groupNumber, allocationHalls);
        }

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("schedule");

        ScheduleOutputWriter.setHoursLegend(sheet);

        int rowNumber = 1;

        for (DayOfWeek dayOfWeek : Schedule.SCHEDULED_DAYS_OF_WEEK) {
            for (int groupNumber = 1; groupNumber <= studentsStream.groups(); groupNumber++) {
                HSSFRow row = sheet.createRow(rowNumber);
                row.setHeight((short)1000);
                rowNumber++;

                short cellOffset = 0;

                HSSFCell dayOfWeekCell = row.createCell(cellOffset);
                cellOffset++;

                HSSFCell groupNumberCell = row.createCell(cellOffset);
                cellOffset++;

                dayOfWeekCell.setCellValue(new HSSFRichTextString(dayOfWeek.name()));
                groupNumberCell.setCellValue(groupNumber);

                int allocationGroupStartCell = cellOffset;
                CompulsoryCourse allocationGroupCourse = null;

                for (int hour = Schedule.START_HOUR; hour < Schedule.END_HOUR; hour++) {
                    HSSFCell cell = row.createCell(cellOffset);
                    cellOffset++;

                    HSSFCellStyle style = workbook.createCellStyle();
                    style.setWrapText(true);
                    cell.setCellStyle(style);

                    TimeSlot timeSlot = new TimeSlot(dayOfWeek, hour);
                    Hall hall = groupsAllocationHalls.get(groupNumber).get(timeSlot);

                    if (hall != null) {
                        SessionAllocation allocation = sessionAllocations.get(new HallTimeSlot(hall, timeSlot));
                        String[] parts = {
                            allocation.course().getName(),
                            allocation.course().getSessionType().name(),
                            allocation.slot().hall().faculty(),
                            allocation.slot().hall().roomNumber(),
                            allocation.course().getTeacher().name()
                        };
                        HSSFRichTextString value = new HSSFRichTextString(String.join(", ", parts));
                        cell.setCellValue(value);

                        if (allocationGroupCourse != null && !allocationGroupCourse.equals(allocation.course())) {
                            sheet.addMergedRegion(new Region(
                                rowNumber - 1,
                                (short)allocationGroupStartCell,
                                rowNumber - 1,
                                (short)(cellOffset - 2)
                            ));
                            allocationGroupStartCell = cellOffset - 1;
                        }
                        allocationGroupCourse = allocation.course();
                    } else {
                        if (allocationGroupCourse != null) {
                            sheet.addMergedRegion(new Region(
                                rowNumber - 1,
                                (short)allocationGroupStartCell,
                                rowNumber - 1,
                                (short)(cellOffset - 2)
                            ));
                        }
                        allocationGroupStartCell = cellOffset;
                        allocationGroupCourse = null;
                    }
                }
            }
            sheet.addMergedRegion(new Region(rowNumber - studentsStream.groups(), (short)0, rowNumber - 1, (short)0));
        }

        String path = "../output/student_streams/" + studentsStream + ".xls";

        try (FileOutputStream out = new FileOutputStream(path)) {
            workbook.write(out);
            System.out.println(path + " written successfully on disk.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
