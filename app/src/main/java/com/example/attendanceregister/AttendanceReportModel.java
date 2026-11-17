package com.example.attendanceregister;

import java.io.Serializable;

/**
 * Model class used to hold aggregated attendance statistics for reports.
 * Computed using SQLite GROUP BY and COUNT queries.
 * Upgraded to support Semester (1 to 6), Division (A1 to A6), and Subject details.
 */
public class AttendanceReportModel implements Serializable {
    private int studentId;
    private String registerNumber;
    private String studentName;
    private String semester;
    private String division;
    private String subjectName;
    private int totalClasses;
    private int attendedClasses;
    private int absentClasses;
    private double percentage;

    public AttendanceReportModel() {
    }

    public AttendanceReportModel(int studentId, String registerNumber, String studentName,
                                 String semester, String division, String subjectName,
                                 int totalClasses, int attendedClasses, int absentClasses, double percentage) {
        this.studentId = studentId;
        this.registerNumber = registerNumber;
        this.studentName = studentName;
        this.semester = semester;
        this.division = division;
        this.subjectName = subjectName;
        this.totalClasses = totalClasses;
        this.attendedClasses = attendedClasses;
        this.absentClasses = absentClasses;
        this.percentage = percentage;
    }

    // Backward compatibility constructor
    public AttendanceReportModel(int studentId, String registerNumber, String studentName, String section,
                                 int totalClasses, int attendedClasses, int absentClasses, double percentage) {
        this.studentId = studentId;
        this.registerNumber = registerNumber;
        this.studentName = studentName;
        this.semester = "Semester 5";
        this.division = section;
        this.subjectName = "";
        this.totalClasses = totalClasses;
        this.attendedClasses = attendedClasses;
        this.absentClasses = absentClasses;
        this.percentage = percentage;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSemester() {
        return semester != null ? semester : "Semester 5";
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getDivision() {
        return division != null ? division : "A1";
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getSubjectName() {
        return subjectName != null ? subjectName : "";
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    // Backward compatibility getter & setter
    public String getSection() {
        return getDivision();
    }

    public void setSection(String section) {
        setDivision(section);
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public int getAttendedClasses() {
        return attendedClasses;
    }

    public void setAttendedClasses(int attendedClasses) {
        this.attendedClasses = attendedClasses;
    }

    public int getAbsentClasses() {
        return absentClasses;
    }

    public void setAbsentClasses(int absentClasses) {
        this.absentClasses = absentClasses;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public boolean isShortage() {
        return totalClasses > 0 && percentage < 75.0;
    }

    public String getFormattedPercentage() {
        if (totalClasses == 0) {
            return "N/A";
        }
        return String.format(java.util.Locale.US, "%.1f%%", percentage);
    }
}
