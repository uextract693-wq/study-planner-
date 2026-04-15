package com.example.smartstudyplanner.model;

import java.time.LocalDate;

public class Subject {
    private String name;
    private double totalHoursNeeded;
    private double hoursCompleted;
    private LocalDate deadline;
    private int difficulty;

    public Subject(String name, double totalHoursNeeded, double hoursCompleted, LocalDate deadline, int difficulty) {
        this.name = name;
        this.totalHoursNeeded = totalHoursNeeded;
        this.hoursCompleted = hoursCompleted;
        this.deadline = deadline;
        this.difficulty = difficulty;
    }

    public String getName() {
        return name;
    }

    public double getTotalHoursNeeded() {
        return totalHoursNeeded;
    }

    public double getHoursCompleted() {
        return hoursCompleted;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public double getHoursLeft() {
        return Math.max(0.0, totalHoursNeeded - hoursCompleted);
    }
}
