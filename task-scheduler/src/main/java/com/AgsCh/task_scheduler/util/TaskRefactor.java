package com.AgsCh.task_scheduler.util;

import java.time.DayOfWeek;
import java.util.*;

public class TaskRefactor {

    private static final Map<String, DayOfWeek> DAY_MAPPING = new HashMap<>();

    static {
        DAY_MAPPING.put("lunes", DayOfWeek.MONDAY);
        DAY_MAPPING.put("martes", DayOfWeek.TUESDAY);
        DAY_MAPPING.put("miercoles", DayOfWeek.WEDNESDAY);
        DAY_MAPPING.put("miércoles", DayOfWeek.WEDNESDAY);
        DAY_MAPPING.put("jueves", DayOfWeek.THURSDAY);
        DAY_MAPPING.put("viernes", DayOfWeek.FRIDAY);
        DAY_MAPPING.put("sabado", DayOfWeek.SATURDAY);
        DAY_MAPPING.put("sábado", DayOfWeek.SATURDAY);
        DAY_MAPPING.put("domingo", DayOfWeek.SUNDAY);
    }

    public static Map<String, List<DayOfWeek>> refactorDays(
            Map<String, List<String>> tasks) {

        Map<String, List<DayOfWeek>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : tasks.entrySet()) {

            List<DayOfWeek> days = new ArrayList<>();

            for (String day : entry.getValue()) {
                DayOfWeek converted = UniversalDayTranslator.toDayOfWeek(day);
                if (converted == null) {
                    throw new IllegalArgumentException(
                            "Día inválido o no reconocido: " + day);
                }
                days.add(converted);
            }

            result.put(entry.getKey(), days);
        }

        return result;
    }
}
