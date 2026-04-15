package com.example.smartstudyplanner;

import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartstudyplanner.model.Task;
import com.example.smartstudyplanner.service.PlannerService;
import com.example.smartstudyplanner.ui.OutputTableViews;
import com.example.smartstudyplanner.util.FileUtil;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class StudyPlanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_plan);

        MaterialToolbar toolbar = findViewById(R.id.toolbarStudyPlan);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        TextView tvEmpty = findViewById(R.id.tvStudyPlanEmpty);
        HorizontalScrollView hsv = findViewById(R.id.hsvStudyPlanTable);

        PlannerService plannerService = new PlannerService(FileUtil.loadSubjects(this));
        List<Task> tasks = plannerService.generateDailyPlan();

        if (tasks.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(R.string.study_plan_empty_message);
            hsv.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            hsv.setVisibility(View.VISIBLE);
            hsv.removeAllViews();
            double total = 0.0;
            for (Task t : tasks) {
                total += t.getHoursToStudyToday();
            }
            hsv.addView(OutputTableViews.dailyPlanTable(this, tasks, total));
        }
    }
}
