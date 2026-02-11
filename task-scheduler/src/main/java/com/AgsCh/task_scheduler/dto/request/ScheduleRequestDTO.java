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
    private List<Long> personIds;

    @Valid
    @NotEmpty
    private List<Long> functionIds;

    // getters & setters

    public PlanningPeriodRequestDTO getPeriod() {
        return period;
    }

    public void setPeriod(PlanningPeriodRequestDTO period) {
        this.period = period;
    }

    public List<Long> getPersonIds() {
        return personIds;
    }

    public void setPersonIds(List<Long> personIds) {
        this.personIds = personIds;
    }

    public List<Long> getFunctionIds() {
        return functionIds;
    }

    public void setFunctionIds(List<Long> functionIds) {
        this.functionIds = functionIds;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ScheduleRequestDTO {\n");

        // Periodo
        sb.append("  period: ").append(period != null ? period.toString() : "null").append(",\n");

        // Persons
        sb.append("  persons: [\n");
        if (personIds != null) {
            for (Long id : personIds) {
                sb.append("    ").append(id != null ? id.toString() : "null").append(",\n");
            }
        } else {
            sb.append("    null\n");
        }
        sb.append("  ],\n");

        // Functions
        sb.append("  functions: [\n");
        if (functionIds != null) {
            for (Long id : functionIds) {
                sb.append("    ").append(id != null ? id.toString() : "null").append(",\n");
            }
        } else {
            sb.append("    null\n");
        }
        sb.append("  ]\n");

        sb.append("}");
        return sb.toString();
    }

}
