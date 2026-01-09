package com.AgsCh.task_scheduler.dto.request;

import java.time.DayOfWeek;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class TaskRequestDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String category;

    @NotNull
    @NotEmpty
    private List<DayOfWeek> assignedDays;

    // getters & setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<DayOfWeek> getAssignedDays() {
        return assignedDays;
    }

    public void setAssignedDays(List<DayOfWeek> assignedDays) {
        this.assignedDays = assignedDays;
    }
}