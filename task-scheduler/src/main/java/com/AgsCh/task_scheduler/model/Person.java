package com.AgsCh.task_scheduler.model;

import java.time.*;
import java.util.List;
import java.util.UUID;

import org.optaplanner.core.api.domain.lookup.PlanningId;

import jakarta.persistence.*;

@Entity
public class Person {

    @PlanningId @Id
    private String id = UUID.randomUUID().toString(); // Identificador único
    private String name;                 // Nombre legible
    private String category;             // Rol o categoría
    private LocalDate birthDate;            // Fecha de nacimiento
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> availableDays; // Días que puede trabajar

    public Person() {}

    public Person(String name, String category, LocalDate birthDate, List<DayOfWeek> availableDays) {
        this.name = name;
        this.category = category;
        this.birthDate = birthDate;
        this.availableDays = availableDays;
    }

    @Override
    public String toString() {
        return name;
    }

    // Getters y setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public LocalDate getBirthDate() { return birthDate; }
    public List<DayOfWeek> getAvailableDays() { return availableDays; }

    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public void setAvailableDays(List<DayOfWeek> availableDays) { this.availableDays = availableDays; }
}
