package com.example.attendanceregister;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Entry Activity for Class Attendance Register.
 * Demonstrates:
 * - Activity lifecycle methods with Log.d statements (College Lab requirement)
 * - Navigation to Login and Registration screens using explicit Intents (Lab 4)
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityLifecycle";

    private Button btnMainLogin;
    private Button btnMainRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called: Initializing Class Attendance Register...");
        setContentView(R.layout.activity_main);

        btnMainLogin = findViewById(R.id.btnMainLogin);
        btnMainRegister = findViewById(R.id.btnMainRegister);

        // Pre-initialize database to trigger table creation and default records
        DBHelper dbHelper = new DBHelper(this);
        dbHelper.getWritableDatabase();

        // Navigate to Login Activity
        btnMainLogin.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to LoginActivity via Explicit Intent");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Navigate to Register Activity
        btnMainRegister.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to RegisterActivity via Explicit Intent");
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called: Activity is becoming visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called: Activity has focus and is running");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called: Activity is paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called: Activity is no longer visible");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart() called: Activity is restarting");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called: Activity is being destroyed");
    }
}
