package com.AgsCh.task_scheduler.dto.response;

import com.AgsCh.task_scheduler.model.Category;

import java.time.DayOfWeek;
import java.util.Set;

public class TaskResponseDTO {

    private Long id;
    private String name;
    private Set<Category> allowedCategories;
    private Set<DayOfWeek> assignedDays;

    public TaskResponseDTO(Long id, String name, Set<Category> allowedCategories, Set<DayOfWeek> assignedDays) {
        this.id = id;
        this.name = name;
        this.allowedCategories = allowedCategories;
        this.assignedDays = assignedDays;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Category> getAllowedCategories() {
        return allowedCategories;
    }

    public Set<DayOfWeek> getAssignedDays(){
        return assignedDays;
    }

}
