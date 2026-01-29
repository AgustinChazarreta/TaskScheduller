package com.AgsCh.task_scheduler.dto.response;

import java.time.DayOfWeek;
import java.util.Set;

import com.AgsCh.task_scheduler.model.Category;

public class PersonResponseDTO {

    private Long id;
    private String name;
    private Category category;
    private Set<DayOfWeek> availableDays;

    public PersonResponseDTO(Long id, String name, Category category, Set<DayOfWeek> availableDays) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.availableDays = availableDays;
    }

    public Long getId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public Set<DayOfWeek> getAvailableDays() {
        return availableDays;
    }
}
