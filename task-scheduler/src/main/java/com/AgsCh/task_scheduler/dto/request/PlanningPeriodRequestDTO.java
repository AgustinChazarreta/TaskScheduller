package com.AgsCh.task_scheduler.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public class PlanningPeriodRequestDTO {

    @NotNull
    @JsonProperty("startDate")
    private LocalDate startDate;

    @NotNull
    @JsonProperty("endDate")
    private LocalDate endDate;

    public PlanningPeriodRequestDTO() {}

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}