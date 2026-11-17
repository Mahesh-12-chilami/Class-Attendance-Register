package com.example.attendanceregister;

import java.io.Serializable;

/**
 * Model class representing a Subject belonging to a specific Semester.
 */
public class Subject implements Serializable {
    private int id;
    private String name;
    private String semester;

    public Subject() {
    }

    public Subject(int id, String name, String semester) {
        this.id = id;
        this.name = name;
        this.semester = semester;
    }

    public Subject(String name, String semester) {
        this.name = name;
        this.semester = semester;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    @Override
    public String toString() {
        return name;
    }
}
