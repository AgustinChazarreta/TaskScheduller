package com.AgsCh.task_scheduler.dto.request;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class PersonRequestDTO {
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String category;

    @NotBlank   
    private LocalDate birthDate;

    @NotNull
    @NotEmpty
    private List<DayOfWeek> availableDays;
    
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<DayOfWeek> getAvailableDays() {
        return availableDays;
    }
    
    public void setAvailableDays(List<DayOfWeek> availableDays) {
        this.availableDays = availableDays;
    }
}