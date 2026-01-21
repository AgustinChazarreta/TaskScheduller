package com.AgsCh.task_scheduler.util.normalizer;

import java.time.DayOfWeek;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Convierte los días en formato String a DayOfWeek.
 * Lanza excepción si encuentra un día no reconocido.
 */
public class TaskRefactor {

    private TaskRefactor() {
        // utility class
    }

    public static Map<String, Set<DayOfWeek>> refactorDays(Map<String, List<String>> tasks) {
        Map<String, Set<DayOfWeek>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : tasks.entrySet()) {
            Set<DayOfWeek> days = new HashSet<>();
            for (String day : entry.getValue()) {
                DayOfWeek converted = UniversalDayTranslator.toDayOfWeek(day);
                if (converted == null) {
                    throw new IllegalArgumentException("Día inválido o no reconocido: " + day);
                }
                days.add(converted);
            }
            result.put(entry.getKey(), days);
        }
        return result;
    }
}
