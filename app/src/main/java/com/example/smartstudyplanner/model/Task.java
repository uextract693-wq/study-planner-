package com.example.smartstudyplanner.model;

public class Task {
    private final String subjectName;
    private final double hoursToStudyToday;

    public Task(String subjectName, double hoursToStudyToday) {
        this.subjectName = subjectName;
        this.hoursToStudyToday = hoursToStudyToday;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public double getHoursToStudyToday() {
        return hoursToStudyToday;
    }
}
