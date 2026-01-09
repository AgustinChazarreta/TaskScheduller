package com.AgsCh.task_scheduler.dto.request;

import java.time.DayOfWeek;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TaskAssignmentRequestDTO {

    @NotBlank(message = "taskName is required")
    private String taskName;

    @NotBlank(message = "personName is required")
    private String personName;

    @NotNull(message = "day is required")
    private DayOfWeek day;

    // getters & setters
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }
}
