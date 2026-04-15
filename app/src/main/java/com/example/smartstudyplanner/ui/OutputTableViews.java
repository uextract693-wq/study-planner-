package com.example.smartstudyplanner.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.smartstudyplanner.R;
import com.example.smartstudyplanner.model.Subject;
import com.example.smartstudyplanner.model.Task;
import com.example.smartstudyplanner.service.PlannerService;
import com.example.smartstudyplanner.util.DateUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class OutputTableViews {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private OutputTableViews() {
    }

    private static int dp(Context c, int v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static TextView cell(Context c, String text, boolean header) {
        TextView tv = new TextView(c);
        tv.setText(text);
        int p = dp(c, 10);
        tv.setPadding(p, p, p, p);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tv.setTextColor(ContextCompat.getColor(c, R.color.ssp_text_primary));
        if (header) {
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setBackgroundColor(ContextCompat.getColor(c, R.color.ssp_divider));
        }
        return tv;
    }

    private static TableRow.LayoutParams rowParamWeight(float weight) {
        return new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight);
    }

    public static TableLayout dailyPlanTable(Context c, List<Task> tasks, double totalHours) {
        TableLayout table = new TableLayout(c);
        table.setStretchAllColumns(true);
        TableRow header = new TableRow(c);
        header.addView(cell(c, "Subject Name", true), rowParamWeight(1f));
        header.addView(cell(c, "Hours", true), rowParamWeight(1f));
        table.addView(header);

        for (Task task : tasks) {
            TableRow row = new TableRow(c);
            row.addView(cell(c, task.getSubjectName(), false), rowParamWeight(1f));
            row.addView(cell(c, String.format("%s hrs", formatTwo(task.getHoursToStudyToday())), false), rowParamWeight(1f));
            table.addView(row);
        }

        TableRow totalRow = new TableRow(c);
        TextView totalLabel = cell(c, "Total Study Time", true);
        TextView totalVal = cell(c, String.format("%s hrs", formatTwo(totalHours)), true);
        totalRow.addView(totalLabel, rowParamWeight(1f));
        totalRow.addView(totalVal, rowParamWeight(1f));
        table.addView(totalRow);
        return table;
    }

    public static TableLayout subjectsTable(Context c, List<Subject> subjects, LocalDate today) {
        TableLayout table = new TableLayout(c);
        table.setStretchAllColumns(true);
        TableRow header = new TableRow(c);
        String[] titles = {"Name", "Hours Left", "Deadline", "Difficulty", "Status"};
        for (String t : titles) {
            header.addView(cell(c, t, true), rowParamWeight(1f));
        }
        table.addView(header);

        for (Subject subject : subjects) {
            long daysRemaining = DateUtil.getDaysBetween(today, subject.getDeadline());
            String status = daysRemaining <= 2 ? "HIGH RISK" : "NORMAL";
            TableRow row = new TableRow(c);
            row.addView(cell(c, subject.getName(), false), rowParamWeight(1f));
            row.addView(cell(c, String.format("%s hrs", formatTwo(subject.getHoursLeft())), false), rowParamWeight(1f));
            row.addView(cell(c, subject.getDeadline().format(DATE_FORMATTER), false), rowParamWeight(1f));
            row.addView(cell(c, String.valueOf(subject.getDifficulty()), false), rowParamWeight(1f));
            row.addView(cell(c, status, false), rowParamWeight(1f));
            table.addView(row);
        }
        return table;
    }

    public static TableLayout simulationTable(Context c, List<PlannerService.SimulationResult> results) {
        TableLayout table = new TableLayout(c);
        table.setStretchAllColumns(true);
        TableRow header = new TableRow(c);
        header.addView(cell(c, "Subject", true), rowParamWeight(1f));
        header.addView(cell(c, "Outcome", true), rowParamWeight(2f));
        table.addView(header);

        for (PlannerService.SimulationResult result : results) {
            TableRow row = new TableRow(c);
            row.addView(cell(c, result.getSubjectName(), false), rowParamWeight(1f));
            String outcome = result.isImpossible()
                    ? "IMPOSSIBLE (deadline missed)"
                    : String.format("%s hrs/day", formatTwo(result.getHoursPerDay()));
            row.addView(cell(c, outcome, false), rowParamWeight(2f));
            table.addView(row);
        }
        return table;
    }

    public static TableLayout subjectSavedTable(Context c,
                                                String name,
                                                String totalHours,
                                                String completedHours,
                                                String deadline,
                                                int difficulty) {
        TableLayout table = new TableLayout(c);
        table.setStretchAllColumns(true);
        TableRow header = new TableRow(c);
        header.addView(cell(c, "Field", true), rowParamWeight(1f));
        header.addView(cell(c, "Value", true), rowParamWeight(2f));
        table.addView(header);

        addPairRow(table, c, "Name", name);
        addPairRow(table, c, "Total Hours", totalHours + " hrs");
        addPairRow(table, c, "Hours Completed", completedHours + " hrs");
        addPairRow(table, c, "Deadline", deadline);
        addPairRow(table, c, "Difficulty", String.valueOf(difficulty));
        return table;
    }

    private static void addPairRow(TableLayout table, Context c, String k, String v) {
        TableRow row = new TableRow(c);
        row.addView(cell(c, k, false), rowParamWeight(1f));
        row.addView(cell(c, v, false), rowParamWeight(2f));
        table.addView(row);
    }

    private static String formatTwo(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }

    public static HorizontalScrollView wrapScrollable(Context c, TableLayout table) {
        HorizontalScrollView hsv = new HorizontalScrollView(c);
        hsv.setHorizontalScrollBarEnabled(true);
        hsv.setFillViewport(true);
        hsv.addView(table);
        return hsv;
    }
}
