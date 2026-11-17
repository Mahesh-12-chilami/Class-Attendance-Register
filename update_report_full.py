import docx
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import parse_xml, OxmlElement
from docx.oxml.ns import nsdecls, qn
import os

input_path = r"C:\Users\Lenovo\OneDrive\Desktop\Android Project\Class_Attendance_Register_Project_Report.docx"
output_path = r"C:\Users\Lenovo\OneDrive\Desktop\Android Project\Class_Attendance_Register_Final_Report.docx"

doc = docx.Document(input_path)

def set_run_font(run, name="Calibri", size_pt=11, bold=False, italic=False, color_rgb=(40, 40, 40)):
    run.font.name = name
    if size_pt:
        run.font.size = Pt(size_pt)
    run.bold = bold
    run.italic = italic
    if color_rgb:
        run.font.color.rgb = RGBColor(*color_rgb)

def update_para(p, text, align=None, name="Calibri", size_pt=11, bold=False, italic=False, color_rgb=(40, 40, 40)):
    p.text = text
    if align is not None:
        p.alignment = align
    if p.runs:
        set_run_font(p.runs[0], name, size_pt, bold, italic, color_rgb)

def format_cell(cell, text, bold=False, size_pt=9.5, align=WD_ALIGN_PARAGRAPH.LEFT, color_rgb=(40, 40, 40), fill_hex=None):
    cell.text = text
    p = cell.paragraphs[0]
    p.alignment = align
    if p.runs:
        set_run_font(p.runs[0], name="Calibri", size_pt=size_pt, bold=bold, italic=False, color_rgb=color_rgb)
    if fill_hex:
        shading = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
        cell._tc.get_or_add_tcPr().append(shading)

NAVY = (42, 35, 110)      # #2A236E
PURPLE = (60, 50, 130)    # #3C3282
BODY_COL = (40, 40, 40)   # #282828

print("Step 1: Updating Abstract...")
abstract_p = doc.paragraphs[38]
abstract_text = (
    "Class Attendance Register is an enterprise-grade, offline-first native Android mobile application designed to replace "
    "traditional paper-based registers with a streamlined, error-free digital system for college faculty and academic administrations. "
    "In conventional academic institutions, lecturers manually record attendance on physical sheets, which is inherently vulnerable "
    "to proxy entries, accidental human recording errors, instructional time loss, paper wear-and-tear, and cumbersome manual "
    "calculations of semester-end attendance shortages.\n\n"
    "Class Attendance Register resolves these challenges through an intuitive mobile architecture developed using Android Studio, "
    "pure Java, and a local embedded SQLite database engine (AttendanceRegister.db, Schema Version 3). The application implements "
    "a rigorous multi-tiered academic structure supporting all six college semesters (Semester 1 through Semester 6), six independent "
    "class divisions (A1 to A6) with strict division isolation, and dynamic semester-wise subject curricula. After secure credential "
    "authentication, faculty select their assigned semester, division, subject, date, and period from interactive dropdown Spinners. "
    "The system queries the database and dynamically generates interactive student CheckBox controls in Java code with live real-time "
    "counters tracking Present, Absent, and Total students.\n\n"
    "A cornerstone innovation of the system is its Permanent Attendance Session Lock mechanism. Every lecture session is uniquely "
    "identified by the five-tuple (Semester, Division, Subject, Date, Period). Once attendance is saved through an atomic SQLite "
    "transaction, the session is permanently locked into a dedicated AttendanceSessions table with a database-level composite UNIQUE "
    "constraint. Any subsequent attempt to re-mark or overwrite that session is intercepted and blocked with the alert message: "
    "\"Attendance Already Submitted. This Session Is Locked.\" Furthermore, the application provides comprehensive administrative "
    "modules for Student and Subject management, alongside an advanced 4-tier filtered Attendance Report module that executes complex "
    "SQL aggregate queries (GROUP BY, COUNT) to compute individual attendance percentages and flag shortages below the mandatory "
    "university threshold of 75% in prominent red visual alerts. This dissertation report presents the complete system requirements, "
    "architectural design, database schemas, user interface layouts, implementation source code, and comprehensive test case verification results."
)
update_para(abstract_p, abstract_text, align=WD_ALIGN_PARAGRAPH.JUSTIFY, size_pt=10.5, color_rgb=BODY_COL)

