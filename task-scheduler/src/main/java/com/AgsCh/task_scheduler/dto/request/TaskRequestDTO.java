package com.AgsCh.task_scheduler.dto.request;

import java.time.DayOfWeek;
import java.util.Set;
import com.AgsCh.task_scheduler.model.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class TaskRequestDTO {

    @NotBlank
    private String name;

    @NotNull
    @NotEmpty
    private Set<Category> allowedCategories;

    @NotNull
    @NotEmpty
    private Set<DayOfWeek> assignedDays;

    // getters & setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Category> getAllowedCategories() {
        return allowedCategories;
    }

    public void setAllowedCategories(Set<Category> allowedCategories) {
        this.allowedCategories = allowedCategories;
    }

    public Set<DayOfWeek> getAssignedDays() {
        return assignedDays;
    }

    public void setAssignedDays(Set<DayOfWeek> assignedDays) {
        this.assignedDays = assignedDays;
    }
}