package com.AgsCh.task_scheduler.dto.response;

import java.time.DayOfWeek;

public class TaskAssignmentResponseDTO {

    private DayOfWeek day;
    private String taskName;
    private String personName;

    public TaskAssignmentResponseDTO(
            DayOfWeek day,
            String taskName,
            String personName
    ) {
        this.day = day;
        this.taskName = taskName;
        this.personName = personName;
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