print("Step 2: Updating Chapter 1 (Introduction)...")
update_para(doc.paragraphs[49],
    "Student attendance tracking is a fundamental academic and administrative responsibility across collegiate institutions. "
    "Student learning consistency, academic performance, eligibility for Karnatak University semester examinations, and statutory "
    "regulatory compliance strictly depend on maintaining an accurate, tamper-proof attendance registry. Class Attendance Register is "
    "a full-featured, offline-first native Android application built specifically for academic lecturers, laboratory instructors, and "
    "faculty members of BCA and science colleges.",
    align=WD_ALIGN_PARAGRAPH.JUSTIFY, size_pt=11, color_rgb=BODY_COL
)

update_para(doc.paragraphs[50],
    "Developed using Java, Android XML layouts, and the embedded SQLite database engine (AttendanceRegister.db) in Android Studio, "
    "the application allows faculty to create secure accounts, navigate a central Dashboard, select sessions across Semesters 1 to 6 "
    "and Divisions A1 to A6, dynamically render student rosters with programmatically generated CheckBoxes, record attendance by class period, "
    "enforce a permanent session-locking mechanism, and generate real-time attendance shortage reports highlighting students who fall below "
    "the mandatory 75% university attendance threshold.",
    align=WD_ALIGN_PARAGRAPH.JUSTIFY, size_pt=11, color_rgb=BODY_COL
)

update_para(doc.paragraphs[54],
    "Traditional classroom attendance methods in colleges suffer from severe operational inefficiencies:\n"
    "1. Excessive Classroom Time Loss: Spending 5 to 10 minutes calling roll numbers reduces instructional teaching time.\n"
    "2. Manual Calculation Errors: Calculating total classes conducted and individual attendance percentages manually is error-prone.\n"
    "3. Division and Section Mixing: Paper registers lack automated division validation, causing accidental student mixing across divisions.\n"
    "4. Duplicate Session Vulnerability: Faculty may accidentally mark attendance twice for the same period without any system alert.\n"
    "5. Delayed Shortage Detection: Students with attendance shortages below 75% are typically identified only at the close of the semester, leaving no opportunity for academic recovery.\n"
    "6. Physical Register Vulnerabilities: Paper registers are susceptible to loss, ink fading, water damage, and physical wear.",
    align=WD_ALIGN_PARAGRAPH.JUSTIFY, size_pt=11, color_rgb=BODY_COL
)

# Objectives
update_para(doc.paragraphs[56], "• Objective 1: To develop a secure Android application featuring faculty registration, login authentication, and a modern Dashboard interface.", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[57], "• Objective 2: To implement a 5-step dynamic class session selection covering Semesters 1 to 6, Divisions A1 to A6, and semester-specific subjects using custom Dropdown Spinners.", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[58], "• Objective 3: To dynamically query registered students from SQLite and generate interactive CheckBoxes in Java code, strictly isolating students by semester and division.", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[59], "• Objective 4: To implement a permanent attendance session-locking mechanism using a dedicated SQLite AttendanceSessions table, atomic transactions, and composite unique constraints.", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[60], "• Objective 5: To provide dedicated administrative modules for Student Management and Semester-Wise Subject Management with safe deletion checks.", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[61], "• Objective 6: To automate attendance percentage computation using SQLite GROUP BY queries and display real-time shortage alerts flagging students below 75% in distinct visual red badges.", size_pt=11, color_rgb=BODY_COL)

update_para(doc.paragraphs[63],
    "The operational scope of the project covers Android smartphones and tablets running Android 7.0 (Nougat, API 24) through "
    "Android 14 (API 34). The primary intended users are college faculty members, laboratory instructors, and academic coordinators. "
    "Because the app relies on an embedded SQLite database engine (AttendanceRegister.db), it functions with zero latency entirely "
    "offline without requiring an active internet connection, making it exceptionally reliable in campus classrooms with intermittent connectivity.",
    align=WD_ALIGN_PARAGRAPH.JUSTIFY, size_pt=11, color_rgb=BODY_COL
)

print("Step 3: Updating Chapter 3 (Technical Requirements Tables)...")
# Table 04 Hardware Requirements
t_hw = doc.tables[4]
format_cell(t_hw.cell(1, 0), "Processor", bold=False)
format_cell(t_hw.cell(1, 1), "Intel Core i3 / i5 / AMD Ryzen (2.0 GHz or higher)")
format_cell(t_hw.cell(1, 2), "ARMv7 / ARM64 Quad-Core 1.5 GHz or higher")

format_cell(t_hw.cell(2, 0), "RAM")
format_cell(t_hw.cell(2, 1), "8 GB RAM (16 GB recommended for Android Studio)")
format_cell(t_hw.cell(2, 2), "2 GB RAM or higher")

format_cell(t_hw.cell(3, 0), "Disk Storage")
format_cell(t_hw.cell(3, 1), "10 GB available SSD space for Android SDK & Gradle")
format_cell(t_hw.cell(3, 2), "50 MB available internal storage")

