package com.AgsCh.task_scheduler.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class TaskAssignmentResponseDTO {

    private int week;
    private DayOfWeek day;
    private String taskName;
    private String personName;

    public TaskAssignmentResponseDTO(
            LocalDate date,
            String taskName,
            String personName) {

        this.day = date.getDayOfWeek();
        this.taskName = taskName;
        this.personName = personName;
        this.week = date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
    }

    public int getWeek() {
        return week;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getPersonName() {
        return personName;
    }
}
