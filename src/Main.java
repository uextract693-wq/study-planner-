import model.Subject;
import service.PlannerService;
import ui.ConsoleUI;
import util.FileUtil;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Subject> loadedSubjects = FileUtil.loadSubjects();
        PlannerService plannerService = new PlannerService(loadedSubjects);
        ConsoleUI consoleUI = new ConsoleUI(plannerService);

        try {
            consoleUI.start();
        } finally {
            FileUtil.saveSubjects(plannerService.getAllSubjects());
            System.out.println("Subjects saved successfully.");
        }
    }
}