format_cell(t_hw.cell(4, 0), "Display / Screen")
format_cell(t_hw.cell(4, 1), "1366 x 768 or 1920 x 1080 Desktop Resolution")
format_cell(t_hw.cell(4, 2), "5.0\" to 6.8\" Screen (HD / FHD Mobile Display)")

# Table 05 Software Requirements
t_sw = doc.tables[5]
format_cell(t_sw.cell(1, 0), "Operating System")
format_cell(t_sw.cell(1, 1), "Microsoft Windows 10 / 11 (64-bit)")

format_cell(t_sw.cell(2, 0), "Integrated Development Environment")
format_cell(t_sw.cell(2, 1), "Android Studio Iguana / Jellyfish / Koala (Build 2024.1+)")

format_cell(t_sw.cell(3, 0), "Programming Language & JDK")
format_cell(t_sw.cell(3, 1), "Java (OpenJDK 17 LTS / Java 17 Compatibility)")

format_cell(t_sw.cell(4, 0), "Android SDK Configuration")
format_cell(t_sw.cell(4, 1), "Min SDK: 24 (Android 7.0), Target SDK: 34, Compile SDK: 34")

format_cell(t_sw.cell(5, 0), "Build System & Gradle Tools")
format_cell(t_sw.cell(5, 1), "Gradle 8.4 & Android Gradle Plugin (AGP) 8.2.2")

format_cell(t_sw.cell(6, 0), "Database Technology")
format_cell(t_sw.cell(6, 1), "SQLite v3 (Native Android SQLiteOpenHelper, Version 3)")

print("Step 4: Updating Chapter 4 (Project Description & Modules)...")
update_para(doc.paragraphs[78],
    "Class Attendance Register is an advanced Level 3 Android application designed to provide comprehensive, offline-first attendance management. "
    "The system is architected around nine robust functional modules and a centralized database helper class:",
    size_pt=11, color_rgb=BODY_COL
)

update_para(doc.paragraphs[79],
    "1. Faculty Authentication Module (RegisterActivity, LoginActivity): Handles faculty registration with input validation (unique faculty ID, valid email format, 6+ character password, matching confirmation) and credential verification against SQLite.",
    size_pt=11, color_rgb=BODY_COL
)
update_para(doc.paragraphs[80],
    "2. Modern Dashboard Module (DashboardActivity): Provides an interactive central navigation portal displaying a personalized faculty welcome banner and direct access cards for Mark Attendance, Attendance Reports, Manage Students, Manage Subjects, and Logout.",
    size_pt=11, color_rgb=BODY_COL
)
update_para(doc.paragraphs[81],
    "3. Class Session Selection Module (SelectClassActivity): Orchestrates a 5-step selection flow (Semester 1–6, Division A1–A6, Dynamic Subject, DatePicker, Period 1–6) equipped with real-time session lock detection and visual status badges.",
    size_pt=11, color_rgb=BODY_COL
)
update_para(doc.paragraphs[82],
    "4. Dynamic Attendance Workspace (AttendanceActivity): Programmatically queries enrolled students for the chosen semester and division, dynamically instantiates interactive CheckBox widgets in Java, updates live counters (Total, Present, Absent), and commits attendance.",
    size_pt=11, color_rgb=BODY_COL
)
update_para(doc.paragraphs[83],
    "5. Permanent Session Lock Module: Permanently freezes submitted attendance sessions across app restarts using the composite key (Semester, Division, Subject, Date, Period) within an atomic SQLite transaction in AttendanceSessions.",
    size_pt=11, color_rgb=BODY_COL
)

# Add paragraphs for modules 6, 7, 8 if not already present
update_para(doc.paragraphs[85],
    "As mandated by the BCA curriculum guidelines for the Level 3 Advanced Project, the application seamlessly integrates concepts from six foundational laboratory programs:\n"
    "• L4 (Sign Up / Login): Implemented in RegisterActivity and LoginActivity using explicit Intents with Extras and SQLite persistence.\n"
    "• L5 (Alert Dialog): Implemented across DashboardActivity (logout confirmation), StudentManagementActivity (delete confirmation), SubjectManagementActivity (safe deletion check), and AttendanceActivity (locked session block).\n"
    "• L6 (CheckBox & StringBuilder): Implemented in AttendanceActivity via dynamic programmatic CheckBox generation and StringBuilder attendance summary generation.\n"
    "• L9 (Spinner & ArrayAdapter): Implemented in SelectClassActivity, ReportActivity, and management dialogs for dynamic dropdown data binding.\n"
    "• L10 (TimePicker): Implemented in SelectClassActivity using TimePickerDialog for custom lecture period time configuration.\n"
    "• L12 (SQLite Database): Implemented in DBHelper.java with full CRUD operations, Cursor operations, transactions, and aggregate GROUP BY queries.",
    size_pt=11, color_rgb=BODY_COL
)

