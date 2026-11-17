package com.example.attendanceregister;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 5: Student Management.
 * Upgraded to support:
 * - Adding/Editing students with Semester (1 to 6) and Division (A1 to A6)
 * - Filtering students by Semester and Division
 * - Real-time search by Student Name or Register Number
 * - Register number uniqueness validation
 * - AlertDialog for confirmation before deleting SQLite records
 */
public class StudentManagementActivity extends AppCompatActivity implements StudentManagementAdapter.OnStudentActionListener {

    private EditText etSearchStudent;
    private Spinner spFilterSemester, spFilterDivision;
    private Button btnAddStudent;
    private ListView lvStudents;
    private TextView tvEmptyStudents;

    private DBHelper dbHelper;
    private List<Student> studentList = new ArrayList<>();
    private StudentManagementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_management);

        dbHelper = new DBHelper(this);

        etSearchStudent = findViewById(R.id.etSearchStudent);
        spFilterSemester = findViewById(R.id.spFilterSemester);
        spFilterDivision = findViewById(R.id.spFilterDivision);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        lvStudents = findViewById(R.id.lvStudents);
        tvEmptyStudents = findViewById(R.id.tvEmptyStudents);

        setupFilterSpinners();

        btnAddStudent.setOnClickListener(v -> showAddEditStudentDialog(null));

        // Real-time search filter
        etSearchStudent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        applyFilters();
    }

    private void setupFilterSpinners() {
        String[] semesters = {
                "All Semesters",
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
        spFilterSemester.setAdapter(semesterAdapter);

        String[] divisions = {
                "All Divisions",
                "A1",
                "A2",
                "A3",
                "A4",
                "A5",
                "A6"
        };
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, divisions);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterDivision.setAdapter(divisionAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spFilterSemester.setOnItemSelectedListener(filterListener);
        spFilterDivision.setOnItemSelectedListener(filterListener);
    }

    private void applyFilters() {
        String query = etSearchStudent.getText().toString().trim();
        String semesterFilter = (String) spFilterSemester.getSelectedItem();
        String divisionFilter = (String) spFilterDivision.getSelectedItem();

        studentList = dbHelper.searchStudents(query, semesterFilter, divisionFilter);
        updateUI();
    }

    private void updateUI() {
        if (studentList.isEmpty()) {
            tvEmptyStudents.setVisibility(View.VISIBLE);
            lvStudents.setVisibility(View.GONE);
        } else {
            tvEmptyStudents.setVisibility(View.GONE);
            lvStudents.setVisibility(View.VISIBLE);
            adapter = new StudentManagementAdapter(this, studentList, this);
            lvStudents.setAdapter(adapter);
        }
    }

    /**
     * Shows Dialog to Add or Edit a Student with Semester and Division.
     */
    private void showAddEditStudentDialog(Student studentToEdit) {
        boolean isEditing = (studentToEdit != null);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_student, null);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etRegNo = dialogView.findViewById(R.id.etDialogRegNo);
        EditText etStudentName = dialogView.findViewById(R.id.etDialogStudentName);
        Spinner spDialogSemester = dialogView.findViewById(R.id.spDialogSemester);
        Spinner spDialogDivision = dialogView.findViewById(R.id.spDialogDivision);

        // Semester Spinner
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
        spDialogSemester.setAdapter(semesterAdapter);

        // Division Spinner (A1 to A6)
        String[] divisions = {"A1", "A2", "A3", "A4", "A5", "A6"};
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, divisions);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDialogDivision.setAdapter(divisionAdapter);

        if (isEditing) {
            tvDialogTitle.setText(R.string.dialog_edit_student);
            etRegNo.setText(studentToEdit.getRegisterNumber());
            etStudentName.setText(studentToEdit.getName());

            for (int i = 0; i < semesters.length; i++) {
                if (semesters[i].equalsIgnoreCase(studentToEdit.getSemester())) {
                    spDialogSemester.setSelection(i);
                    break;
                }
            }

            for (int i = 0; i < divisions.length; i++) {
                if (divisions[i].equalsIgnoreCase(studentToEdit.getDivision())) {
                    spDialogDivision.setSelection(i);
                    break;
                }
            }
        } else {
            tvDialogTitle.setText(R.string.dialog_add_student);
            spDialogSemester.setSelection(4); // Default to Semester 5
            spDialogDivision.setSelection(0); // Default to A1
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(isEditing ? "Update" : "Add", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                String regNo = etRegNo.getText().toString().trim().toUpperCase();
                String name = etStudentName.getText().toString().trim();
                String semester = (String) spDialogSemester.getSelectedItem();
                String division = (String) spDialogDivision.getSelectedItem();

                if (TextUtils.isEmpty(regNo)) {
                    etRegNo.setError("Register number is required");
                    etRegNo.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(name)) {
                    etStudentName.setError("Student name is required");
                    etStudentName.requestFocus();
                    return;
                }

                int excludeId = isEditing ? studentToEdit.getId() : -1;
                if (dbHelper.isRegisterNumberExists(regNo, excludeId)) {
                    etRegNo.setError("Register number already exists! Must be unique.");
                    etRegNo.requestFocus();
                    return;
                }

                boolean success;
                if (isEditing) {
                    success = dbHelper.updateStudent(studentToEdit.getId(), regNo, name, semester, division);
                    if (success) {
                        Toast.makeText(StudentManagementActivity.this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    success = dbHelper.insertStudent(regNo, name, semester, division);
                    if (success) {
                        Toast.makeText(StudentManagementActivity.this, "Student added to " + semester + " (" + division + ") successfully", Toast.LENGTH_SHORT).show();
                    }
                }

                if (success) {
                    dialog.dismiss();
                    applyFilters();
                } else {
                    Toast.makeText(StudentManagementActivity.this, "Database operation failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    @Override
    public void onEdit(Student student) {
        showAddEditStudentDialog(student);
    }

    @Override
    public void onDelete(Student student) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete student " + student.getName() + " (" + student.getRegisterNumber() + ")?\nAssociated attendance records will also be deleted.")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = dbHelper.deleteStudent(student.getId());
                    if (deleted) {
                        Toast.makeText(StudentManagementActivity.this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                        applyFilters();
                    } else {
                        Toast.makeText(StudentManagementActivity.this, "Failed to delete student", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
