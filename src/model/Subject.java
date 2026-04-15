package model;

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

    public void setName(String name) {
        this.name = name;
    }

    public double getTotalHoursNeeded() {
        return totalHoursNeeded;
    }

    public void setTotalHoursNeeded(double totalHoursNeeded) {
        this.totalHoursNeeded = totalHoursNeeded;
    }

    public double getHoursCompleted() {
        return hoursCompleted;
    }

    public void setHoursCompleted(double hoursCompleted) {
        this.hoursCompleted = hoursCompleted;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public double getHoursLeft() {
        double hoursLeft = totalHoursNeeded - hoursCompleted;
        return Math.max(0.0, hoursLeft);
    }
}
