package model;

public class Task {
    private String subjectName;
    private double hoursToStudyToday;

    public Task(String subjectName, double hoursToStudyToday) {
        this.subjectName = subjectName;
        this.hoursToStudyToday = hoursToStudyToday;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public double getHoursToStudyToday() {
        return hoursToStudyToday;
    }

    public void setHoursToStudyToday(double hoursToStudyToday) {
        this.hoursToStudyToday = hoursToStudyToday;
    }
}
