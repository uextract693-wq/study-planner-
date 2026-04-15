package com.example.smartstudyplanner.service;

import com.example.smartstudyplanner.model.Subject;
import com.example.smartstudyplanner.model.Task;
import com.example.smartstudyplanner.util.DateUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlannerService {
    private final List<Subject> subjects;

    public PlannerService(List<Subject> subjects) {
        this.subjects = new ArrayList<>(subjects);
    }

    public void addSubject(Subject subject) {
        subjects.add(subject);
    }

    public List<Subject> getAllSubjects() {
        return new ArrayList<>(subjects);
    }

    public List<Subject> getSubjectsSortedByUrgency() {
        List<Subject> sorted = new ArrayList<>(subjects);
        sorted.sort(Comparator.comparing(Subject::getDeadline)
                .thenComparing(Subject::getDifficulty, Comparator.reverseOrder()));
        return sorted;
    }

    public String getRiskStatus(Subject subject) {
        long daysRemaining = DateUtil.getDaysBetween(LocalDate.now(), subject.getDeadline());
        return daysRemaining < 2 ? "HIGH RISK" : "NORMAL";
    }

    public List<Task> generateDailyPlan() {
        List<SubjectPriority> priorities = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Subject subject : subjects) {
            long daysRemaining = DateUtil.getDaysBetween(today, subject.getDeadline());
            double hoursLeft = subject.getHoursLeft();
            if (daysRemaining < 0 || hoursLeft <= 0) {
                continue;
            }

            long effectiveDays = daysRemaining == 0 ? 1 : daysRemaining;
            double dailyHours = hoursLeft / effectiveDays;

            // Internal only: used for sorting output tasks by urgency.
            double priorityScore = dailyHours * subject.getDifficulty();
            priorities.add(new SubjectPriority(subject.getName(), roundToTwo(dailyHours), priorityScore));
        }

        priorities.sort((a, b) -> Double.compare(b.priorityScore, a.priorityScore));

        List<Task> tasks = new ArrayList<>();
        for (SubjectPriority priority : priorities) {
            tasks.add(new Task(priority.subjectName, priority.dailyHours));
        }
        return tasks;
    }

    public List<SimulationResult> simulateMissingDays(int missedDays) {
        List<SimulationResult> results = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Subject subject : subjects) {
            long daysRemaining = DateUtil.getDaysBetween(today, subject.getDeadline());
            double hoursLeft = subject.getHoursLeft();
            if (daysRemaining < 0 || hoursLeft <= 0) {
                continue;
            }

            long effectiveDays = daysRemaining == 0 ? 1 : daysRemaining;
            long newDaysRemaining = effectiveDays - missedDays;
            if (newDaysRemaining <= 0) {
                results.add(new SimulationResult(subject.getName(), 0.0, true));
            } else {
                double daily = roundToTwo(hoursLeft / newDaysRemaining);
                results.add(new SimulationResult(subject.getName(), daily, false));
            }
        }

        return results;
    }

    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static class SubjectPriority {
        private final String subjectName;
        private final double dailyHours;
        private final double priorityScore;

        private SubjectPriority(String subjectName, double dailyHours, double priorityScore) {
            this.subjectName = subjectName;
            this.dailyHours = dailyHours;
            this.priorityScore = priorityScore;
        }
    }

    public static class SimulationResult {
        private final String subjectName;
        private final double hoursPerDay;
        private final boolean impossible;

        public SimulationResult(String subjectName, double hoursPerDay, boolean impossible) {
            this.subjectName = subjectName;
            this.hoursPerDay = hoursPerDay;
            this.impossible = impossible;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public double getHoursPerDay() {
            return hoursPerDay;
        }

        public boolean isImpossible() {
            return impossible;
        }
    }
}