# Table 06 Reused Lab Programs
t_lab = doc.tables[6]
format_cell(t_lab.cell(1, 2), "Implemented in RegisterActivity and LoginActivity using explicit Intents with Extras and SQLite credential verification.")
format_cell(t_lab.cell(2, 2), "Implemented across Dashboard (logout), Student Management (deletion), Subject Management (integrity check), and locked session alerts.")
format_cell(t_lab.cell(3, 2), "Reused in AttendanceActivity to dynamically instantiate CheckBoxes in Java code and track live Present/Absent statistics.")
format_cell(t_lab.cell(4, 2), "Reused in SelectClassActivity for Semester, Division, Subject, Period, and in ReportActivity for advanced filtering.")
format_cell(t_lab.cell(5, 2), "Reused in SelectClassActivity using TimePickerDialog for custom lecture timetable scheduling.")
format_cell(t_lab.cell(6, 2), "Implemented in DBHelper.java managing 5 relational tables, ContentValues, Cursor, atomic transactions, and GROUP BY queries.")

# Business rules
update_para(doc.paragraphs[87], "Rule 1 (Credential Validation): Faculty registration requires a unique Faculty ID, valid email format, and a password of at least 6 characters with identical confirmation.", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[88], "Rule 2 (Division Isolation): Students belonging to Division A1 are strictly separated and can never appear in rosters or reports for Divisions A2 to A6.", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[89], "Rule 3 (Semester-Wise Dynamic Subject Filtering): The Subject dropdown loads exclusively the subjects belonging to the currently selected semester.", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[90], "Rule 4 (Permanent Session Locking): Once attendance is committed for a specific Semester, Division, Subject, Date, and Period, that session is permanently locked in SQLite and cannot be modified or re-submitted.", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[91], "Rule 5 (Safe Subject Deletion): A subject that has existing attendance records saved in the database cannot be deleted through the UI to preserve academic audit integrity.", size_pt=11, color_rgb=BODY_COL)

print("Step 5: Updating Chapter 5 (System Design & Data Dictionaries)...")
# ER schema description
update_para(doc.paragraphs[107],
    "The local SQLite database ('AttendanceRegister.db', Version 3) comprises five interconnected relational tables: "
    "Faculty, Students, Subjects, Attendance, and AttendanceSessions. Figure 5.4 displays their entity-relationship schema.",
    size_pt=11, color_rgb=BODY_COL
)

# Table 07: Faculty Table
t_fac = doc.tables[7]
format_cell(t_fac.cell(1, 0), "faculty_id")
format_cell(t_fac.cell(1, 1), "TEXT")
format_cell(t_fac.cell(1, 2), "PRIMARY KEY")
format_cell(t_fac.cell(1, 3), "Unique alphanumeric faculty identifier (e.g. FAC01)")

format_cell(t_fac.cell(2, 0), "faculty_name")
format_cell(t_fac.cell(2, 1), "TEXT")
format_cell(t_fac.cell(2, 2), "NOT NULL")
format_cell(t_fac.cell(2, 3), "Full academic name of the faculty member")

format_cell(t_fac.cell(3, 0), "email")
format_cell(t_fac.cell(3, 1), "TEXT")
format_cell(t_fac.cell(3, 2), "NOT NULL")
format_cell(t_fac.cell(3, 3), "Registered faculty email address")

format_cell(t_fac.cell(4, 0), "password")
format_cell(t_fac.cell(4, 1), "TEXT")
format_cell(t_fac.cell(4, 2), "NOT NULL")
format_cell(t_fac.cell(4, 3), "Plain/encrypted password credential")

# Table 08: Students Table
t_stu = doc.tables[8]
format_cell(t_stu.cell(1, 0), "student_id")
format_cell(t_stu.cell(1, 1), "INTEGER")
format_cell(t_stu.cell(1, 2), "PRIMARY KEY AUTOINCREMENT")
format_cell(t_stu.cell(1, 3), "Unique auto-generated student record identifier")

format_cell(t_stu.cell(2, 0), "register_number")
format_cell(t_stu.cell(2, 1), "TEXT")
format_cell(t_stu.cell(2, 2), "UNIQUE NOT NULL")
format_cell(t_stu.cell(2, 3), "University register number (e.g. 21BCA01)")

format_cell(t_stu.cell(3, 0), "student_name")
format_cell(t_stu.cell(3, 1), "TEXT")
format_cell(t_stu.cell(3, 2), "NOT NULL")
format_cell(t_stu.cell(3, 3), "Full registered name of the student")

