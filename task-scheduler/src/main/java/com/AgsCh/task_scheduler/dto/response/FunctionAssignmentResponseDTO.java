package com.AgsCh.task_scheduler.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

import com.AgsCh.task_scheduler.model.FunctionAssignment;

public class FunctionAssignmentResponseDTO {

    private LocalDate date; // 🔹 nuevo campo
    private int week;
    private DayOfWeek day;
    private String functionName;
    private String personName;
    private String personNickname;

    public FunctionAssignmentResponseDTO(
            LocalDate date,
            String functionName,
            String personName,
            String personNickname) {

        this.date = date; // 🔹 guardar fecha completa
        this.day = date.getDayOfWeek();
        this.functionName = functionName;
        this.personName = personName;
        this.personNickname = personNickname;
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

    public String getPersonName() {
        return personName;
    }

    public String getPersonNickname() {
        return personNickname;
    }

    // ================= CONVERTIR DESDE ENTITY =================
    public static FunctionAssignmentResponseDTO fromEntity(FunctionAssignment entity) {

        String personName = entity.getPerson() != null ? entity.getPerson().getFullName() : "UNASSIGNED";
        String personNickname = entity.getPerson() != null ? entity.getPerson().getNickName() : "";

        return new FunctionAssignmentResponseDTO(
                entity.getDate(),
                entity.getFunction().getName(),
                personName,
                personNickname);
    }
}