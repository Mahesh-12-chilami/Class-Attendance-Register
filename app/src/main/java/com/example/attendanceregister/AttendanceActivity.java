package com.example.attendanceregister;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Screen 3: Mark Attendance.
 * Upgraded to support:
 * - Semester & Division filtering (Students from A1 never appear in A2)
 * - Permanent Session Locking:
 *   - Checked upon entry. If session is already submitted, dialog prevents access.
 *   - Saved in an atomic SQLite transaction with UNIQUE constraint on (semester, division, subject, date, period).
 *   - Automatically locks session upon submission. No editing or resubmission allowed.
 * - Dynamic CheckBox creation in Java code
 * - Live counter updates (Total, Present, Absent)
 * - Lab 6 StringBuilder summary
 */
public class AttendanceActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceActivity";

    private TextView tvSessionHeaderInfo, tvTotalCount, tvPresentCount, tvAbsentCount, tvNoStudentsWarning;
    private Button btnMarkAllPresent, btnClearAll, btnSaveAttendance;
    private LinearLayout llStudentsContainer, layoutLockedBanner;
    private TextView tvLockedBannerText;

    private DBHelper dbHelper;
    private String semester = "Semester 5";
    private String division = "A1";
    private int subjectId = 1;
    private String subjectName = "Subject";
    private String date = "2026-11-17";
    private String period = "Period 1";

    private List<Student> studentList = new ArrayList<>();
    private final Map<Integer, CheckBox> studentCheckBoxMap = new HashMap<>();
    private final Map<Integer, TextView> studentStatusTextMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        dbHelper = new DBHelper(this);

        // Retrieve session extras
        if (getIntent() != null) {
            String semExtra = getIntent().getStringExtra("SEMESTER");
            String divExtra = getIntent().getStringExtra("DIVISION");
            String subNameExtra = getIntent().getStringExtra("SUBJECT_NAME");
            String dateExtra = getIntent().getStringExtra("DATE");
            String periodExtra = getIntent().getStringExtra("PERIOD");

            if (semExtra != null) semester = semExtra;
            if (divExtra != null) division = divExtra;
            if (subNameExtra != null) subjectName = subNameExtra;
            if (dateExtra != null) date = dateExtra;
            if (periodExtra != null) period = periodExtra;
            subjectId = getIntent().getIntExtra("SUBJECT_ID", 1);
        }

        tvSessionHeaderInfo = findViewById(R.id.tvSessionHeaderInfo);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvPresentCount = findViewById(R.id.tvPresentCount);
        tvAbsentCount = findViewById(R.id.tvAbsentCount);
        tvNoStudentsWarning = findViewById(R.id.tvNoStudentsWarning);
        btnMarkAllPresent = findViewById(R.id.btnMarkAllPresent);
        btnClearAll = findViewById(R.id.btnClearAll);
        btnSaveAttendance = findViewById(R.id.btnSaveAttendance);
        llStudentsContainer = findViewById(R.id.llStudentsContainer);
        layoutLockedBanner = findViewById(R.id.layoutLockedBanner);
        tvLockedBannerText = findViewById(R.id.tvLockedBannerText);

        tvSessionHeaderInfo.setText(subjectName + " | " + semester + " (" + division + ") | " + date + " | " + period);

        // Strict lock check upon entry
        if (dbHelper.isSessionLocked(semester, division, subjectId, date, period)) {
            showLockedSessionDialog();
            return;
        }

        // Load students strictly belonging to the chosen Semester AND Division
        loadStudentsAndGenerateUI();

        btnMarkAllPresent.setOnClickListener(v -> markAllStudents(true));
        btnClearAll.setOnClickListener(v -> markAllStudents(false));
        btnSaveAttendance.setOnClickListener(v -> saveAndLockAttendance());
    }

    /**
     * Enforces requirement:
     * "Once attendance is successfully saved, that session must automatically become LOCKED.
     *  Do not allow faculty to open the same session for marking attendance again.
     *  Display a clear message: 'Attendance Already Submitted. This Session Is Locked.'"
     */
    private void showLockedSessionDialog() {
        layoutLockedBanner.setVisibility(View.VISIBLE);
        tvLockedBannerText.setText("Attendance has already been submitted for " + semester + " (" + division + "), " + subjectName + ", " + period + " on " + date + ".");
        btnSaveAttendance.setEnabled(false);
        btnMarkAllPresent.setEnabled(false);
        btnClearAll.setEnabled(false);

        new AlertDialog.Builder(this)
                .setTitle("Attendance Already Submitted")
                .setMessage("Attendance Already Submitted. This Session Is Locked.\n\n"
                        + "Attendance has already been submitted for this period. You cannot mark it again.\n\n"
                        + "• Semester: " + semester + "\n"
                        + "• Division: " + division + "\n"
                        + "• Subject: " + subjectName + "\n"
                        + "• Date: " + date + "\n"
                        + "• Period: " + period)
                .setIcon(R.drawable.ic_lock)
                .setCancelable(false)
                .setPositiveButton("Exit", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }

    /**
     * Dynamically generates student CheckBox rows in Java code.
     * Strictly filters students by Semester and Division.
     */
    private void loadStudentsAndGenerateUI() {
        studentList = dbHelper.getStudentsBySemesterAndDivision(semester, division);
        llStudentsContainer.removeAllViews();
        studentCheckBoxMap.clear();
        studentStatusTextMap.clear();

        if (studentList.isEmpty()) {
            tvNoStudentsWarning.setVisibility(View.VISIBLE);
            tvNoStudentsWarning.setText("No students found in " + semester + " Division " + division + ".\nPlease add students in Student Management.");
            tvTotalCount.setText("Total: 0");
            tvPresentCount.setText("Present: 0");
            tvAbsentCount.setText("Absent: 0");
            return;
        }

        tvNoStudentsWarning.setVisibility(View.GONE);

        for (Student student : studentList) {
            // Container CardView
            CardView cardView = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 8, 0, 8);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(12f);
            cardView.setCardElevation(3f);
            cardView.setCardBackgroundColor(Color.WHITE);

            // Row Layout
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER_VERTICAL);
            rowLayout.setPadding(20, 16, 20, 16);

            // Dynamic CheckBox
            CheckBox checkBox = new CheckBox(this);
            checkBox.setChecked(false); // Default to absent
            studentCheckBoxMap.put(student.getId(), checkBox);

            // Student Info
            LinearLayout infoLayout = new LinearLayout(this);
            infoLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            infoParams.setMargins(16, 0, 0, 0);
            infoLayout.setLayoutParams(infoParams);

            TextView tvRegNo = new TextView(this);
            tvRegNo.setText(student.getRegisterNumber() + " (" + student.getSemester() + " - " + student.getDivision() + ")");
            tvRegNo.setTextSize(12f);
            tvRegNo.setTypeface(null, Typeface.BOLD);
            tvRegNo.setTextColor(ContextCompat.getColor(this, R.color.primary));

            TextView tvName = new TextView(this);
            tvName.setText(student.getName());
            tvName.setTextSize(15f);
            tvName.setTypeface(null, Typeface.BOLD);
            tvName.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

            infoLayout.addView(tvRegNo);
            infoLayout.addView(tvName);

            // Status label
            TextView tvStatus = new TextView(this);
            tvStatus.setText("ABSENT");
            tvStatus.setTextSize(12f);
            tvStatus.setTypeface(null, Typeface.BOLD);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_absent));
            tvStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.badge_shortage));
            studentStatusTextMap.put(student.getId(), tvStatus);

            // Listeners
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateRowStatus(student.getId(), isChecked);
                updateCounters();
            });

            cardView.setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));

            rowLayout.addView(checkBox);
            rowLayout.addView(infoLayout);
            rowLayout.addView(tvStatus);
            cardView.addView(rowLayout);

            llStudentsContainer.addView(cardView);
        }

        updateCounters();
    }

    private void updateRowStatus(int studentId, boolean isChecked) {
        TextView tvStatus = studentStatusTextMap.get(studentId);
        if (tvStatus != null) {
            if (isChecked) {
                tvStatus.setText("PRESENT");
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_present));
                tvStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.badge_eligible));
            } else {
                tvStatus.setText("ABSENT");
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_absent));
                tvStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.badge_shortage));
            }
        }
    }

    private void markAllStudents(boolean present) {
        for (Map.Entry<Integer, CheckBox> entry : studentCheckBoxMap.entrySet()) {
            entry.getValue().setChecked(present);
            updateRowStatus(entry.getKey(), present);
        }
        updateCounters();
    }

    private void updateCounters() {
        int total = studentList.size();
        int presentCount = 0;
        for (CheckBox cb : studentCheckBoxMap.values()) {
            if (cb.isChecked()) {
                presentCount++;
            }
        }
        int absentCount = total - presentCount;

        tvTotalCount.setText("Total: " + total);
        tvPresentCount.setText("Present: " + presentCount);
        tvAbsentCount.setText("Absent: " + absentCount);
    }

    /**
     * Saves attendance records and permanently locks the session using SQLite transactions.
     * Prevents duplicate sessions even upon app restart.
     */
    private void saveAndLockAttendance() {
        if (studentList.isEmpty()) {
            Toast.makeText(this, "No students to record attendance for!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Database-level verification before proceeding
        if (dbHelper.isSessionLocked(semester, division, subjectId, date, period)) {
            showLockedSessionDialog();
            return;
        }

        Map<Integer, Boolean> attendanceMap = new HashMap<>();
        StringBuilder presentList = new StringBuilder();
        StringBuilder absentList = new StringBuilder();

        for (Student student : studentList) {
            CheckBox cb = studentCheckBoxMap.get(student.getId());
            boolean isPresent = (cb != null && cb.isChecked());
            attendanceMap.put(student.getId(), isPresent);

            if (isPresent) {
                if (presentList.length() > 0) presentList.append(", ");
                presentList.append(student.getRegisterNumber());
            } else {
                if (absentList.length() > 0) absentList.append(", ");
                absentList.append(student.getRegisterNumber());
            }
        }

        // Perform transactional save and permanent lock
        boolean success = dbHelper.lockAndSaveSessionAttendance(
                semester, division, subjectId, date, period, studentList, attendanceMap
        );

        if (success) {
            StringBuilder summary = new StringBuilder();
            summary.append("Attendance submitted successfully!\n")
                    .append("This session is now PERMANENTLY LOCKED.\n\n")
                    .append("• Semester: ").append(semester).append("\n")
                    .append("• Division: ").append(division).append("\n")
                    .append("• Subject: ").append(subjectName).append("\n")
                    .append("• Date: ").append(date).append("\n")
                    .append("• Period: ").append(period).append("\n\n")
                    .append("Present (").append(tvPresentCount.getText()).append("): ")
                    .append(presentList.length() > 0 ? presentList.toString() : "None").append("\n\n")
                    .append("Absent (").append(tvAbsentCount.getText()).append("): ")
                    .append(absentList.length() > 0 ? absentList.toString() : "None");

            Log.d(TAG, "Attendance Saved & Session Locked:\n" + summary);

            new AlertDialog.Builder(this)
                    .setTitle("Attendance Submitted & Locked")
                    .setMessage(summary.toString())
                    .setIcon(R.drawable.ic_lock)
                    .setCancelable(false)
                    .setPositiveButton("Done", (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    })
                    .setNeutralButton("View Reports", (dialog, which) -> {
                        dialog.dismiss();
                        Intent intent = new Intent(AttendanceActivity.this, ReportActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Error Saving Attendance")
                    .setMessage("Failed to save attendance. A session with this Semester, Division, Subject, Date, and Period already exists or is locked.")
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }
}
