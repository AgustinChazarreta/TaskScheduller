package com.AgsCh.task_scheduler.util.normalizer;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.*;

public class UniversalDayTranslator {

    // idiomas que soportás (podés agregar más)
    private static final List<Locale> SUPPORTED_LOCALES = List.of(
            new Locale("es"),   // español
            Locale.ENGLISH,
            Locale.FRENCH,
            Locale.GERMAN,
            Locale.ITALIAN,
            new Locale("pt")    // portugués
    );

    public static DayOfWeek toDayOfWeek(String input) {

        String normalized = normalize(input);

        for (Locale locale : SUPPORTED_LOCALES) {
            for (DayOfWeek day : DayOfWeek.values()) {

                String full = day.getDisplayName(TextStyle.FULL, locale);
                String shortName = day.getDisplayName(TextStyle.SHORT, locale);

                if (normalized.equals(normalize(full))
                        || normalized.equals(normalize(shortName))) {
                    return day;
                }
            }
        }

        throw new IllegalArgumentException(
                "No se pudo interpretar el día: " + input);
    }

    private static String normalize(String value) {
        return value
                .toLowerCase()
                .replaceAll("\\p{M}", "")
                .trim();
    }
}
