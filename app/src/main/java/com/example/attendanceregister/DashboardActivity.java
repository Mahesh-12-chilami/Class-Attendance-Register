package com.example.attendanceregister;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * Modern Dashboard for Faculty.
 * Demonstrates:
 * - AlertDialog for confirmation (Lab 5)
 * - Navigation to functional modules via Explicit Intents:
 *   1. Mark Attendance
 *   2. Attendance Reports
 *   3. Manage Students
 *   4. Manage Subjects (Semester-Wise)
 * - Displaying dynamic faculty session data
 */
public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeFaculty;
    private CardView cardMarkAttendance, cardAttendanceReports, cardManageStudents, cardManageSubjects;
    private Button btnLogout;
    private ImageButton btnQuickLogout;

    private String facultyId = "";
    private String facultyName = "Faculty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Retrieve passed extras from Login / Register
        if (getIntent() != null) {
            String nameExtra = getIntent().getStringExtra("FACULTY_NAME");
            String idExtra = getIntent().getStringExtra("FACULTY_ID");
            if (nameExtra != null && !nameExtra.isEmpty()) {
                facultyName = nameExtra;
            }
            if (idExtra != null) {
                facultyId = idExtra;
            }
        }

        tvWelcomeFaculty = findViewById(R.id.tvWelcomeFaculty);
        cardMarkAttendance = findViewById(R.id.cardMarkAttendance);
        cardAttendanceReports = findViewById(R.id.cardAttendanceReports);
        cardManageStudents = findViewById(R.id.cardManageStudents);
        cardManageSubjects = findViewById(R.id.cardManageSubjects);
        btnLogout = findViewById(R.id.btnLogout);
        btnQuickLogout = findViewById(R.id.btnQuickLogout);

        tvWelcomeFaculty.setText(getString(R.string.welcome_faculty, facultyName));

        // Mark Attendance module
        cardMarkAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SelectClassActivity.class);
            startActivity(intent);
        });

        // Attendance Reports module
        cardAttendanceReports.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ReportActivity.class);
            startActivity(intent);
        });

        // Manage Students module
        cardManageStudents.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, StudentManagementActivity.class);
            startActivity(intent);
        });

        // Manage Subjects module (Semester-Wise)
        cardManageSubjects.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, SubjectManagementActivity.class);
            startActivity(intent);
        });

        // Logout handlers with AlertDialog confirmation
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        btnQuickLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    /**
     * Demonstrates Lab 5: AlertDialog with Positive and Negative buttons.
     */
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out of your session?")
                .setIcon(R.drawable.ic_logout)
                .setPositiveButton("Logout", (dialog, which) -> {
                    Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finishAffinity();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onBackPressed() {
        showLogoutConfirmationDialog();
    }
}
