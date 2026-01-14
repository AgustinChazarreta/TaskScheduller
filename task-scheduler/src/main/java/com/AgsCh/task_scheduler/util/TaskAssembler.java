package com.AgsCh.task_scheduler.util;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.AgsCh.task_scheduler.model.Category;
import com.AgsCh.task_scheduler.model.Task;

public class TaskAssembler {

    public static List<Task> buildTasks(
            Map<String, List<DayOfWeek>> parsed,
            Map<String, Set<Category>> userCategories) {

        List<Task> tasks = new ArrayList<>();

        for (Map.Entry<String, List<DayOfWeek>> entry : parsed.entrySet()) {

            String name = entry.getKey();
            List<DayOfWeek> days = entry.getValue();

            Set<Category> categories = userCategories.get(name);
            if (categories == null || categories.isEmpty()) {
                throw new IllegalStateException(
                        "La tarea '" + name + "' no tiene categorías asignadas");
            }

            Set<DayOfWeek> assignedDays = EnumSet.copyOf(days);

            Task task = new Task(
                    name,
                    categories,
                    assignedDays
            );

            tasks.add(task);
        }

        return tasks;
    }
}
