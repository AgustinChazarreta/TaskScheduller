package com.AgsCh.task_scheduler.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "persons")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Identificador único
    private Long id;

    @Column(nullable = false)
    private String name; // Nombre legible

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category; // Rol o categoría

    private LocalDate birthDate; // Fecha de nacimiento

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "person_available_days", joinColumns = @JoinColumn(name = "person_id"))
    @Column(name = "available_day")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> availableDays;

    // --------- Constructores ---------
    public Person() {
        this.availableDays = EnumSet.noneOf(DayOfWeek.class);
    }

    public Person(String name, Category category, LocalDate birthDate, Set<DayOfWeek> assignedDays) {
        this.name = name;
        this.category = category;
        this.birthDate = birthDate;
        this.availableDays = assignedDays != null ? EnumSet.copyOf(assignedDays) : EnumSet.noneOf(DayOfWeek.class);
    }

    // --------- Getters y setters ---------
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Set<DayOfWeek> getAvailableDays() {
        return availableDays;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setAvailableDays(Set<DayOfWeek> assignedDays) {
        this.availableDays = assignedDays != null ? EnumSet.copyOf(assignedDays) : EnumSet.noneOf(DayOfWeek.class);
    }

    @Override
    public String toString() {
        return name + " (" + category + ")";
    }
}