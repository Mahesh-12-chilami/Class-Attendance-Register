package com.example.attendanceregister;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SQLiteOpenHelper implementation for Class Attendance Register.
 * Upgraded to Version 3 to support:
 * - Semester-wise Subject Management (Separate subjects for Semesters 1 to 6)
 * - Composite UNIQUE(semester, subject_name) constraint on Subjects
 * - Safe onUpgrade migration preserving existing data
 * - Permanent Session Locking via AttendanceSessions table
 * - Strict database-level unique constraint preventing duplicate sessions
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";
    public static final String DATABASE_NAME = "AttendanceRegister.db";
    public static final int DATABASE_VERSION = 3; // Upgraded version for semester-wise subjects

    // Table 1: Faculty
    public static final String TABLE_FACULTY = "Faculty";
    public static final String COL_FACULTY_ID = "faculty_id";
    public static final String COL_FACULTY_NAME = "faculty_name";
    public static final String COL_FACULTY_EMAIL = "email";
    public static final String COL_FACULTY_PASSWORD = "password";

    // Table 2: Students
    public static final String TABLE_STUDENTS = "Students";
    public static final String COL_STUDENT_ID = "student_id";
    public static final String COL_REGISTER_NUMBER = "register_number";
    public static final String COL_STUDENT_NAME = "student_name";
    public static final String COL_STUDENT_SEMESTER = "semester";
    public static final String COL_STUDENT_DIVISION = "division";
    public static final String COL_STUDENT_SECTION = "section"; // Kept for backward compatibility

    // Table 3: Subjects (Semester-wise)
    public static final String TABLE_SUBJECTS = "Subjects";
    public static final String COL_SUBJECT_ID = "subject_id";
    public static final String COL_SUBJECT_NAME = "subject_name";
    public static final String COL_SUBJECT_SEMESTER = "semester";

    // Table 4: Attendance
    public static final String TABLE_ATTENDANCE = "Attendance";
    public static final String COL_ATTENDANCE_ID = "attendance_id";
    public static final String COL_ATT_STUDENT_ID = "student_id";
    public static final String COL_ATT_SUBJECT_ID = "subject_id";
    public static final String COL_ATT_SEMESTER = "semester";
    public static final String COL_ATT_DIVISION = "division";
    public static final String COL_ATT_SECTION = "section"; // Kept for backward compatibility
    public static final String COL_ATT_DATE = "attendance_date";
    public static final String COL_ATT_PERIOD = "period";
    public static final String COL_ATT_STATUS = "status"; // "Present" or "Absent"

    // Table 5: AttendanceSessions (Permanent Session Lock)
    public static final String TABLE_SESSIONS = "AttendanceSessions";
    public static final String COL_SESSION_ID = "session_id";
    public static final String COL_SESSION_SEMESTER = "semester";
    public static final String COL_SESSION_DIVISION = "division";
    public static final String COL_SESSION_SUBJECT_ID = "subject_id";
    public static final String COL_SESSION_DATE = "attendance_date";
    public static final String COL_SESSION_PERIOD = "period";
    public static final String COL_SESSION_STATUS = "submission_status"; // "LOCKED"
    public static final String COL_SESSION_SUBMITTED_AT = "submitted_at";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables for version " + DATABASE_VERSION);

        // 1. Create Faculty Table
        String createFacultyTable = "CREATE TABLE " + TABLE_FACULTY + " ("
                + COL_FACULTY_ID + " TEXT PRIMARY KEY, "
                + COL_FACULTY_NAME + " TEXT NOT NULL, "
                + COL_FACULTY_EMAIL + " TEXT NOT NULL, "
                + COL_FACULTY_PASSWORD + " TEXT NOT NULL"
                + ");";
        db.execSQL(createFacultyTable);

        // 2. Create Students Table with Semester & Division
        String createStudentsTable = "CREATE TABLE " + TABLE_STUDENTS + " ("
                + COL_STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_REGISTER_NUMBER + " TEXT UNIQUE NOT NULL, "
                + COL_STUDENT_NAME + " TEXT NOT NULL, "
                + COL_STUDENT_SEMESTER + " TEXT NOT NULL, "
                + COL_STUDENT_DIVISION + " TEXT NOT NULL, "
                + COL_STUDENT_SECTION + " TEXT"
                + ");";
        db.execSQL(createStudentsTable);

        // 3. Create Subjects Table with Semester & composite UNIQUE constraint
        String createSubjectsTable = "CREATE TABLE " + TABLE_SUBJECTS + " ("
                + COL_SUBJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SUBJECT_NAME + " TEXT NOT NULL, "
                + COL_SUBJECT_SEMESTER + " TEXT NOT NULL, "
                + "UNIQUE (" + COL_SUBJECT_SEMESTER + ", " + COL_SUBJECT_NAME + ")"
                + ");";
        db.execSQL(createSubjectsTable);

        // 4. Create Attendance Table with Semester & Division
        String createAttendanceTable = "CREATE TABLE " + TABLE_ATTENDANCE + " ("
                + COL_ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ATT_STUDENT_ID + " INTEGER NOT NULL, "
                + COL_ATT_SUBJECT_ID + " INTEGER NOT NULL, "
                + COL_ATT_SEMESTER + " TEXT NOT NULL, "
                + COL_ATT_DIVISION + " TEXT NOT NULL, "
                + COL_ATT_SECTION + " TEXT, "
                + COL_ATT_DATE + " TEXT NOT NULL, "
                + COL_ATT_PERIOD + " TEXT NOT NULL, "
                + COL_ATT_STATUS + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_ATT_STUDENT_ID + ") REFERENCES " + TABLE_STUDENTS + "(" + COL_STUDENT_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY(" + COL_ATT_SUBJECT_ID + ") REFERENCES " + TABLE_SUBJECTS + "(" + COL_SUBJECT_ID + ") ON DELETE CASCADE"
                + ");";
        db.execSQL(createAttendanceTable);

        // 5. Create AttendanceSessions Table with UNIQUE session constraint
        String createSessionsTable = "CREATE TABLE " + TABLE_SESSIONS + " ("
                + COL_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SESSION_SEMESTER + " TEXT NOT NULL, "
                + COL_SESSION_DIVISION + " TEXT NOT NULL, "
                + COL_SESSION_SUBJECT_ID + " INTEGER NOT NULL, "
                + COL_SESSION_DATE + " TEXT NOT NULL, "
                + COL_SESSION_PERIOD + " TEXT NOT NULL, "
                + COL_SESSION_STATUS + " TEXT NOT NULL, "
                + COL_SESSION_SUBMITTED_AT + " TEXT NOT NULL, "
                + "UNIQUE (" + COL_SESSION_SEMESTER + ", " + COL_SESSION_DIVISION + ", "
                + COL_SESSION_SUBJECT_ID + ", " + COL_SESSION_DATE + ", " + COL_SESSION_PERIOD + ")"
                + ");";
        db.execSQL(createSessionsTable);

        // Populate semester-wise initial Subjects
        insertInitialSemesterSubjects(db);

        // Populate initial Students across Semesters and Divisions
        insertInitialStudents(db);

        // Populate default demo faculty
        ContentValues cvFac = new ContentValues();
        cvFac.put(COL_FACULTY_ID, "FAC01");
        cvFac.put(COL_FACULTY_NAME, "Prof. Sharma");
        cvFac.put(COL_FACULTY_EMAIL, "faculty@college.edu");
        cvFac.put(COL_FACULTY_PASSWORD, "admin123");
        db.insert(TABLE_FACULTY, null, cvFac);

        Log.d(TAG, "Database initialized successfully with Version 3 schema.");
    }

    /**
     * Seeds independent subjects for each of the six semesters.
     */
    private void insertInitialSemesterSubjects(SQLiteDatabase db) {
        String[][] semesterSubjects = {
                // Semester 1 Subjects
                {"Semester 1", "C Programming"},
                {"Semester 1", "Digital Electronics"},
                {"Semester 1", "Discrete Mathematics"},
                {"Semester 1", "Communication Skills"},

                // Semester 2 Subjects
                {"Semester 2", "Data Structures using C"},
                {"Semester 2", "Computer Organization"},
                {"Semester 2", "Database Concepts"},
                {"Semester 2", "Numerical Analysis"},

                // Semester 3 Subjects
                {"Semester 3", "Object Oriented Programming (C++)"},
                {"Semester 3", "Database Management System"},
                {"Semester 3", "Operating System Concepts"},
                {"Semester 3", "Software Engineering"},

                // Semester 4 Subjects
                {"Semester 4", "Java Programming"},
                {"Semester 4", "Computer Networks"},
                {"Semester 4", "Python Programming"},
                {"Semester 4", "Design and Analysis of Algorithms"},

                // Semester 5 Subjects
                {"Semester 5", "Advanced Java Programming"},
                {"Semester 5", "Android App Development"},
                {"Semester 5", "Web Technologies (HTML/CSS/JS)"},
                {"Semester 5", "Information Security"},

                // Semester 6 Subjects
                {"Semester 6", "Cloud Computing"},
                {"Semester 6", "Machine Learning Basics"},
                {"Semester 6", "Mobile Computing"},
                {"Semester 6", "Project Work / Seminar"}
        };

        for (String[] pair : semesterSubjects) {
            ContentValues cv = new ContentValues();
            cv.put(COL_SUBJECT_SEMESTER, pair[0]);
            cv.put(COL_SUBJECT_NAME, pair[1]);
            db.insertWithOnConflict(TABLE_SUBJECTS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    private void insertInitialStudents(SQLiteDatabase db) {
        String[][] students = {
                // Semester 5 - Division A1 (Minimum 4 students)
                {"21BCA01", "Aarav Sharma", "Semester 5", "A1"},
                {"21BCA02", "Ananya Patel", "Semester 5", "A1"},
                {"21BCA03", "Rohan Verma", "Semester 5", "A1"},
                {"21BCA04", "Sneha Iyer", "Semester 5", "A1"},

                // Semester 5 - Division A2 (Separate division test)
                {"21BCA05", "Priya Nair", "Semester 5", "A2"},
                {"21BCA06", "Vikram Rao", "Semester 5", "A2"},
                {"21BCA07", "Neha Gupta", "Semester 5", "A2"},
                {"21BCA08", "Aditya Singh", "Semester 5", "A2"},

                // Semester 5 - Division A3
                {"21BCA09", "Deepa Menon", "Semester 5", "A3"},
                {"21BCA10", "Rahul Deshmukh", "Semester 5", "A3"},

                // Semester 4 - Division A1
                {"22BCA01", "Arjun Nair", "Semester 4", "A1"},
                {"22BCA02", "Bhavna Joshi", "Semester 4", "A1"},

                // Semester 3 - Division A1
                {"23BCA01", "Chetan Das", "Semester 3", "A1"},
                {"23BCA02", "Divya Saxena", "Semester 3", "A1"},

                // Semester 6 - Division A1
                {"20BCA01", "Esha Roy", "Semester 6", "A1"},
                {"20BCA02", "Farhan Khan", "Semester 6", "A1"}
        };

        for (String[] stu : students) {
            ContentValues cv = new ContentValues();
            cv.put(COL_REGISTER_NUMBER, stu[0]);
            cv.put(COL_STUDENT_NAME, stu[1]);
            cv.put(COL_STUDENT_SEMESTER, stu[2]);
            cv.put(COL_STUDENT_DIVISION, stu[3]);
            cv.put(COL_STUDENT_SECTION, stu[3]);
            db.insertWithOnConflict(TABLE_STUDENTS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    /**
     * Safe database upgrade without losing existing records.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + " safely...");

        if (oldVersion < 2) {
            try {
                if (!isColumnExists(db, TABLE_STUDENTS, COL_STUDENT_SEMESTER)) {
                    db.execSQL("ALTER TABLE " + TABLE_STUDENTS + " ADD COLUMN " + COL_STUDENT_SEMESTER + " TEXT DEFAULT 'Semester 5'");
                }
                if (!isColumnExists(db, TABLE_STUDENTS, COL_STUDENT_DIVISION)) {
                    db.execSQL("ALTER TABLE " + TABLE_STUDENTS + " ADD COLUMN " + COL_STUDENT_DIVISION + " TEXT DEFAULT 'A1'");
                }

                if (!isColumnExists(db, TABLE_ATTENDANCE, COL_ATT_SEMESTER)) {
                    db.execSQL("ALTER TABLE " + TABLE_ATTENDANCE + " ADD COLUMN " + COL_ATT_SEMESTER + " TEXT DEFAULT 'Semester 5'");
                }
                if (!isColumnExists(db, TABLE_ATTENDANCE, COL_ATT_DIVISION)) {
                    db.execSQL("ALTER TABLE " + TABLE_ATTENDANCE + " ADD COLUMN " + COL_ATT_DIVISION + " TEXT DEFAULT 'A1'");
                }

                String createSessionsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_SESSIONS + " ("
                        + COL_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COL_SESSION_SEMESTER + " TEXT NOT NULL, "
                        + COL_SESSION_DIVISION + " TEXT NOT NULL, "
                        + COL_SESSION_SUBJECT_ID + " INTEGER NOT NULL, "
                        + COL_SESSION_DATE + " TEXT NOT NULL, "
                        + COL_SESSION_PERIOD + " TEXT NOT NULL, "
                        + COL_SESSION_STATUS + " TEXT NOT NULL, "
                        + COL_SESSION_SUBMITTED_AT + " TEXT NOT NULL, "
                        + "UNIQUE (" + COL_SESSION_SEMESTER + ", " + COL_SESSION_DIVISION + ", "
                        + COL_SESSION_SUBJECT_ID + ", " + COL_SESSION_DATE + ", " + COL_SESSION_PERIOD + ")"
                        + ");";
                db.execSQL(createSessionsTable);
            } catch (Exception e) {
                Log.e(TAG, "Error during v2 migration", e);
            }
        }

        if (oldVersion < 3) {
            try {
                // Ensure Subjects table has semester column
                if (!isColumnExists(db, TABLE_SUBJECTS, COL_SUBJECT_SEMESTER)) {
                    db.execSQL("ALTER TABLE " + TABLE_SUBJECTS + " ADD COLUMN " + COL_SUBJECT_SEMESTER + " TEXT DEFAULT 'Semester 5'");
                }
                // Seed subjects for all six semesters
                insertInitialSemesterSubjects(db);
                Log.d(TAG, "Database migrated to Version 3: Semester-wise subjects populated.");
            } catch (Exception e) {
                Log.e(TAG, "Error during v3 migration", e);
            }
        }
    }

    private boolean isColumnExists(SQLiteDatabase db, String table, String column) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String colName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    if (column.equalsIgnoreCase(colName)) {
                        return true;
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking column existence", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return false;
    }

    // ==========================================
    // FACULTY OPERATIONS
    // ==========================================

    public boolean insertFaculty(String id, String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FACULTY_ID, id.trim());
        cv.put(COL_FACULTY_NAME, name.trim());
        cv.put(COL_FACULTY_EMAIL, email.trim().toLowerCase());
        cv.put(COL_FACULTY_PASSWORD, password);

        long result = db.insert(TABLE_FACULTY, null, cv);
        return result != -1;
    }

    public boolean isFacultyExists(String facultyId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FACULTY, new String[]{COL_FACULTY_ID},
                COL_FACULTY_ID + " = ?", new String[]{facultyId.trim()},
                null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public String checkFacultyLogin(String idOrEmail, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_FACULTY_NAME + " FROM " + TABLE_FACULTY
                + " WHERE (" + COL_FACULTY_ID + " = ? OR " + COL_FACULTY_EMAIL + " = ?) AND " + COL_FACULTY_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{idOrEmail.trim(), idOrEmail.trim().toLowerCase(), password});
        String facultyName = null;
        if (cursor != null && cursor.moveToFirst()) {
            facultyName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FACULTY_NAME));
            cursor.close();
        }
        return facultyName;
    }

    // ==========================================
    // STUDENT OPERATIONS (SEMESTER & DIVISION)
    // ==========================================

    public boolean insertStudent(String regNo, String name, String semester, String division) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_REGISTER_NUMBER, regNo.trim().toUpperCase());
        cv.put(COL_STUDENT_NAME, name.trim());
        cv.put(COL_STUDENT_SEMESTER, semester.trim());
        cv.put(COL_STUDENT_DIVISION, division.trim().toUpperCase());
        cv.put(COL_STUDENT_SECTION, division.trim().toUpperCase());

        long result = db.insert(TABLE_STUDENTS, null, cv);
        return result != -1;
    }

    public boolean updateStudent(int id, String regNo, String name, String semester, String division) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_REGISTER_NUMBER, regNo.trim().toUpperCase());
        cv.put(COL_STUDENT_NAME, name.trim());
        cv.put(COL_STUDENT_SEMESTER, semester.trim());
        cv.put(COL_STUDENT_DIVISION, division.trim().toUpperCase());
        cv.put(COL_STUDENT_SECTION, division.trim().toUpperCase());

        int affected = db.update(TABLE_STUDENTS, cv, COL_STUDENT_ID + " = ?", new String[]{String.valueOf(id)});
        return affected > 0;
    }

    public boolean deleteStudent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ATTENDANCE, COL_ATT_STUDENT_ID + " = ?", new String[]{String.valueOf(id)});
        int affected = db.delete(TABLE_STUDENTS, COL_STUDENT_ID + " = ?", new String[]{String.valueOf(id)});
        return affected > 0;
    }

    public boolean isRegisterNumberExists(String regNo, int excludeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_STUDENT_ID + " FROM " + TABLE_STUDENTS
                + " WHERE " + COL_REGISTER_NUMBER + " = ? AND " + COL_STUDENT_ID + " != ?";
        Cursor cursor = db.rawQuery(query, new String[]{regNo.trim().toUpperCase(), String.valueOf(excludeId)});
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STUDENTS, null, null, null, null, null,
                COL_STUDENT_SEMESTER + " ASC, " + COL_STUDENT_DIVISION + " ASC, " + COL_REGISTER_NUMBER + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_STUDENT_ID));
                String reg = cursor.getString(cursor.getColumnIndexOrThrow(COL_REGISTER_NUMBER));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_NAME));
                String sem = cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_SEMESTER));
                String div = cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_DIVISION));
                list.add(new Student(id, reg, name, sem, div));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Student> getStudentsBySemesterAndDivision(String semester, String division) {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STUDENTS, null,
                COL_STUDENT_SEMESTER + " = ? AND " + COL_STUDENT_DIVISION + " = ?",
                new String[]{semester.trim(), division.trim().toUpperCase()},
                null, null, COL_REGISTER_NUMBER + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_STUDENT_ID));
                String reg = cursor.getString(cursor.getColumnIndexOrThrow(COL_REGISTER_NUMBER));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_NAME));
                String sem = cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_SEMESTER));
                String div = cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_DIVISION));
                list.add(new Student(id, reg, name, sem, div));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Student> searchStudents(String query, String semesterFilter, String divisionFilter) {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder queryBuilder = new StringBuilder();
        List<String> args = new ArrayList<>();

        queryBuilder.append("(").append(COL_STUDENT_NAME).append(" LIKE ? OR ")
                .append(COL_REGISTER_NUMBER).append(" LIKE ?)");
        String searchParam = "%" + query.trim() + "%";
        args.add(searchParam);
        args.add(searchParam);

        if (semesterFilter != null && !semesterFilter.equalsIgnoreCase("All Semesters") && !semesterFilter.isEmpty()) {
            queryBuilder.append(" AND ").append(COL_STUDENT_SEMESTER).append(" = ?");
            args.add(semesterFilter.trim());
        }

        if (divisionFilter != null && !divisionFilter.equalsIgnoreCase("All Divisions") && !divisionFilter.isEmpty()) {
            queryBuilder.append(" AND ").append(COL_STUDENT_DIVISION).append(" = ?");
            args.add(divisionFilter.trim().toUpperCase());
        }

        Cursor cursor = db.query(TABLE_STUDENTS, null,
                queryBuilder.toString(),
                args.toArray(new String[0]),
                null, null, COL_REGISTER_NUMBER + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_STUDENT_ID));
                String reg = cursor.getString(cursor.getColumnIndexOrThrow(COL_REGISTER_NUMBER));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_NAME));
                String sem = cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_SEMESTER));
                String div = cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_DIVISION));
                list.add(new Student(id, reg, name, sem, div));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // ==========================================
    // SEMESTER-WISE SUBJECT OPERATIONS
    // ==========================================

    /**
     * Retrieves subject names strictly belonging to the given Semester.
     * Guarantees Semester 1 subjects are never shown for Semester 2, etc.
     */
    public List<String> getSubjectNamesBySemester(String semester) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SUBJECTS, new String[]{COL_SUBJECT_NAME},
                COL_SUBJECT_SEMESTER + " = ?", new String[]{semester.trim()},
                null, null, COL_SUBJECT_NAME + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(cursor.getColumnIndexOrThrow(COL_SUBJECT_NAME)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    /**
     * Retrieves full Subject objects strictly for the given Semester.
     */
    public List<Subject> getSubjectsBySemester(String semester) {
        List<Subject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = null;
        String[] selectionArgs = null;

        if (semester != null && !semester.equalsIgnoreCase("All Semesters") && !semester.isEmpty()) {
            selection = COL_SUBJECT_SEMESTER + " = ?";
            selectionArgs = new String[]{semester.trim()};
        }

        Cursor cursor = db.query(TABLE_SUBJECTS, null,
                selection, selectionArgs,
                null, null, COL_SUBJECT_SEMESTER + " ASC, " + COL_SUBJECT_NAME + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SUBJECT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_SUBJECT_NAME));
                String sem = cursor.getString(cursor.getColumnIndexOrThrow(COL_SUBJECT_SEMESTER));
                list.add(new Subject(id, name, sem));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<String> getAllSubjects() {
        return getSubjectNamesBySemester("Semester 5");
    }

    public int getSubjectIdByNameAndSemester(String name, String semester) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SUBJECTS, new String[]{COL_SUBJECT_ID},
                COL_SUBJECT_NAME + " = ? AND " + COL_SUBJECT_SEMESTER + " = ?",
                new String[]{name.trim(), semester.trim()},
                null, null, null);
        int subId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            subId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SUBJECT_ID));
            cursor.close();
        }
        return subId;
    }

    public int getSubjectIdByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SUBJECTS, new String[]{COL_SUBJECT_ID},
                COL_SUBJECT_NAME + " = ?", new String[]{name.trim()},
                null, null, null);
        int subId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            subId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SUBJECT_ID));
            cursor.close();
        }
        return subId;
    }

    public String getSubjectNameById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SUBJECTS, new String[]{COL_SUBJECT_NAME},
                COL_SUBJECT_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
        String name = "";
        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_SUBJECT_NAME));
            cursor.close();
        }
        return name;
    }

    public boolean insertSubject(String name, String semester) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SUBJECT_NAME, name.trim());
        cv.put(COL_SUBJECT_SEMESTER, semester.trim());
        long result = db.insert(TABLE_SUBJECTS, null, cv);
        return result != -1;
    }

    public boolean updateSubject(int id, String name, String semester) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SUBJECT_NAME, name.trim());
        cv.put(COL_SUBJECT_SEMESTER, semester.trim());
        int affected = db.update(TABLE_SUBJECTS, cv, COL_SUBJECT_ID + " = ?", new String[]{String.valueOf(id)});
        return affected > 0;
    }

    public boolean isSubjectExists(String name, String semester, int excludeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_SUBJECT_ID + " FROM " + TABLE_SUBJECTS
                + " WHERE " + COL_SUBJECT_NAME + " = ? AND " + COL_SUBJECT_SEMESTER + " = ? AND " + COL_SUBJECT_ID + " != ?";
        Cursor cursor = db.rawQuery(query, new String[]{name.trim(), semester.trim(), String.valueOf(excludeId)});
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public boolean hasSubjectAttendanceRecords(int subjectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ATTENDANCE, new String[]{COL_ATTENDANCE_ID},
                COL_ATT_SUBJECT_ID + " = ?", new String[]{String.valueOf(subjectId)},
                null, null, null);
        boolean hasRecords = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return hasRecords;
    }

    public boolean deleteSubject(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int affected = db.delete(TABLE_SUBJECTS, COL_SUBJECT_ID + " = ?", new String[]{String.valueOf(id)});
        return affected > 0;
    }

    // ==========================================
    // PERMANENT SESSION LOCK OPERATIONS
    // ==========================================

    public boolean isSessionLocked(String semester, String division, int subjectId, String date, String period) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SESSIONS, new String[]{COL_SESSION_ID},
                COL_SESSION_SEMESTER + " = ? AND " + COL_SESSION_DIVISION + " = ? AND "
                        + COL_SESSION_SUBJECT_ID + " = ? AND " + COL_SESSION_DATE + " = ? AND "
                        + COL_SESSION_PERIOD + " = ? AND " + COL_SESSION_STATUS + " = 'LOCKED'",
                new String[]{semester.trim(), division.trim().toUpperCase(), String.valueOf(subjectId), date.trim(), period.trim()},
                null, null, null);
        boolean locked = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();

        if (locked) return true;

        Cursor attCursor = db.query(TABLE_ATTENDANCE, new String[]{COL_ATTENDANCE_ID},
                COL_ATT_SEMESTER + " = ? AND " + COL_ATT_DIVISION + " = ? AND "
                        + COL_ATT_SUBJECT_ID + " = ? AND " + COL_ATT_DATE + " = ? AND "
                        + COL_ATT_PERIOD + " = ?",
                new String[]{semester.trim(), division.trim().toUpperCase(), String.valueOf(subjectId), date.trim(), period.trim()},
                null, null, null);
        boolean attExists = (attCursor != null && attCursor.getCount() > 0);
        if (attCursor != null) attCursor.close();

        return attExists;
    }

    public String getSessionLockDetails(String semester, String division, int subjectId, String date, String period) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SESSIONS, new String[]{COL_SESSION_SUBMITTED_AT},
                COL_SESSION_SEMESTER + " = ? AND " + COL_SESSION_DIVISION + " = ? AND "
                        + COL_SESSION_SUBJECT_ID + " = ? AND " + COL_SESSION_DATE + " = ? AND "
                        + COL_SESSION_PERIOD + " = ?",
                new String[]{semester.trim(), division.trim().toUpperCase(), String.valueOf(subjectId), date.trim(), period.trim()},
                null, null, null);
        String submittedAt = "Submitted";
        if (cursor != null && cursor.moveToFirst()) {
            submittedAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_SESSION_SUBMITTED_AT));
            cursor.close();
        }
        return submittedAt;
    }

    public boolean lockAndSaveSessionAttendance(String semester, String division, int subjectId,
                                                String date, String period,
                                                List<Student> students, Map<Integer, Boolean> attendanceMap) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (isSessionLocked(semester, division, subjectId, date, period)) {
            Log.w(TAG, "Attempted to save already locked session! Aborting.");
            return false;
        }

        db.beginTransaction();
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());

            for (Student student : students) {
                boolean isPresent = Boolean.TRUE.equals(attendanceMap.get(student.getId()));
                ContentValues cv = new ContentValues();
                cv.put(COL_ATT_STUDENT_ID, student.getId());
                cv.put(COL_ATT_SUBJECT_ID, subjectId);
                cv.put(COL_ATT_SEMESTER, semester.trim());
                cv.put(COL_ATT_DIVISION, division.trim().toUpperCase());
                cv.put(COL_ATT_SECTION, division.trim().toUpperCase());
                cv.put(COL_ATT_DATE, date.trim());
                cv.put(COL_ATT_PERIOD, period.trim());
                cv.put(COL_ATT_STATUS, isPresent ? "Present" : "Absent");

                long attId = db.insertOrThrow(TABLE_ATTENDANCE, null, cv);
                if (attId == -1) {
                    throw new RuntimeException("Failed to insert student attendance record: " + student.getRegisterNumber());
                }
            }

            ContentValues sessionCv = new ContentValues();
            sessionCv.put(COL_SESSION_SEMESTER, semester.trim());
            sessionCv.put(COL_SESSION_DIVISION, division.trim().toUpperCase());
            sessionCv.put(COL_SESSION_SUBJECT_ID, subjectId);
            sessionCv.put(COL_SESSION_DATE, date.trim());
            sessionCv.put(COL_SESSION_PERIOD, period.trim());
            sessionCv.put(COL_SESSION_STATUS, "LOCKED");
            sessionCv.put(COL_SESSION_SUBMITTED_AT, timestamp);

            long sessionId = db.insertOrThrow(TABLE_SESSIONS, null, sessionCv);
            if (sessionId == -1) {
                throw new RuntimeException("Failed to lock attendance session!");
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Session permanently locked: " + semester + " " + division + " Subject:" + subjectId + " " + date + " " + period);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Transaction failed while locking attendance session. Rolled back.", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public Map<Integer, String> getSessionAttendanceStatus(String semester, String division, int subjectId, String date, String period) {
        Map<Integer, String> map = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ATTENDANCE,
                new String[]{COL_ATT_STUDENT_ID, COL_ATT_STATUS},
                COL_ATT_SEMESTER + " = ? AND " + COL_ATT_DIVISION + " = ? AND "
                        + COL_ATT_SUBJECT_ID + " = ? AND " + COL_ATT_DATE + " = ? AND " + COL_ATT_PERIOD + " = ?",
                new String[]{semester.trim(), division.trim().toUpperCase(), String.valueOf(subjectId), date.trim(), period.trim()},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int studentId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ATT_STUDENT_ID));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_ATT_STATUS));
                map.put(studentId, status);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return map;
    }

    // ==========================================
    // ADVANCED ATTENDANCE REPORT QUERIES
    // ==========================================

    public List<AttendanceReportModel> getAdvancedAttendanceReport(String semesterFilter, String divisionFilter,
                                                                   int subjectId, String dateFilter) {
        List<AttendanceReportModel> reportList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        List<Student> students;
        boolean hasSemester = semesterFilter != null && !semesterFilter.equalsIgnoreCase("All Semesters") && !semesterFilter.isEmpty();
        boolean hasDivision = divisionFilter != null && !divisionFilter.equalsIgnoreCase("All Divisions") && !divisionFilter.isEmpty();

        if (hasSemester && hasDivision) {
            students = getStudentsBySemesterAndDivision(semesterFilter, divisionFilter);
        } else if (hasSemester) {
            students = new ArrayList<>();
            Cursor cur = db.query(TABLE_STUDENTS, null, COL_STUDENT_SEMESTER + " = ?", new String[]{semesterFilter.trim()}, null, null, COL_REGISTER_NUMBER + " ASC");
            if (cur != null && cur.moveToFirst()) {
                do {
                    students.add(new Student(
                            cur.getInt(cur.getColumnIndexOrThrow(COL_STUDENT_ID)),
                            cur.getString(cur.getColumnIndexOrThrow(COL_REGISTER_NUMBER)),
                            cur.getString(cur.getColumnIndexOrThrow(COL_STUDENT_NAME)),
                            cur.getString(cur.getColumnIndexOrThrow(COL_STUDENT_SEMESTER)),
                            cur.getString(cur.getColumnIndexOrThrow(COL_STUDENT_DIVISION))
                    ));
                } while (cur.moveToNext());
                cur.close();
            }
        } else if (hasDivision) {
            students = new ArrayList<>();
            Cursor cur = db.query(TABLE_STUDENTS, null, COL_STUDENT_DIVISION + " = ?", new String[]{divisionFilter.trim().toUpperCase()}, null, null, COL_REGISTER_NUMBER + " ASC");
            if (cur != null && cur.moveToFirst()) {
                do {
                    students.add(new Student(
                            cur.getInt(cur.getColumnIndexOrThrow(COL_STUDENT_ID)),
                            cur.getString(cur.getColumnIndexOrThrow(COL_REGISTER_NUMBER)),
                            cur.getString(cur.getColumnIndexOrThrow(COL_STUDENT_NAME)),
                            cur.getString(cur.getColumnIndexOrThrow(COL_STUDENT_SEMESTER)),
                            cur.getString(cur.getColumnIndexOrThrow(COL_STUDENT_DIVISION))
                    ));
                } while (cur.moveToNext());
                cur.close();
            }
        } else {
            students = getAllStudents();
        }

        String subjectName = getSubjectNameById(subjectId);

        StringBuilder sessionCountSql = new StringBuilder("SELECT COUNT(DISTINCT " + COL_ATT_DATE + " || '_' || " + COL_ATT_PERIOD + ") AS total_sessions FROM " + TABLE_ATTENDANCE + " WHERE " + COL_ATT_SUBJECT_ID + " = ?");
        List<String> sessionArgs = new ArrayList<>();
        sessionArgs.add(String.valueOf(subjectId));

        if (hasSemester) {
            sessionCountSql.append(" AND ").append(COL_ATT_SEMESTER).append(" = ?");
            sessionArgs.add(semesterFilter.trim());
        }
        if (hasDivision) {
            sessionCountSql.append(" AND ").append(COL_ATT_DIVISION).append(" = ?");
            sessionArgs.add(divisionFilter.trim().toUpperCase());
        }
        if (dateFilter != null && !dateFilter.equalsIgnoreCase("All Dates") && !dateFilter.isEmpty()) {
            sessionCountSql.append(" AND ").append(COL_ATT_DATE).append(" = ?");
            sessionArgs.add(dateFilter.trim());
        }

        int totalConducted = 0;
        Cursor sessionCursor = db.rawQuery(sessionCountSql.toString(), sessionArgs.toArray(new String[0]));
        if (sessionCursor != null && sessionCursor.moveToFirst()) {
            totalConducted = sessionCursor.getInt(sessionCursor.getColumnIndexOrThrow("total_sessions"));
            sessionCursor.close();
        }

        for (Student student : students) {
            StringBuilder statsSql = new StringBuilder("SELECT " + COL_ATT_STATUS + ", COUNT(" + COL_ATTENDANCE_ID + ") AS status_count "
                    + "FROM " + TABLE_ATTENDANCE + " "
                    + "WHERE " + COL_ATT_STUDENT_ID + " = ? AND " + COL_ATT_SUBJECT_ID + " = ?");
            List<String> statsArgs = new ArrayList<>();
            statsArgs.add(String.valueOf(student.getId()));
            statsArgs.add(String.valueOf(subjectId));

            if (dateFilter != null && !dateFilter.equalsIgnoreCase("All Dates") && !dateFilter.isEmpty()) {
                statsSql.append(" AND ").append(COL_ATT_DATE).append(" = ?");
                statsArgs.add(dateFilter.trim());
            }

            statsSql.append(" GROUP BY ").append(COL_ATT_STATUS);

            Cursor statsCursor = db.rawQuery(statsSql.toString(), statsArgs.toArray(new String[0]));

            int attended = 0;
            int absent = 0;

            if (statsCursor != null && statsCursor.moveToFirst()) {
                do {
                    String status = statsCursor.getString(statsCursor.getColumnIndexOrThrow(COL_ATT_STATUS));
                    int count = statsCursor.getInt(statsCursor.getColumnIndexOrThrow("status_count"));
                    if ("Present".equalsIgnoreCase(status)) {
                        attended = count;
                    } else if ("Absent".equalsIgnoreCase(status)) {
                        absent = count;
                    }
                } while (statsCursor.moveToNext());
                statsCursor.close();
            }

            int studentSessions = attended + absent;
            int effectiveTotal = Math.max(totalConducted, studentSessions);

            double percentage = 0.0;
            if (effectiveTotal > 0) {
                percentage = ((double) attended / effectiveTotal) * 100.0;
            }

            AttendanceReportModel model = new AttendanceReportModel(
                    student.getId(),
                    student.getRegisterNumber(),
                    student.getName(),
                    student.getSemester(),
                    student.getDivision(),
                    subjectName,
                    effectiveTotal,
                    attended,
                    absent,
                    percentage
            );
            reportList.add(model);
        }

        return reportList;
    }
}