format_cell(t_stu.cell(4, 0), "semester")
format_cell(t_stu.cell(4, 1), "TEXT")
format_cell(t_stu.cell(4, 2), "NOT NULL")
format_cell(t_stu.cell(4, 3), "Enrolled semester (Semester 1 to Semester 6)")

format_cell(t_stu.cell(5, 0), "division")
format_cell(t_stu.cell(5, 1), "TEXT")
format_cell(t_stu.cell(5, 2), "NOT NULL")
format_cell(t_stu.cell(5, 3), "Assigned class division (A1, A2, A3, A4, A5, A6)")

# Table 09: Attendance Table
t_att = doc.tables[9]
format_cell(t_att.cell(1, 0), "attendance_id")
format_cell(t_att.cell(1, 1), "INTEGER")
format_cell(t_att.cell(1, 2), "PRIMARY KEY AUTOINCREMENT")
format_cell(t_att.cell(1, 3), "Unique attendance entry identifier")

format_cell(t_att.cell(2, 0), "student_id")
format_cell(t_att.cell(2, 1), "INTEGER")
format_cell(t_att.cell(2, 2), "FOREIGN KEY (Students)")
format_cell(t_att.cell(2, 3), "Foreign key referencing student_id in Students table")

format_cell(t_att.cell(3, 0), "subject_id")
format_cell(t_att.cell(3, 1), "INTEGER")
format_cell(t_att.cell(3, 2), "FOREIGN KEY (Subjects)")
format_cell(t_att.cell(3, 3), "Foreign key referencing subject_id in Subjects table")

format_cell(t_att.cell(4, 0), "semester")
format_cell(t_att.cell(4, 1), "TEXT")
format_cell(t_att.cell(4, 2), "NOT NULL")
format_cell(t_att.cell(4, 3), "Semester associated with the lecture session")

format_cell(t_att.cell(5, 0), "division")
format_cell(t_att.cell(5, 1), "TEXT")
format_cell(t_att.cell(5, 2), "NOT NULL")
format_cell(t_att.cell(5, 3), "Division associated with the lecture session (A1–A6)")

format_cell(t_att.cell(6, 0), "attendance_date")
format_cell(t_att.cell(6, 1), "TEXT")
format_cell(t_att.cell(6, 2), "NOT NULL (YYYY-MM-DD)")
format_cell(t_att.cell(6, 3), "Calendar date on which attendance was recorded")

format_cell(t_att.cell(7, 0), "status")
format_cell(t_att.cell(7, 1), "TEXT")
format_cell(t_att.cell(7, 2), "NOT NULL")
format_cell(t_att.cell(7, 3), "Attendance state: 'Present' or 'Absent'")

print("Step 6: Updating Chapter 6 (User Interface Captions)...")
update_para(doc.paragraphs[119],
    "The faculty registration screen (RegisterActivity) allows new lecturers to establish credentials by entering their "
    "full name, faculty ID, email address, password, and password confirmation. The system enforces input validation to prevent duplicate accounts.",
    size_pt=11, color_rgb=BODY_COL
)
update_para(doc.paragraphs[123],
    "The login screen (LoginActivity) authenticates registered faculty credentials against records stored in the SQLite database. "
    "Upon successful authentication, the faculty session data is transferred to DashboardActivity via an explicit Intent.",
    size_pt=11, color_rgb=BODY_COL
)
update_para(doc.paragraphs[127],
    "SelectClassActivity provides a modern 5-step selection flow (Semester, Division A1–A6, Subject, Date, Period). "
    "The screen features real-time session lock detection displaying a status badge indicating whether the session is ready or already submitted and locked.",
    size_pt=11, color_rgb=BODY_COL
)
update_para(doc.paragraphs[131],
    "AttendanceActivity renders the primary attendance workspace. It queries students strictly matching the chosen semester and division, "
    "dynamically instantiates interactive CheckBox widgets in Java code, and displays live counters updating Present, Absent, and Total students in real time.",
    size_pt=11, color_rgb=BODY_COL
)
update_para(doc.paragraphs[135],
    "When a faculty member attempts to access an already-submitted session, the system intercepts the request and displays a blocking AlertDialog: "
    "\"Attendance Already Submitted. This Session Is Locked.\" Duplicate submissions are permanently prevented at both UI and database levels.",
    size_pt=11, color_rgb=BODY_COL
)
update_para(doc.paragraphs[139],
    "ReportActivity queries the SQLite database using SQL aggregate functions to display each student's conducted lectures, attended lectures, "
    "and attendance percentage. Students falling below the mandatory university threshold of 75% are highlighted in prominent red shortage alert cards.",
    size_pt=11, color_rgb=BODY_COL
)
update_para(doc.paragraphs[143],
    "Figure 6.7 demonstrates verification of the underlying 'attendance' and 'AttendanceSessions' tables in DB Browser for SQLite, "
    "confirming relational key linking, semester and division mapping, period indices, and session lock records.",
    size_pt=11, color_rgb=BODY_COL
)

