package com.AgsCh.task_scheduler.util;

import org.apache.poi.xwpf.usermodel.*;

import com.AgsCh.task_scheduler.model.Category;
import com.AgsCh.task_scheduler.model.Task;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.*;

public class WordParser {

    public static Map<String, List<String>> parseTasks(String filePath) {

        Map<String, List<String>> tasks = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(filePath); XWPFDocument document = new XWPFDocument(fis)) {
            if (document.getTables().isEmpty()) {
                return tasks;
            }

            XWPFTable table = document.getTables().get(0);
            List<XWPFTableRow> rows = table.getRows();

            if (rows.size() < 2) {
                return tasks;
            }

            // encabezados (días de la semana)
            List<String> days = new ArrayList<>();
            List<XWPFTableCell> headerCells = rows.get(0).getTableCells();

            for (int i = 1; i < headerCells.size(); i++) {
                String day = headerCells.get(i).getText().trim();
                days.add(day);
            }

            // filas de tareas
            for (int r = 1; r < rows.size(); r++) {
                List<XWPFTableCell> cells = rows.get(r).getTableCells();

                String taskName = cells.get(0).getText().trim();
                if (taskName.isEmpty())
                    continue;

                List<String> assignedDays = new ArrayList<>();
                for (int c = 1; c < cells.size() && c <= days.size(); c++) {
                    if (!cells.get(c).getText().trim().isEmpty()) {
                        assignedDays.add(days.get(c - 1));
                    }
                }

                // Se agregan las tareas al Map con su lista de días asignados
                tasks.put(taskName, assignedDays);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error leyendo el archivo Word", e);
        }

        return tasks;
    }

    public static void main(String[] args) {

        String path = "D://Agustín//Escritorio/Lorem.docx";

        // 1. Parseo del Word
        Map<String, List<String>> parsed = WordParser.parseTasks(path);

        // 2. Normalización de días (String -> DayOfWeek)
        Map<String, List<DayOfWeek>> normalized = TaskRefactor.refactorDays(parsed);

        System.out.println("=== TAREAS NORMALIZADAS ===");
        normalized.forEach((task, days) -> System.out.println(task + " -> " + days));

        // 3. Simulación de input del usuario (categorías)
        Map<String, Set<Category>> userCategories = UserInputSimulator.enrich(normalized);

        // 4. Ensamblado de Tasks finales
        List<Task> tasks = TaskAssembler.buildTasks(normalized, userCategories);

        System.out.println("\n=== TASKS FINALES ===");

        tasks.forEach(task -> {
            System.out.println("ID: " + task.getId());
            System.out.println("Nombre: " + task.getName());
            System.out.println("Días asignados: " + task.getAssignedDays());
            System.out.println("Categorías permitidas: " + task.getAllowedCategories());
            System.out.println("----------------------------------");
        });

    }

}
