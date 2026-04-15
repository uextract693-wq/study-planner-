package com.example.smartstudyplanner.util;

import android.content.Context;

import com.example.smartstudyplanner.model.Subject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class FileUtil {
    private static final String FILE_NAME = "subjects.txt";

    private FileUtil() {
    }

    public static void saveSubjects(Context context, List<Subject> subjects) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Subject subject : subjects) {
                writer.write(subject.getName() + ","
                        + subject.getTotalHoursNeeded() + ","
                        + subject.getHoursCompleted() + ","
                        + subject.getDeadline() + ","
                        + subject.getDifficulty());
                writer.newLine();
            }
        } catch (IOException ignored) {
        }
    }

    public static List<Subject> loadSubjects(Context context) {
        List<Subject> subjects = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            return subjects;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != 5) {
                    continue;
                }

                try {
                    String name = parts[0].trim();
                    double total = Double.parseDouble(parts[1].trim());
                    double completed = Double.parseDouble(parts[2].trim());
                    LocalDate deadline = LocalDate.parse(parts[3].trim());
                    int difficulty = Integer.parseInt(parts[4].trim());

                    if (name.isEmpty() || total < 0 || completed < 0 || completed > total || difficulty < 1 || difficulty > 5) {
                        continue;
                    }

                    subjects.add(new Subject(name, total, completed, deadline, difficulty));
                } catch (NumberFormatException | DateTimeParseException ignored) {
                }
            }
        } catch (IOException ignored) {
        }

        return subjects;
    }
}
