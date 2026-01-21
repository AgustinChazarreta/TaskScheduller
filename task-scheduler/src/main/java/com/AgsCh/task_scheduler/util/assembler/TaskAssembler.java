package com.AgsCh.task_scheduler.util.assembler;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.AgsCh.task_scheduler.model.Category;
import com.AgsCh.task_scheduler.model.Task;

/**
 * Ensambla objetos Task a partir de los días normalizados y categorías de usuario.
 */
public class TaskAssembler {

    private TaskAssembler() {
        // utility class
    }

    public static List<Task> buildTasks(Map<String, Set<DayOfWeek>> parsed,
                                        Map<String, Set<Category>> userCategories) {
        List<Task> tasks = new ArrayList<>();

        for (Map.Entry<String, Set<DayOfWeek>> entry : parsed.entrySet()) {
            String name = entry.getKey();
            Set<DayOfWeek> days = entry.getValue();

            Set<Category> categories = userCategories.get(name);
            if (categories == null || categories.isEmpty()) {
                throw new IllegalStateException("La tarea '" + name + "' no tiene categorías asignadas");
            }

            EnumSet<DayOfWeek> assignedDays = EnumSet.noneOf(DayOfWeek.class);
            assignedDays.addAll(days);

            Task task = new Task(name, categories, assignedDays);
            tasks.add(task);
        }

        return tasks;
    }
}
