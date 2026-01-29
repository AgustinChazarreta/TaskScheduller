package com.AgsCh.task_scheduler.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ScheduleRequestDTO {

    @Valid
    @NotNull
    private PlanningPeriodRequestDTO period;

    @Valid
    @NotEmpty
    private List<PersonRequestDTO> persons;

    @Valid
    @NotEmpty
    private List<TaskRequestDTO> tasks;

    // getters & setters

    public PlanningPeriodRequestDTO getPeriod() {
        return period;
    }

    public void setPeriod(PlanningPeriodRequestDTO period) {
        this.period = period;
    }

    public List<PersonRequestDTO> getPersons() {
        return persons;
    }

    public void setPersons(List<PersonRequestDTO> persons) {
        this.persons = persons;
    }

    public List<TaskRequestDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskRequestDTO> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ScheduleRequestDTO {\n");

        // Periodo
        sb.append("  period: ").append(period != null ? period.toString() : "null").append(",\n");

        // Persons
        sb.append("  persons: [\n");
        if (persons != null) {
            for (PersonRequestDTO p : persons) {
                sb.append("    ").append(p != null ? p.toString() : "null").append(",\n");
            }
        } else {
            sb.append("    null\n");
        }
        sb.append("  ],\n");

        // Tasks
        sb.append("  tasks: [\n");
        if (tasks != null) {
            for (TaskRequestDTO t : tasks) {
                sb.append("    ").append(t != null ? t.toString() : "null").append(",\n");
            }
        } else {
            sb.append("    null\n");
        }
        sb.append("  ]\n");

        sb.append("}");
        return sb.toString();
    }

}
