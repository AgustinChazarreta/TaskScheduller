package com.AgsCh.task_scheduler.model;

import java.time.*;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import org.optaplanner.core.api.domain.lookup.PlanningId;

import jakarta.persistence.*;

@Entity
public class Person {

    @PlanningId @Id
    private String id = UUID.randomUUID().toString(); // Identificador único
    private String name;                 // Nombre legible
    private Category category;             // Rol o categoría
    private LocalDate birthDate;            // Fecha de nacimiento
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> availableDays; // Días que puede trabajar

    public Person() {}

    public Person(String name, Category category, LocalDate birthDate, Set<DayOfWeek> assignedDays) {
        this.name = name;
        this.category = category;
        this.birthDate = birthDate;
        this.availableDays = EnumSet.copyOf(assignedDays);
    }

    @Override
    public String toString() {
        return name;
    }

    // Getters y setters
    public String getId() { return id; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public LocalDate getBirthDate() { return birthDate; }
    public Set<DayOfWeek> getAvailableDays() { return availableDays; }

    public void setName(String name) { this.name = name; }
    public void setCategory(Category category) { this.category = category; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public void setAvailableDays(Set<DayOfWeek> assignedDays) { this.availableDays = assignedDays; }
}
