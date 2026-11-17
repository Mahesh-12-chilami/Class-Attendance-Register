package com.example.attendanceregister;

import java.io.Serializable;

/**
 * Model class representing a Student.
 * Supports Semester (1 to 6) and Division (A1 to A6).
 * Implements Serializable to allow passing between Activities via Intents.
 */
public class Student implements Serializable {
    private int id;
    private String registerNumber;
    private String name;
    private String semester;
    private String division;

    public Student() {
    }

    public Student(int id, String registerNumber, String name, String semester, String division) {
        this.id = id;
        this.registerNumber = registerNumber;
        this.name = name;
        this.semester = semester;
        this.division = division;
    }

    public Student(String registerNumber, String name, String semester, String division) {
        this.registerNumber = registerNumber;
        this.name = name;
        this.semester = semester;
        this.division = division;
    }

    // Backward compatibility constructor
    public Student(int id, String registerNumber, String name, String section) {
        this.id = id;
        this.registerNumber = registerNumber;
        this.name = name;
        parseSection(section);
    }

    // Backward compatibility constructor
    public Student(String registerNumber, String name, String section) {
        this.registerNumber = registerNumber;
        this.name = name;
        parseSection(section);
    }

    private void parseSection(String section) {
        if (section != null && section.contains("-")) {
            String[] parts = section.split("-");
            this.semester = parts[0].trim();
            this.division = parts.length > 1 ? parts[1].trim() : "A1";
        } else {
            this.semester = "Semester 5";
            this.division = (section != null && !section.isEmpty()) ? section.trim() : "A1";
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSemester() {
        return (semester != null && !semester.isEmpty()) ? semester : "Semester 5";
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getDivision() {
        return (division != null && !division.isEmpty()) ? division : "A1";
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

    @Override
    public String toString() {
        return registerNumber + " - " + name + " (" + getSemester() + " " + getDivision() + ")";
    }
}
