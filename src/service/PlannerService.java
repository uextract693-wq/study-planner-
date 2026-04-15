package service;

import model.Subject;
import model.Task;
import util.DateUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlannerService {
    private final List<Subject> subjects;

    public PlannerService() {
        this.subjects = new ArrayList<>();
    }

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
        if (daysRemaining < 2) {
            return "HIGH RISK";
        }
        return "NORMAL";
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

            long effectiveDays = (daysRemaining == 0) ? 1 : daysRemaining;
            double dailyHours = hoursLeft / effectiveDays;

            // priorityScore stays internal to sorting logic by design.
            double priorityScore = dailyHours * subject.getDifficulty();
            priorities.add(new SubjectPriority(subject.getName(), dailyHours, priorityScore));
        }

        // Explicit descending sort by priority score.
        priorities.sort((a, b) -> Double.compare(b.priorityScore(), a.priorityScore()));

        List<Task> tasks = new ArrayList<>();
        for (SubjectPriority priority : priorities) {
            tasks.add(new Task(priority.subjectName(), roundToTwo(priority.dailyHours())));
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

            long effectiveDays = (daysRemaining == 0) ? 1 : daysRemaining;
            long newDaysRemaining = effectiveDays - missedDays;

            if (newDaysRemaining <= 0) {
                results.add(new SimulationResult(subject.getName(), 0.0, true));
            } else {
                double newDailyHours = hoursLeft / newDaysRemaining;
                results.add(new SimulationResult(subject.getName(), roundToTwo(newDailyHours), false));
            }
        }
        return results;
    }

    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record SubjectPriority(String subjectName, double dailyHours, double priorityScore) {
    }

    public record SimulationResult(String subjectName, double hoursPerDay, boolean impossible) {
    }
}
