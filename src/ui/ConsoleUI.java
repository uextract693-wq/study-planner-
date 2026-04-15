package ui;

import model.Subject;
import model.Task;
import service.PlannerService;
import util.DateUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private static final int NAME_WIDTH = 15;
    private static final String HEADER_PREFIX = "## ";
    private static final String ROW_INDENT = "   ";

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BG_DARK = "\u001B[48;5;234m";
    private static final String ANSI_FG_BRIGHT = "\u001B[97m";
    private static final String ANSI_DIM = "\u001B[2m";

    private static final String DOUBLE_LINE = "========================================";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private static final String IC_MENU = "▶ ";
    private static final String IC_FORM = "› ";
    private static final String IC_PLAN = "✦ ";
    private static final String IC_BOOKS = "📚 ";
    private static final String IC_SIM = "⏱ ";
    private static final String IC_OK = "✓ ";
    private static final String IC_WARN = "⚠ ";

    private final PlannerService plannerService;
    private final Scanner scanner;

    public ConsoleUI(PlannerService plannerService) {
        this.plannerService = plannerService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            int choice = showMenu();
            switch (choice) {
                case 1 -> handleAddSubject();
                case 2 -> displayPlan();
                case 3 -> displaySubjects();
                case 4 -> handleSimulation();
                case 5 -> {
                    System.out.print(ANSI_RESET);
                    System.out.println();
                    System.out.println("Exiting Smart Study Planner...");
                    running = false;
                }
                default -> {
                    outLine("");
                    outLine("Invalid choice. Please enter a number between 1 and 5.");
                    pauseForContinue();
                }
            }
        }
    }

    private int showMenu() {
        outLine("");
        outLine(DOUBLE_LINE);
        outLine("SMART STUDY PLANNER");
        outLine("===================");
        outLine("");
        outLine(IC_MENU + "1. Add Subject");
        outLine(IC_MENU + "2. View Today's Study Plan");
        outLine(IC_MENU + "3. View All Subjects");
        outLine(IC_MENU + "4. Simulate Missing Days");
        outLine(IC_MENU + "5. Exit");
        outLine("");
        outLine("---");
        outLine("");
        int choice = readInt("Enter your choice: ");
        outLine("");
        outLine("---");
        return choice;
    }

    private void handleAddSubject() {
        beginScreenFrame();
        outLine("--- ADD NEW SUBJECT ---");
        outLine("");
        outLine("╭─────────────────────────────────────────╮");
        outLine("│  " + IC_FORM + "New subject details                    │");
        outLine("╰─────────────────────────────────────────╯");
        outLine("");

        subsection(IC_FORM + "Subject name");
        String name = readNonEmptyString("Enter subject name: ");
        outLine("");

        subsection(IC_FORM + "Workload");
        double totalHours = readDouble("Enter total hours required: ", 0.0);
        double completedHours = readCompletedHours("Enter hours completed: ", totalHours);
        outLine("");

        subsection(IC_FORM + "Deadline");
        LocalDate deadline = readDate("Enter deadline (YYYY-MM-DD): ");
        outLine("");

        subsection(IC_FORM + "Difficulty (1–5)");
        int difficulty = readDifficulty("Enter difficulty (1-5): ");
        outLine("");

        Subject subject = new Subject(name, totalHours, completedHours, deadline, difficulty);
        plannerService.addSubject(subject);

        outLine(IC_OK + "Subject added successfully.");
        pauseForContinue();
    }

    private void displayPlan() {
        beginScreenFrame();
        outLine(DOUBLE_LINE);
        outLine(IC_PLAN + "TODAY'S STUDY PLAN");
        outLine("==================");
        outLine("");

        List<Task> tasks = plannerService.generateDailyPlan();
        if (tasks.isEmpty()) {
            outLine(IC_WARN + "No study plan available. Add subjects first.");
            pauseForContinue();
            return;
        }

        outLine("╭─────────────────────────────────────────╮");
        outLine("│  Schedule                               │");
        outLine("╰─────────────────────────────────────────╯");
        outLine("");
        outLine(HEADER_PREFIX + String.format("%-15s | %5s", "Subject Name", "Hours"));
        outLine("─────────────────────────────────────────");

        double totalStudyTime = 0.0;
        for (Task task : tasks) {
            outLine(ROW_INDENT + String.format("%-" + NAME_WIDTH + "s | %6.2f hrs",
                    task.getSubjectName(),
                    task.getHoursToStudyToday()));
            totalStudyTime += task.getHoursToStudyToday();
        }

        outLine("");
        outLine("---");
        outLine(String.format("Total Study Time: %.2f hrs", totalStudyTime));
        pauseForContinue();
    }

    private void displaySubjects() {
        beginScreenFrame();
        outLine(DOUBLE_LINE);
        outLine(IC_BOOKS + "ALL SUBJECTS");
        outLine("============");
        outLine("");

        List<Subject> subjects = plannerService.getSubjectsSortedByUrgency();
        if (subjects.isEmpty()) {
            outLine(IC_WARN + "No subjects available. Add subjects first.");
            pauseForContinue();
            return;
        }

        outLine("╭──────────────────────────────────────────────────────────────────────────╮");
        outLine("│  Subject overview                                                        │");
        outLine("╰──────────────────────────────────────────────────────────────────────────╯");
        outLine("");
        outLine(HEADER_PREFIX + String.format("%-" + NAME_WIDTH + "s | %-11s | %-12s | %-10s | %-10s",
                "Name", "Hours Left", "Deadline", "Difficulty", "Status"));
        outLine("──────────────────────────────────────────────────────────────────────────");

        LocalDate today = LocalDate.now();
        for (Subject subject : subjects) {
            long daysRemaining = DateUtil.getDaysBetween(today, subject.getDeadline());
            String status = daysRemaining <= 2 ? "HIGH RISK" : "NORMAL";

            outLine(ROW_INDENT + String.format("%-" + NAME_WIDTH + "s | %-11s | %-12s | %-10d | %-10s",
                    subject.getName(),
                    String.format("%.2f hrs", subject.getHoursLeft()),
                    subject.getDeadline().format(DATE_FORMATTER),
                    subject.getDifficulty(),
                    status));
        }

        outLine("");
        outLine("---");
        pauseForContinue();
    }

    private void handleSimulation() {
        beginScreenFrame();
        outLine(DOUBLE_LINE);
        outLine(IC_SIM + "SIMULATION RESULT");
        outLine("=================");
        outLine("");

        if (plannerService.getAllSubjects().isEmpty()) {
            outLine(IC_WARN + "No subjects available. Add subjects first.");
            pauseForContinue();
            return;
        }

        outLine("╭─────────────────────────────────────────╮");
        outLine("│  Missed days                            │");
        outLine("╰─────────────────────────────────────────╯");
        outLine("");
        int missedDays = readNonNegativeInt("Enter number of days to miss: ");
        List<PlannerService.SimulationResult> results = plannerService.simulateMissingDays(missedDays);
        if (results.isEmpty()) {
            outLine(IC_WARN + "No valid subjects available for simulation.");
            pauseForContinue();
            return;
        }

        outLine("");
        outLine("╭─────────────────────────────────────────╮");
        outLine("│  Outcome                                │");
        outLine("╰─────────────────────────────────────────╯");
        outLine("");
        outLine("\"" + "If you miss " + missedDays + " days:" + "\"");
        outLine("");

        for (PlannerService.SimulationResult result : results) {
            if (result.impossible()) {
                outLine(String.format("%-" + NAME_WIDTH + "s → IMPOSSIBLE (deadline missed)",
                        result.subjectName()));
            } else {
                outLine(String.format("%-" + NAME_WIDTH + "s → %6.2f hrs/day",
                        result.subjectName(),
                        result.hoursPerDay()));
            }
        }

        outLine("");
        outLine("---");
        pauseForContinue();
    }

    private void subsection(String title) {
        System.out.println(ANSI_BG_DARK + ANSI_DIM + ANSI_FG_BRIGHT + title + ANSI_RESET);
    }

    private void beginScreenFrame() {
        outLine("");
    }

    private void outLine(String text) {
        System.out.println(ANSI_BG_DARK + ANSI_FG_BRIGHT + text + ANSI_RESET);
    }

    private void printPrompt(String prompt) {
        System.out.print(ANSI_BG_DARK + ANSI_FG_BRIGHT + prompt + ANSI_RESET);
    }

    private String readNonEmptyString(String prompt) {
        while (true) {
            printPrompt(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            outLine(IC_WARN + "Input cannot be empty. Please try again.");
        }
    }

    private double readDouble(String prompt, double minimum) {
        while (true) {
            printPrompt(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value < minimum) {
                    outLine(IC_WARN + "Value must be at least " + String.format("%.2f", minimum) + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                outLine("Invalid number. Please try again.");
            }
        }
    }

    private double readCompletedHours(String prompt, double totalHours) {
        while (true) {
            double completedHours = readDouble(prompt, 0.0);
            if (completedHours > totalHours) {
                outLine(IC_WARN + "Completed hours cannot exceed total hours required.");
                continue;
            }
            return completedHours;
        }
    }

    private int readInt(String prompt) {
        while (true) {
            printPrompt(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                outLine("Invalid number. Please try again.");
            }
        }
    }

    private int readNonNegativeInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value < 0) {
                outLine(IC_WARN + "Number of days cannot be negative. Please try again.");
                continue;
            }
            return value;
        }
    }

    private int readDifficulty(String prompt) {
        while (true) {
            int difficulty = readInt(prompt);
            if (difficulty < 1 || difficulty > 5) {
                outLine("Difficulty must be between 1 and 5.");
                continue;
            }
            return difficulty;
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            printPrompt(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                outLine("Date format must be YYYY-MM-DD.");
            }
        }
    }

    private void pauseForContinue() {
        outLine("");
        printPrompt("Press Enter to continue...");
        scanner.nextLine();
    }
}
