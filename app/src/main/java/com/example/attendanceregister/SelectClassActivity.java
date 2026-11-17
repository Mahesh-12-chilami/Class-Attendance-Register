package com.example.attendanceregister;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Screen 2: Select Class Session.
 * Upgraded to support:
 * - 1. Semester Selection (Semester 1 to Semester 6)
 * - 2. Division Selection (A1 to A6)
 * - 3. Dynamic Semester-wise Subject Selection (Loads ONLY subjects for selected semester)
 * - 4. Date Selection
 * - 5. Period Selection (Period 1 to Period 6)
 * - Real-time permanent session lock detection and visual status indicator
 * - Prohibits opening or marking attendance for already submitted/locked sessions
 */
public class SelectClassActivity extends AppCompatActivity {

    private Spinner spSemester, spDivision, spSubject, spPeriod;
    private LinearLayout layoutDatePicker;
    private TextView tvSelectedDate;
    private Button btnPickTime, btnProceedAttendance;

    // Session Status UI components
    private LinearLayout layoutSessionStatus;
    private ImageView ivSessionStatusIcon;
    private TextView tvSessionStatusTitle, tvSessionStatusSub;

    private DBHelper dbHelper;
    private Calendar selectedCalendar;
    private SimpleDateFormat dateFormat;

