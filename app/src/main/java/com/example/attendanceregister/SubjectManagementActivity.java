package com.example.attendanceregister;

import android.os.Bundle;
import android.text.TextUtils;
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
 * Screen: Subject Management.
 * Demonstrates:
 * - Independent curriculum management for Semesters 1 to 6
 * - Composite uniqueness validation on (semester + subject_name)
 * - Safe data preservation: Prohibits deleting subjects with existing attendance records
 * - AlertDialog for confirmation (Lab 5)
 */
public class SubjectManagementActivity extends AppCompatActivity implements SubjectAdapter.OnSubjectActionListener {

    private Spinner spSubjectFilterSemester;
    private Button btnAddSubject;
    private ListView lvSubjects;
    private TextView tvEmptySubjects;

    private DBHelper dbHelper;
    private List<Subject> subjectList = new ArrayList<>();
    private SubjectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_management);

        dbHelper = new DBHelper(this);

        spSubjectFilterSemester = findViewById(R.id.spSubjectFilterSemester);
        btnAddSubject = findViewById(R.id.btnAddSubject);
        lvSubjects = findViewById(R.id.lvSubjects);
        tvEmptySubjects = findViewById(R.id.tvEmptySubjects);

        setupSemesterFilter();

        btnAddSubject.setOnClickListener(v -> showAddEditSubjectDialog(null));

        loadSubjects();
    }

    private void setupSemesterFilter() {
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
        spSubjectFilterSemester.setAdapter(semesterAdapter);
        spSubjectFilterSemester.setSelection(5); // Default to Semester 5

        spSubjectFilterSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadSubjects();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadSubjects() {
        String selectedSemester = (String) spSubjectFilterSemester.getSelectedItem();
        subjectList = dbHelper.getSubjectsBySemester(selectedSemester);

        if (subjectList.isEmpty()) {
            tvEmptySubjects.setVisibility(View.VISIBLE);
            lvSubjects.setVisibility(View.GONE);
        } else {
            tvEmptySubjects.setVisibility(View.GONE);
            lvSubjects.setVisibility(View.VISIBLE);
            adapter = new SubjectAdapter(this, subjectList, this);
            lvSubjects.setAdapter(adapter);
        }
    }

    private void showAddEditSubjectDialog(Subject subjectToEdit) {
        boolean isEditing = (subjectToEdit != null);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_subject, null);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvSubjectDialogTitle);
        Spinner spDialogSemester = dialogView.findViewById(R.id.spDialogSubjectSemester);
        EditText etSubjectName = dialogView.findViewById(R.id.etDialogSubjectName);

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

        if (isEditing) {
            tvDialogTitle.setText("Edit Subject");
            etSubjectName.setText(subjectToEdit.getName());

            for (int i = 0; i < semesters.length; i++) {
                if (semesters[i].equalsIgnoreCase(subjectToEdit.getSemester())) {
                    spDialogSemester.setSelection(i);
                    break;
                }
            }
        } else {
            tvDialogTitle.setText("Add New Subject");
            String currentFilter = (String) spSubjectFilterSemester.getSelectedItem();
            if (currentFilter != null && !currentFilter.equalsIgnoreCase("All Semesters")) {
                for (int i = 0; i < semesters.length; i++) {
                    if (semesters[i].equalsIgnoreCase(currentFilter)) {
                        spDialogSemester.setSelection(i);
                        break;
                    }
                }
            } else {
                spDialogSemester.setSelection(4); // Default to Semester 5
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(isEditing ? "Update" : "Add", null)
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                String subjectName = etSubjectName.getText().toString().trim();
                String semester = (String) spDialogSemester.getSelectedItem();

                if (TextUtils.isEmpty(subjectName)) {
                    etSubjectName.setError("Subject name is required");
                    etSubjectName.requestFocus();
                    return;
                }

                int excludeId = isEditing ? subjectToEdit.getId() : -1;
                if (dbHelper.isSubjectExists(subjectName, semester, excludeId)) {
                    etSubjectName.setError("Subject '" + subjectName + "' already exists in " + semester + "!");
                    etSubjectName.requestFocus();
                    return;
                }

                boolean success;
                if (isEditing) {
                    success = dbHelper.updateSubject(subjectToEdit.getId(), subjectName, semester);
                    if (success) {
                        Toast.makeText(SubjectManagementActivity.this, "Subject updated successfully", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    success = dbHelper.insertSubject(subjectName, semester);
                    if (success) {
                        Toast.makeText(SubjectManagementActivity.this, "Subject added to " + semester + " successfully", Toast.LENGTH_SHORT).show();
                    }
                }

                if (success) {
                    dialog.dismiss();
                    loadSubjects();
                } else {
                    Toast.makeText(SubjectManagementActivity.this, "Database operation failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    @Override
    public void onEdit(Subject subject) {
        showAddEditSubjectDialog(subject);
    }

    /**
     * Enforces requirement:
     * "Do not allow deletion of a subject that has existing attendance records unless a safe data-preserving process is implemented."
     */
    @Override
    public void onDelete(Subject subject) {
        // Safe check for existing attendance records
        if (dbHelper.hasSubjectAttendanceRecords(subject.getId())) {
            new AlertDialog.Builder(this)
                    .setTitle("Cannot Delete Subject")
                    .setMessage("Subject '" + subject.getName() + "' (" + subject.getSemester() + ") has existing attendance records saved in the database.\n\nTo preserve historical attendance records, this subject cannot be deleted.")
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete subject '" + subject.getName() + "' from " + subject.getSemester() + "?")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = dbHelper.deleteSubject(subject.getId());
                    if (deleted) {
                        Toast.makeText(SubjectManagementActivity.this, "Subject deleted successfully", Toast.LENGTH_SHORT).show();
                        loadSubjects();
                    } else {
                        Toast.makeText(SubjectManagementActivity.this, "Failed to delete subject", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
