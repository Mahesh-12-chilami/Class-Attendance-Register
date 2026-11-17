package com.example.attendanceregister;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Faculty Login Screen.
 * Demonstrates:
 * - Credential authentication against SQLite database
 * - Empty input validation
 * - Navigation to Dashboard upon successful login using explicit Intents (Lab 4)
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etLoginId, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);

        etLoginId = findViewById(R.id.etLoginId);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> handleLogin());

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String idOrEmail = etLoginId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(idOrEmail)) {
            etLoginId.setError("Faculty ID or Email is required");
            etLoginId.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        String facultyName = dbHelper.checkFacultyLogin(idOrEmail, password);
        if (facultyName != null) {
            Toast.makeText(this, "Login successful! Welcome back, " + facultyName, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            intent.putExtra("FACULTY_ID", idOrEmail);
            intent.putExtra("FACULTY_NAME", facultyName);
            startActivity(intent);
            finishAffinity();
        } else {
            Toast.makeText(this, "Invalid credentials! Please check your ID and Password.", Toast.LENGTH_LONG).show();
        }
    }
}
