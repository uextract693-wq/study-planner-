package com.example.smartstudyplanner;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartstudyplanner.model.Subject;
import com.example.smartstudyplanner.service.PlannerService;
import com.example.smartstudyplanner.ui.OutputTableViews;
import com.example.smartstudyplanner.util.FileUtil;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private PlannerService plannerService;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private EditText etName;
    private EditText etTotalHours;
    private EditText etCompletedHours;
    private EditText etDeadline;
    private EditText etDifficulty;
    private EditText etMissedDays;
    private FrameLayout outputContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        plannerService = new PlannerService(FileUtil.loadSubjects(this));
        bindViews();
        wireActions();
        showOutputText(getString(R.string.output_placeholder));
    }

    @Override
    protected void onPause() {
        super.onPause();
        FileUtil.saveSubjects(this, plannerService.getAllSubjects());
    }

    private void bindViews() {
        etName = findViewById(R.id.etName);
        etTotalHours = findViewById(R.id.etTotalHours);
        etCompletedHours = findViewById(R.id.etCompletedHours);
        etDeadline = findViewById(R.id.etDeadline);
        etDifficulty = findViewById(R.id.etDifficulty);
        etMissedDays = findViewById(R.id.etMissedDays);
        outputContainer = findViewById(R.id.outputContainer);
    }

    private void wireActions() {
        Button btnAdd = findViewById(R.id.btnAddSubject);
        Button btnPlan = findViewById(R.id.btnViewPlan);
        Button btnSubjects = findViewById(R.id.btnViewSubjects);
        Button btnSimulate = findViewById(R.id.btnSimulate);

        btnAdd.setOnClickListener(v -> addSubject());
        btnPlan.setOnClickListener(v -> startActivity(new Intent(this, StudyPlanActivity.class)));
        btnSubjects.setOnClickListener(v -> showSubjects());
        btnSimulate.setOnClickListener(v -> simulateMissingDays());
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void showOutputText(String text) {
        outputContainer.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText(text);
        int p = dp(12);
        tv.setPadding(p, p, p, p);
        tv.setTextColor(ContextCompat.getColor(this, R.color.ssp_text_primary));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        outputContainer.addView(tv);
    }

    private void showOutputTable(TableLayout table) {
        outputContainer.removeAllViews();
        outputContainer.addView(OutputTableViews.wrapScrollable(this, table));
    }

    private void showOutputVertical(View top, View bottom) {
        outputContainer.removeAllViews();
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(top);
        ll.addView(bottom);
        outputContainer.addView(ll);
    }

    private TextView outputCaption(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        int p = dp(12);
        tv.setPadding(p, p, p, dp(6));
        tv.setTextColor(ContextCompat.getColor(this, R.color.ssp_text_primary));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        return tv;
    }

    private void addSubject() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            toast("Input cannot be empty. Please try again.");
            return;
        }

        String totalStr = etTotalHours.getText().toString().trim();
        String completedStr = etCompletedHours.getText().toString().trim();
        String difficultyStr = etDifficulty.getText().toString().trim();
        String deadlineStr = etDeadline.getText().toString().trim();

        if (totalStr.isEmpty() || completedStr.isEmpty() || difficultyStr.isEmpty() || deadlineStr.isEmpty()) {
            toast("Please fill all fields with valid values.");
            return;
        }

        Double total = parseDoubleOrNull(totalStr);
        if (total == null) {
            toast("Invalid number. Please try again.");
            return;
        }
        Double completed = parseDoubleOrNull(completedStr);
        if (completed == null) {
            toast("Invalid number. Please try again.");
            return;
        }
        Integer difficulty = parseIntOrNull(difficultyStr);
        if (difficulty == null) {
            toast("Invalid number. Please try again.");
            return;
        }

        LocalDate deadline;
        try {
            deadline = LocalDate.parse(deadlineStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            toast("Date format must be YYYY-MM-DD.");
            return;
        }

        if (total < 0 || completed < 0) {
            toast("Invalid number. Please try again.");
            return;
        }
        if (completed > total) {
            toast("Completed hours cannot exceed total hours required.");
            return;
        }
        if (difficulty < 1 || difficulty > 5) {
            toast("Difficulty must be between 1 and 5.");
            return;
        }

        plannerService.addSubject(new Subject(name, total, completed, deadline, difficulty));
        FileUtil.saveSubjects(this, plannerService.getAllSubjects());
        toast("Subject added successfully.");
        clearInputFields();

        TableLayout table = OutputTableViews.subjectSavedTable(this,
                name,
                decimalFormat.format(total),
                decimalFormat.format(completed),
                deadline.format(DATE_FORMATTER),
                difficulty);
        showOutputVertical(
                outputCaption(getString(R.string.subject_saved_title)),
                OutputTableViews.wrapScrollable(this, table));
    }

    private void showSubjects() {
        List<Subject> subjects = plannerService.getSubjectsSortedByUrgency();
        if (subjects.isEmpty()) {
            showOutputText(getString(R.string.empty_subjects_message));
            return;
        }
        showOutputTable(OutputTableViews.subjectsTable(this, subjects, LocalDate.now()));
    }

    private void simulateMissingDays() {
        if (plannerService.getAllSubjects().isEmpty()) {
            showOutputText(getString(R.string.empty_simulation_message));
            return;
        }

        String missedStr = etMissedDays.getText().toString().trim();
        if (missedStr.isEmpty()) {
            toast("Enter number of days to miss.");
            return;
        }
        Integer missedDays = parseIntOrNull(missedStr);
        if (missedDays == null) {
            toast("Invalid number. Please try again.");
            return;
        }
        if (missedDays < 0) {
            toast("Number of days cannot be negative. Please try again.");
            return;
        }

        List<PlannerService.SimulationResult> results = plannerService.simulateMissingDays(missedDays);
        if (results.isEmpty()) {
            showOutputText(getString(R.string.simulation_no_valid_message));
            return;
        }

        TextView quote = new TextView(this);
        quote.setText("\"" + "If you miss " + missedDays + " days:" + "\"");
        int p = dp(12);
        quote.setPadding(p, p, p, dp(8));
        quote.setTextColor(ContextCompat.getColor(this, R.color.ssp_text_primary));
        quote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        TableLayout simTable = OutputTableViews.simulationTable(this, results);
        showOutputVertical(quote, OutputTableViews.wrapScrollable(this, simTable));
    }

    private Double parseDoubleOrNull(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIntOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void clearInputFields() {
        etName.setText("");
        etTotalHours.setText("");
        etCompletedHours.setText("");
        etDeadline.setText("");
        etDifficulty.setText("");
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
