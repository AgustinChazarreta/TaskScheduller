package com.AgsCh.task_scheduler.dto.request.requestAdapter;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public class PlanningPeriodRequestDTO {

    @NotNull
    private LocalDate startDate;

    @NotNull
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
