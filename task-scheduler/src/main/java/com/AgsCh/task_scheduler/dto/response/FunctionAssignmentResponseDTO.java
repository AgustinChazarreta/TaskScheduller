package com.AgsCh.task_scheduler.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

public class FunctionAssignmentResponseDTO {

    private LocalDate date; // 🔹 nuevo campo
    private int week;
    private DayOfWeek day;
    private String functionName;
    private List<String> personNames;
    private List<String> personNicknames;

    public FunctionAssignmentResponseDTO(
            LocalDate date,
            String functionName,
            List<String> personNames,
            List<String> personNicknames) {

        this.date = date;
        this.day = date.getDayOfWeek();
        this.functionName = functionName;
        this.personNames = personNames;
        this.personNicknames = personNicknames;
        this.week = date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
    }

    // ================= GETTERS =================
    public LocalDate getDate() {
        return date;
    }

    public int getWeek() {
        return week;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<String> getPersonNames() {
        return personNames;
    }

    public List<String> getPersonNicknames() {
        return personNicknames;
    }
}