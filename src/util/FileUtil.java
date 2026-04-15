package util;

import model.Subject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class FileUtil {
    private static final String FILE_NAME = "subjects.txt";

    private FileUtil() {
    }

    public static void saveSubjects(List<Subject> subjects) {
        Path filePath = Path.of(FILE_NAME);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (Subject subject : subjects) {
                writer.write(String.format("%s,%s,%s,%s,%s",
                        subject.getName(),
                        subject.getTotalHoursNeeded(),
                        subject.getHoursCompleted(),
                        subject.getDeadline(),
                        subject.getDifficulty()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Could not save subjects: " + e.getMessage());
        }
    }

    public static List<Subject> loadSubjects() {
        List<Subject> subjects = new ArrayList<>();
        Path filePath = Path.of(FILE_NAME);

        if (!Files.exists(filePath)) {
            return subjects;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
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
                    double totalHours = Double.parseDouble(parts[1].trim());
                    double completedHours = Double.parseDouble(parts[2].trim());
                    LocalDate deadline = LocalDate.parse(parts[3].trim());
                    int difficulty = Integer.parseInt(parts[4].trim());

                    if (name.isEmpty() || totalHours < 0 || completedHours < 0 || completedHours > totalHours
                            || difficulty < 1 || difficulty > 5) {
                        continue;
                    }

                    subjects.add(new Subject(name, totalHours, completedHours, deadline, difficulty));
                } catch (NumberFormatException | DateTimeParseException ex) {
                    // Skip bad rows and continue loading remaining data.
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load subjects: " + e.getMessage());
        }

        return subjects;
    }
}
