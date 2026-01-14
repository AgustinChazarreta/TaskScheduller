package com.AgsCh.task_scheduler.util;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.AgsCh.task_scheduler.model.Category;

public class UserInputSimulator {

    public static Map<String, Set<Category>> enrich(
            Map<String, List<DayOfWeek>> normalizedTasks) {

        Map<String, Set<Category>> result = new HashMap<>();

        for (String taskName : normalizedTasks.keySet()) {

            // simulación simple
            Set<Category> categories = EnumSet.of(Category.CATEGORY_1);

            result.put(taskName, categories);
        }

        return result;
    }
}
