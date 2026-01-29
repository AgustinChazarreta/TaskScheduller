package com.AgsCh.task_scheduler.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import com.AgsCh.task_scheduler.model.Category;

public class PersonResponseDTO {

    private Long id;
    private String name;
    private Category category;
    private LocalDate birthDate;
    private Set<DayOfWeek> availableDays;

    public PersonResponseDTO(Long id, String name, Category category, LocalDate birthDate, Set<DayOfWeek> availableDays) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.birthDate = birthDate;
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

    public LocalDate getBirthDate(){
        return birthDate;
    }

    public Set<DayOfWeek> getAvailableDays() {
        return availableDays;
    }
}
