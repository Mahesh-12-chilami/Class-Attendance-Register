package com.example.attendanceregister;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Faculty Registration Screen.
 * Demonstrates:
 * - Comprehensive input validation
 * - Duplicate faculty ID checking
 * - SQLite record insertion
 * - Explicit Intent navigation with Toast feedback (Lab 4)
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etFacultyName, etFacultyId, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DBHelper(this);

        etFacultyName = findViewById(R.id.etRegFacultyName);
        etFacultyId = findViewById(R.id.etRegFacultyId);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> handleRegistration());

        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegistration() {
        String name = etFacultyName.getText().toString().trim();
        String facultyId = etFacultyId.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 1. Empty field validation
        if (TextUtils.isEmpty(name)) {
            etFacultyName.setError("Faculty name is required");
            etFacultyName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(facultyId)) {
            etFacultyId.setError("Faculty ID is required");
            etFacultyId.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email address is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // 2. Duplicate Faculty ID validation
        if (dbHelper.isFacultyExists(facultyId)) {
            Toast.makeText(this, "Faculty ID already exists! Please use a unique ID.", Toast.LENGTH_LONG).show();
            etFacultyId.setError("Duplicate Faculty ID");
            etFacultyId.requestFocus();
            return;
        }

        // 3. Insert Faculty into SQLite
        boolean isInserted = dbHelper.insertFaculty(facultyId, name, email, password);
        if (isInserted) {
            Toast.makeText(this, "Registration successful! Welcome, " + name, Toast.LENGTH_LONG).show();

            // Navigate to Dashboard with faculty information via Explicit Intent
            Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
            intent.putExtra("FACULTY_ID", facultyId);
            intent.putExtra("FACULTY_NAME", name);
            startActivity(intent);
            finishAffinity();
        } else {
            Toast.makeText(this, "Registration failed due to a database error. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