print("Step 7: Updating Chapter 7 (Implementation Details & Code Snippets)...")
# Table 10: Project Directory Structure
t_dir = doc.tables[10]
actual_dir_structure = (
    "Class Attendance Register/\n"
    "├── app/\n"
    "│   ├── build.gradle (compileSdk 34, Java 17 compatibility, Material 1.11.0)\n"
    "│   ├── proguard-rules.pro\n"
    "│   └── src/main/\n"
    "│       ├── AndroidManifest.xml (All 8 Activities declared with launcher)\n"
    "│       ├── java/com/example/attendanceregister/\n"
    "│       │   ├── MainActivity.java (Lifecycle demonstration & Log.d diagnostics)\n"
    "│       │   ├── LoginActivity.java (Faculty credential verification)\n"
    "│       │   ├── RegisterActivity.java (Faculty account creation & validation)\n"
    "│       │   ├── DashboardActivity.java (Central navigation portal & logout dialog)\n"
    "│       │   ├── SelectClassActivity.java (Semester, Division, Dynamic Subject, Period)\n"
    "│       │   ├── AttendanceActivity.java (Dynamic CheckBoxes, Live counters, Lock)\n"
    "│       │   ├── ReportActivity.java (4-tier filter, GROUP BY aggregate analytics)\n"
    "│       │   ├── StudentManagementActivity.java (Student CRUD, semester & division)\n"
    "│       │   ├── SubjectManagementActivity.java (Semester-wise subject CRUD & safe deletion)\n"
    "│       │   ├── DBHelper.java (SQLiteOpenHelper, 5 tables, atomic transactions)\n"
    "│       │   ├── Student.java (Student data model with semester & division)\n"
    "│       │   ├── Subject.java (Subject model with semester association)\n"
    "│       │   ├── Attendance.java (Attendance transaction record model)\n"
    "│       │   ├── AttendanceReportModel.java (Aggregated analytics model)\n"
    "│       │   ├── StudentManagementAdapter.java (Student list custom adapter)\n"
    "│       │   ├── SubjectAdapter.java (Subject list custom adapter)\n"
    "│       │   └── AttendanceReportAdapter.java (Report card list adapter)\n"
    "│       └── res/\n"
    "│           ├── layout/ (11 XML layouts: activities, custom cards, dialogs)\n"
    "│           ├── drawable/ (Vector assets: ic_lock, ic_attendance, ic_report, badges)\n"
    "│           └── values/ (colors.xml, strings.xml, themes.xml)\n"
    "├── build.gradle (Project-level configuration, AGP 8.2.2)\n"
    "├── gradle.properties (AndroidX & JVM heap options)\n"
    "└── settings.gradle (Dependency resolution management)"
)
format_cell(t_dir.cell(0, 0), actual_dir_structure, size_pt=9.0)

# Table 11: Dynamic CheckBox Snippet
t_snip1 = doc.tables[11]
actual_code_snippet1 = (
    "// Dynamic Student CheckBox Instantiation in Java (AttendanceActivity.java)\n"
    "List<Student> students = dbHelper.getStudentsBySemesterAndDivision(semester, division);\n"
    "llStudentsContainer.removeAllViews();\n"
    "for (Student student : students) {\n"
    "    CardView cardView = new CardView(this);\n"
    "    cardView.setRadius(12f);\n"
    "    cardView.setCardElevation(3f);\n"
    "    LinearLayout rowLayout = new LinearLayout(this);\n"
    "    rowLayout.setOrientation(LinearLayout.HORIZONTAL);\n"
    "    rowLayout.setGravity(Gravity.CENTER_VERTICAL);\n"
    "\n"
    "    CheckBox checkBox = new CheckBox(this);\n"
    "    checkBox.setChecked(false); // Default Absent\n"
    "    studentCheckBoxMap.put(student.getId(), checkBox);\n"
    "\n"
    "    TextView tvName = new TextView(this);\n"
    "    tvName.setText(student.getRegisterNumber() + \" - \" + student.getName());\n"
    "    tvName.setTextSize(15f);\n"
    "\n"
    "    checkBox.setOnCheckedChangeListener((btn, isChecked) -> {\n"
    "        updateRowStatus(student.getId(), isChecked);\n"
    "        updateCounters(); // Live counter update\n"
    "    });\n"
    "\n"
    "    rowLayout.addView(checkBox);\n"
    "    rowLayout.addView(tvName);\n"
    "    cardView.addView(rowLayout);\n"
    "    llStudentsContainer.addView(cardView);\n"
    "}"
)
format_cell(t_snip1.cell(0, 0), actual_code_snippet1, size_pt=9.0)