    private boolean isCurrentSessionLocked = false;
    private List<String> currentSubjectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_class);

        dbHelper = new DBHelper(this);
        selectedCalendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        spSemester = findViewById(R.id.spSemester);
        spDivision = findViewById(R.id.spDivision);
        spSubject = findViewById(R.id.spSubject);
        spPeriod = findViewById(R.id.spPeriod);
        layoutDatePicker = findViewById(R.id.layoutDatePicker);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);

        btnProceedAttendance = findViewById(R.id.btnProceedAttendance);

        layoutSessionStatus = findViewById(R.id.layoutSessionStatus);
        ivSessionStatusIcon = findViewById(R.id.ivSessionStatusIcon);
        tvSessionStatusTitle = findViewById(R.id.tvSessionStatusTitle);
        tvSessionStatusSub = findViewById(R.id.tvSessionStatusSub);

        // Default to current date
        tvSelectedDate.setText(dateFormat.format(selectedCalendar.getTime()));

        setupSpinners();
        setupDatePicker();


        btnProceedAttendance.setOnClickListener(v -> handleProceedAttendance());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh subjects for currently selected semester in case subjects were added/updated
        String currentSemester = (String) spSemester.getSelectedItem();
        if (currentSemester != null) {
            loadSemesterSubjects(currentSemester);
        }
        checkSessionLockStatus();
    }

    private void setupSpinners() {
        // 1. Semester Spinner (Semester 1 to 6)
        String[] semesters = {
                "Semester 1",
                "Semester 2",
                "Semester 3",
                "Semester 4",
                "Semester 5",
                "Semester 6"
        };
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, semesters);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSemester.setAdapter(semesterAdapter);
        spSemester.setSelection(4); // Default to Semester 5

        // 2. Division Spinner (A1 to A6)
        String[] divisions = {"A1", "A2", "A3", "A4", "A5", "A6"};
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, divisions);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDivision.setAdapter(divisionAdapter);
        spDivision.setSelection(0); // Default to A1

        // 3. Period Spinner
        String[] periods = {
                "Period 1",
                "Period 2",
                "Period 3",
                "Period 4",
                "Period 5",
                "Period 6"
        };
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, periods);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPeriod.setAdapter(periodAdapter);

        // Load subjects dynamically for initial semester selection
        loadSemesterSubjects("Semester 5");

        // When Semester changes, dynamically load ONLY that semester's subjects
        spSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSemester = semesters[position];
                loadSemesterSubjects(selectedSemester);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Other spinner change listeners to check session lock status
        AdapterView.OnItemSelectedListener lockCheckListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkSessionLockStatus();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spDivision.setOnItemSelectedListener(lockCheckListener);
        spSubject.setOnItemSelectedListener(lockCheckListener);
        spPeriod.setOnItemSelectedListener(lockCheckListener);
    }

    /**
     * Dynamically loads only subjects belonging to the selected semester into spSubject.
     */
    private void loadSemesterSubjects(String semester) {
        currentSubjectList = dbHelper.getSubjectNamesBySemester(semester);

        if (currentSubjectList.isEmpty()) {
            List<String> emptyList = new ArrayList<>();
            emptyList.add("No Subjects Available");
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, emptyList);
            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spSubject.setAdapter(emptyAdapter);
            Toast.makeText(this, "No subjects registered for " + semester, Toast.LENGTH_SHORT).show();
        } else {
            ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, currentSubjectList);
            subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spSubject.setAdapter(subjectAdapter);
            spSubject.setSelection(0);
        }

        checkSessionLockStatus();
    }

    private void setupDatePicker() {
        layoutDatePicker.setOnClickListener(v -> {
            int year = selectedCalendar.get(Calendar.YEAR);
            int month = selectedCalendar.get(Calendar.MONTH);
            int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    SelectClassActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        selectedCalendar.set(Calendar.YEAR, selectedYear);
                        selectedCalendar.set(Calendar.MONTH, selectedMonth);
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                        tvSelectedDate.setText(dateFormat.format(selectedCalendar.getTime()));
                        checkSessionLockStatus();
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }


    /**
     * Checks if the combination (Semester, Division, Subject, Date, Period) is locked in SQLite.
     * Updates visual indicator badge and button styling accordingly.
     */
    private void checkSessionLockStatus() {
        String semester = (String) spSemester.getSelectedItem();
        String division = (String) spDivision.getSelectedItem();
        String subject = (String) spSubject.getSelectedItem();
        String period = (String) spPeriod.getSelectedItem();
        String date = tvSelectedDate.getText().toString();

        if (semester == null || division == null || subject == null || period == null
                || "No Subjects Available".equals(subject)) {
            layoutSessionStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.badge_shortage));
            ivSessionStatusIcon.setImageResource(R.drawable.ic_warning);
            tvSessionStatusTitle.setText("STATUS: NO SUBJECT SELECTED");
            tvSessionStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.status_absent));
            tvSessionStatusSub.setText("Please select or add a subject for this semester.");
            btnProceedAttendance.setText("No Subjects Available");
            btnProceedAttendance.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_danger));
            return;
        }

        int subjectId = dbHelper.getSubjectIdByNameAndSemester(subject, semester);
        if (subjectId == -1) subjectId = dbHelper.getSubjectIdByName(subject);

        isCurrentSessionLocked = dbHelper.isSessionLocked(semester, division, subjectId, date, period);

        if (isCurrentSessionLocked) {
            String lockDetails = dbHelper.getSessionLockDetails(semester, division, subjectId, date, period);

            layoutSessionStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.badge_shortage));
            ivSessionStatusIcon.setImageResource(R.drawable.ic_lock);
            tvSessionStatusTitle.setText("STATUS: SUBMITTED / LOCKED");
            tvSessionStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.status_absent));
            tvSessionStatusSub.setText("Attendance has already been submitted for this period. Session is locked (" + lockDetails + ").");

            btnProceedAttendance.setText("Attendance Already Submitted (Locked)");
            btnProceedAttendance.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_danger));
        } else {
            layoutSessionStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.badge_eligible));
            ivSessionStatusIcon.setImageResource(R.drawable.ic_check);
            tvSessionStatusTitle.setText("STATUS: NOT SUBMITTED");
            tvSessionStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.status_present));
            tvSessionStatusSub.setText("Attendance can be marked for this session.");

            btnProceedAttendance.setText(R.string.btn_proceed_attendance);
            btnProceedAttendance.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_primary));
        }
    }

    private void handleProceedAttendance() {
        String semester = (String) spSemester.getSelectedItem();
        String division = (String) spDivision.getSelectedItem();
        String subject = (String) spSubject.getSelectedItem();
        String period = (String) spPeriod.getSelectedItem();
        String date = tvSelectedDate.getText().toString();

        if (semester == null || division == null || subject == null || period == null) {
            Toast.makeText(this, "Please select all session fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("No Subjects Available".equalsIgnoreCase(subject)) {
            Toast.makeText(this, "No subjects available for " + semester + ". Please add subjects in Subject Management.", Toast.LENGTH_LONG).show();
            return;
        }

        int subjectId = dbHelper.getSubjectIdByNameAndSemester(subject, semester);
        if (subjectId == -1) subjectId = dbHelper.getSubjectIdByName(subject);

        // Double check session lock status
        if (dbHelper.isSessionLocked(semester, division, subjectId, date, period)) {
            new AlertDialog.Builder(this)
                    .setTitle("Attendance Already Submitted")
                    .setMessage("Attendance Already Submitted. This Session Is Locked.\n\n"
                            + "Attendance has already been submitted for this period. You cannot mark it again.\n\n"
                            + "• Semester: " + semester + "\n"
                            + "• Division: " + division + "\n"
                            + "• Subject: " + subject + "\n"
                            + "• Date: " + date + "\n"
                            + "• Period: " + period)
                    .setIcon(R.drawable.ic_lock)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        // Navigate to AttendanceActivity
        Intent intent = new Intent(SelectClassActivity.this, AttendanceActivity.class);
        intent.putExtra("SEMESTER", semester);
        intent.putExtra("DIVISION", division);
        intent.putExtra("SECTION", division); // Backward compatibility
        intent.putExtra("SUBJECT_ID", subjectId);
        intent.putExtra("SUBJECT_NAME", subject);
        intent.putExtra("DATE", date);
        intent.putExtra("PERIOD", period);
        startActivity(intent);
    }
}
