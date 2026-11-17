package com.example.attendanceregister;

import java.io.Serializable;

/**
 * Model class representing an Attendance record.
 * Supports Semester (1 to 6) and Division (A1 to A6).
 */
public class Attendance implements Serializable {
    private int id;
    private int studentId;
    private int subjectId;
    private String semester;
    private String division;
    private String attendanceDate;
    private String period;
    private String status; // "Present" or "Absent"

    public Attendance() {
    }

    public Attendance(int id, int studentId, int subjectId, String semester, String division,
                      String attendanceDate, String period, String status) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.semester = semester;
        this.division = division;
        this.attendanceDate = attendanceDate;
        this.period = period;
        this.status = status;
    }

    public Attendance(int studentId, int subjectId, String semester, String division,
                      String attendanceDate, String period, String status) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.semester = semester;
        this.division = division;
        this.attendanceDate = attendanceDate;
        this.period = period;
        this.status = status;
    }

    // Backward compatibility constructor
    public Attendance(int id, int studentId, int subjectId, String section,
                      String attendanceDate, String period, String status) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.semester = "Semester 5";
        this.division = section;
        this.attendanceDate = attendanceDate;
        this.period = period;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
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

    // Backward compatibility getter & setter
    public String getSection() {
        return getDivision();
    }

    public void setSection(String section) {
        setDivision(section);
    }

    public String getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(String attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPresent() {
        return "Present".equalsIgnoreCase(status);
    }
}
