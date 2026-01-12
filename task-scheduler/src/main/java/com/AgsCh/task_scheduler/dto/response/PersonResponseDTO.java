package com.AgsCh.task_scheduler.dto.response;

import java.time.DayOfWeek;
import java.util.Set;

import com.AgsCh.task_scheduler.model.Category;

public class PersonResponseDTO {

    private String name;
    private Category category;
    private Set<DayOfWeek> assignedDays;

    public PersonResponseDTO(String name, Category category, Set<DayOfWeek> assignedDays) {
        this.name = name;
        this.category = category;
        this.assignedDays = assignedDays;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public Set<DayOfWeek> getAssignedDays() {
        return assignedDays;
    }
}
