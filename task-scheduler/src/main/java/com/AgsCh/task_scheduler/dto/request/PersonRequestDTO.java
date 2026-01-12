package com.AgsCh.task_scheduler.dto.request;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import com.AgsCh.task_scheduler.model.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class PersonRequestDTO {
    
    @NotBlank
    private String name;
    
    @NotBlank
    private Category category;

    @NotBlank   
    private LocalDate birthDate;

    @NotNull
    @NotEmpty
    private Set<DayOfWeek> assignedDays;
    
    public PersonRequestDTO() {
    }
    
    // getters & setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<DayOfWeek> getAvailableDays() {
        return assignedDays;
    }

    public void setAvailableDays(Set<DayOfWeek> assignedDays) {
        this.assignedDays = assignedDays;
    }
}