# Table 12: Aggregate SQL Query Snippet
t_snip2 = doc.tables[12]
actual_code_snippet2 = (
    "// Permanent Session Locking & Atomic Transaction Logic (DBHelper.java)\n"
    "public boolean lockAndSaveSessionAttendance(String semester, String division, int subjectId,\n"
    "                                            String date, String period,\n"
    "                                            List<Student> students, Map<Integer, Boolean> map) {\n"
    "    SQLiteDatabase db = this.getWritableDatabase();\n"
    "    if (isSessionLocked(semester, division, subjectId, date, period)) return false;\n"
    "    db.beginTransaction();\n"
    "    try {\n"
    "        for (Student s : students) {\n"
    "            ContentValues cv = new ContentValues();\n"
    "            cv.put(COL_ATT_STUDENT_ID, s.getId());\n"
    "            cv.put(COL_ATT_SUBJECT_ID, subjectId);\n"
    "            cv.put(COL_ATT_SEMESTER, semester);\n"
    "            cv.put(COL_ATT_DIVISION, division);\n"
    "            cv.put(COL_ATT_DATE, date);\n"
    "            cv.put(COL_ATT_PERIOD, period);\n"
    "            cv.put(COL_ATT_STATUS, Boolean.TRUE.equals(map.get(s.getId())) ? \"Present\" : \"Absent\");\n"
    "            db.insertOrThrow(TABLE_ATTENDANCE, null, cv);\n"
    "        }\n"
    "        ContentValues scv = new ContentValues();\n"
    "        scv.put(COL_SESSION_SEMESTER, semester);\n"
    "        scv.put(COL_SESSION_DIVISION, division);\n"
    "        scv.put(COL_SESSION_SUBJECT_ID, subjectId);\n"
    "        scv.put(COL_SESSION_DATE, date);\n"
    "        scv.put(COL_SESSION_PERIOD, period);\n"
    "        scv.put(COL_SESSION_STATUS, \"LOCKED\");\n"
    "        scv.put(COL_SESSION_SUBMITTED_AT, new SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\").format(new Date()));\n"
    "        db.insertOrThrow(TABLE_SESSIONS, null, scv); // Enforces UNIQUE constraint\n"
    "        db.setTransactionSuccessful();\n"
    "        return true;\n"
    "    } catch (Exception e) {\n"
    "        return false; // Automatically rolled back\n"
    "    } finally {\n"
    "        db.endTransaction();\n"
    "    }\n"
    "}"
)
format_cell(t_snip2.cell(0, 0), actual_code_snippet2, size_pt=9.0)

# Execution steps
update_para(doc.paragraphs[156], "Step 1: Open Android Studio (Hedgehog, Iguana, Jellyfish, or newer) and select 'Open' -> browse to the project directory.", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[157], "Step 2: Allow Gradle to synchronize dependencies (AndroidX AppCompat 1.6.1, Google Material 1.11.0, ConstraintLayout 2.1.4).", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[158], "Step 3: Connect an Android physical smartphone via USB debugging or start an Android Virtual Device (AVD) running API 24 or higher.", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[159], "Step 4: Click 'Run app' (Shift + F10). The APK will compile and launch the main application entry portal.", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[160], "Step 5: Log in with default demo faculty credentials (ID: FAC01 / Password: admin123) or register a new faculty account.", size_pt=11, color_rgb=BODY_COL)
update_para(doc.paragraphs[161], "Step 6: To inspect SQLite tables, navigate to View -> Tool Windows -> Device File Explorer -> /data/data/com.example.attendanceregister/databases/AttendanceRegister.db and inspect using DB Browser for SQLite.", size_pt=11, color_rgb=BODY_COL)

