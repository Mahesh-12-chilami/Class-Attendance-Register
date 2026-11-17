package com.example.attendanceregister;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Screen 4: Attendance Report Screen.
 * Upgraded to support:
 * - Dynamic Semester-wise Subject filtering (Changing semester refreshes subject filter with only that semester's subjects)
 * - Division filter (All Divisions, A1 to A6)
 * - Date filter (All Dates or specific date)
 * - SQLite GROUP BY and COUNT queries
 * - Highlights students below 75% in RED with shortage badges and warnings
 */
public class ReportActivity extends AppCompatActivity {

    private Spinner spReportSemester, spReportDivision, spReportSubject;
    private LinearLayout layoutReportDatePicker;
    private TextView tvReportDateFilter;
    private TextView tvSummaryTotal, tvSummaryEligible, tvSummaryShortage, tvReportEmpty;
    private ListView lvAttendanceReport;

    private DBHelper dbHelper;
    private List<AttendanceReportModel> reportList = new ArrayList<>();
    private AttendanceReportAdapter reportAdapter;

    private Calendar filterCalendar;
    private SimpleDateFormat dateFormat;
    private String selectedDateFilter = "All Dates";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        dbHelper = new DBHelper(this);
        filterCalendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        spReportSemester = findViewById(R.id.spReportSemester);
        spReportDivision = findViewById(R.id.spReportDivision);
        spReportSubject = findViewById(R.id.spReportSubject);
        layoutReportDatePicker = findViewById(R.id.layoutReportDatePicker);
        tvReportDateFilter = findViewById(R.id.tvReportDateFilter);

        tvSummaryTotal = findViewById(R.id.tvSummaryTotal);
        tvSummaryEligible = findViewById(R.id.tvSummaryEligible);
        tvSummaryShortage = findViewById(R.id.tvSummaryShortage);
        tvReportEmpty = findViewById(R.id.tvReportEmpty);
        lvAttendanceReport = findViewById(R.id.lvAttendanceReport);

        setupFilters();
        setupDatePicker();
    }

    private void setupFilters() {
        // 1. Semester Filter
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
        spReportSemester.setAdapter(semesterAdapter);
        spReportSemester.setSelection(5); // Default to Semester 5

        // 2. Division Filter (A1 to A6)
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
        spReportDivision.setAdapter(divisionAdapter);
        spReportDivision.setSelection(1); // Default to A1

        // Dynamically populate subjects for initial semester selection
        updateReportSubjectFilter("Semester 5");

        // When Semester changes, dynamically reload ONLY that semester's subjects
        spReportSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String chosenSemester = semesters[position];
                updateReportSubjectFilter(chosenSemester);
                loadReportData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        AdapterView.OnItemSelectedListener genericFilterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadReportData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spReportDivision.setOnItemSelectedListener(genericFilterListener);
        spReportSubject.setOnItemSelectedListener(genericFilterListener);
    }

    /**
     * Loads only the subjects belonging to the chosen semester.
     */
    private void updateReportSubjectFilter(String semester) {
        List<String> subjectList;
        if ("All Semesters".equalsIgnoreCase(semester)) {
            subjectList = dbHelper.getSubjectNamesBySemester("Semester 5");
        } else {
            subjectList = dbHelper.getSubjectNamesBySemester(semester);
        }

        if (subjectList.isEmpty()) {
            subjectList = new ArrayList<>();
            subjectList.add("No Subjects");
        }

        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, subjectList);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spReportSubject.setAdapter(subjectAdapter);
        spReportSubject.setSelection(0);
    }

    private void setupDatePicker() {
        layoutReportDatePicker.setOnClickListener(v -> {
            String[] options = {"All Dates", "Select Specific Date..."};
            new AlertDialog.Builder(this)
                    .setTitle("Filter By Date")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            selectedDateFilter = "All Dates";
                            tvReportDateFilter.setText(selectedDateFilter);
                            loadReportData();
                        } else {
                            int year = filterCalendar.get(Calendar.YEAR);
                            int month = filterCalendar.get(Calendar.MONTH);
                            int day = filterCalendar.get(Calendar.DAY_OF_MONTH);

                            DatePickerDialog datePicker = new DatePickerDialog(
                                    ReportActivity.this,
                                    (view, selectedYear, selectedMonth, selectedDay) -> {
                                        filterCalendar.set(Calendar.YEAR, selectedYear);
                                        filterCalendar.set(Calendar.MONTH, selectedMonth);
                                        filterCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                                        selectedDateFilter = dateFormat.format(filterCalendar.getTime());
                                        tvReportDateFilter.setText(selectedDateFilter);
                                        loadReportData();
                                    },
                                    year, month, day
                            );
                            datePicker.show();
                        }
                    })
                    .show();
        });
    }

    private void loadReportData() {
        String semesterFilter = (String) spReportSemester.getSelectedItem();
        String divisionFilter = (String) spReportDivision.getSelectedItem();
        String subject = (String) spReportSubject.getSelectedItem();

        if (subject == null || "No Subjects".equalsIgnoreCase(subject)) {
            tvReportEmpty.setVisibility(View.VISIBLE);
            lvAttendanceReport.setVisibility(View.GONE);
            updateSummaryCounters(0, 0, 0);
            return;
        }

        int subjectId = dbHelper.getSubjectIdByNameAndSemester(subject, semesterFilter != null && !semesterFilter.equals("All Semesters") ? semesterFilter : "Semester 5");
        if (subjectId == -1) subjectId = dbHelper.getSubjectIdByName(subject);
        if (subjectId == -1) subjectId = 1;

        // Perform aggregated query using SQLite GROUP BY and COUNT
        reportList = dbHelper.getAdvancedAttendanceReport(
                semesterFilter, divisionFilter, subjectId, selectedDateFilter
        );

        if (reportList.isEmpty()) {
            tvReportEmpty.setVisibility(View.VISIBLE);
            lvAttendanceReport.setVisibility(View.GONE);
            updateSummaryCounters(0, 0, 0);
        } else {
            tvReportEmpty.setVisibility(View.GONE);
            lvAttendanceReport.setVisibility(View.VISIBLE);

            reportAdapter = new AttendanceReportAdapter(this, reportList);
            lvAttendanceReport.setAdapter(reportAdapter);

            int totalStudents = reportList.size();
            int eligibleCount = 0;
            int shortageCount = 0;

            for (AttendanceReportModel model : reportList) {
                if (model.getTotalClasses() > 0) {
                    if (model.isShortage()) {
                        shortageCount++;
                    } else {
                        eligibleCount++;
                    }
                }
            }

            updateSummaryCounters(totalStudents, eligibleCount, shortageCount);
        }
    }

    private void updateSummaryCounters(int total, int eligible, int shortage) {
        tvSummaryTotal.setText("Total: " + total);
        tvSummaryEligible.setText("Eligible (>=75%): " + eligible);
        tvSummaryShortage.setText("Shortage (<75%): " + shortage);
    }
}