print("Step 7: Updating Chapter 8 (Test Cases Table 13)...")
# Table 13: 16 rows. Let's update all test cases with actual implemented features
test_cases_data = [
    ("TC01", "Auth", "Faculty Registration with Valid Inputs", "Name='Prof. Sharma', ID='FAC02', Email='prof@college.edu', Pass='admin123'", "Account saved in SQLite; Toast: 'Registration successful'", "PASS"),
    ("TC02", "Auth", "Registration Empty Field Validation", "Submit registration with blank Faculty ID or Email", "Field displays validation error; database write aborted", "PASS"),
    ("TC03", "Auth", "Duplicate Faculty ID Validation", "Register with already existing ID 'FAC01'", "Toast: 'Faculty ID already exists! Please use a unique ID.'", "PASS"),
    ("TC04", "Auth", "Password Mismatch Check", "Password='password123', Confirm='pass456'", "Field error: 'Passwords do not match'; submission blocked", "PASS"),
    ("TC05", "Auth", "Valid Faculty Login", "ID='FAC01', Password='admin123'", "Toast: 'Login successful!'; navigates to DashboardActivity", "PASS"),
    ("TC06", "Auth", "Invalid Credentials Login", "ID='FAC01', Password='wrongpassword'", "Toast: 'Invalid credentials! Please check your ID and Password.'", "PASS"),
    ("TC07", "Class", "Semester & Division Selection", "Select Semester 5 and Division A1", "Selection verified; updates Subject Spinner with Sem 5 subjects", "PASS"),
    ("TC08", "Class", "Division Isolation Verification", "Switch division from A1 to A2", "Students in A1 (21BCA01–04) are strictly excluded from A2", "PASS"),
    ("TC09", "Class", "Dynamic Semester-Wise Subject Loading", "Switch Semester from 5 to 1", "Subject Spinner instantly clears and loads Semester 1 subjects", "PASS"),
    ("TC10", "Attendance", "Dynamic Student CheckBox Rendering", "Load Semester 5 Division A1 roster", "Instantiates CheckBoxes dynamically in Java for enrolled students", "PASS"),
    ("TC11", "Attendance", "Mark Attendance & Live Counters", "Toggle CheckBoxes for 3 students Present", "Counters update live: Total=4, Present=3, Absent=1", "PASS"),
    ("TC12", "Lock", "Permanent Attendance Session Locking", "Save attendance for Sem 5, Div A1, Period 1", "Session written to AttendanceSessions; status marked 'LOCKED'", "PASS"),
    ("TC13", "Lock", "Duplicate Session Re-Submission Block", "Attempt to mark Period 1 again", "AlertDialog: 'Attendance Already Submitted. This Session Is Locked.'", "PASS"),
    ("TC14", "Lock", "Session Lock Persistence Across Restart", "Close application, restart, and select Period 1", "Status indicator displays 'STATUS: SUBMITTED / LOCKED' from SQLite", "PASS"),
    ("TC15", "Lock", "Session Independence Verification", "Select Period 2 on same date and division", "Status displays 'STATUS: NOT SUBMITTED'; marking allowed", "PASS"),
]

t_tc = doc.tables[13]
for idx, tc in enumerate(test_cases_data):
    row_idx = idx + 1
    if row_idx < len(t_tc.rows):
        format_cell(t_tc.cell(row_idx, 0), tc[0], bold=True, size_pt=9.0)
        format_cell(t_tc.cell(row_idx, 1), tc[1], size_pt=9.0)
        format_cell(t_tc.cell(row_idx, 2), tc[2], size_pt=9.0)
        format_cell(t_tc.cell(row_idx, 3), tc[3], size_pt=8.5)
        format_cell(t_tc.cell(row_idx, 4), tc[4], size_pt=8.5)
        format_cell(t_tc.cell(row_idx, 5), tc[5], bold=True, size_pt=9.0, color_rgb=(46, 125, 50))

print("Step 8: Updating Chapter 9 (Conclusion & Future Enhancements)...")
update_para(doc.paragraphs[166],
    "Class Attendance Register demonstrates the successful engineering of a robust, modern, and highly secure Android attendance "
    "management solution designed specifically for college faculty. By replacing fragile paper records with an automated offline-first "
    "digital platform, the application eliminates human calculation errors, prevents accidental duplicate session recording through a "
    "permanent session-locking mechanism, guarantees division isolation, and provides immediate visibility into student attendance shortages.\n\n"
    "The project comprehensively fulfills all academic requirements of the BCA VI Semester curriculum, integrating practical concepts "
    "from six foundational laboratory programs (L4: Signup/Login with Intents, L5: AlertDialog, L6: CheckBox and StringBuilder, L9: Spinner "
    "and ArrayAdapter, L10: TimePicker, and L12: SQLite Database). The implementation of dynamic UI generation, atomic SQLite transactions, "
    "composite unique session constraints, and SQL aggregate reporting confirms the robust engineering and academic excellence of the application.",
    align=WD_ALIGN_PARAGRAPH.JUSTIFY, size_pt=11, color_rgb=BODY_COL
)

# Save updated document
doc.save(output_path)
print("Saved updated Word document successfully at:", output_path)
