package com.AgsCh.task_scheduler.model;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador único

    @Column(nullable = false)
    private String name; // Nombre de la tarea

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_allowed_categories", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private Set<Category> allowedCategories; // Categorías permitidas

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_assigned_days", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "assigned_day")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> assignedDays; // Días asignados

    // --------- Constructores ---------
    public Task() {
        this.allowedCategories = EnumSet.noneOf(Category.class);
        this.assignedDays = EnumSet.noneOf(DayOfWeek.class);
    }

    public Task(String name, Set<Category> allowedCategories, Set<DayOfWeek> assignedDays) {
        this.name = name;
        this.allowedCategories = allowedCategories != null ? EnumSet.copyOf(allowedCategories) : EnumSet.noneOf(Category.class);
        this.assignedDays = assignedDays != null ? EnumSet.copyOf(assignedDays) : EnumSet.noneOf(DayOfWeek.class);
    }

    // --------- Getters y setters ---------
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Category> getAllowedCategories() {
        return allowedCategories;
    }

    public Set<DayOfWeek> getAssignedDays() {
        return assignedDays;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAllowedCategories(Set<Category> allowedCategories) {
        this.allowedCategories = allowedCategories != null ? EnumSet.copyOf(allowedCategories)
                : EnumSet.noneOf(Category.class);
    }

    public void setAssignedDays(Set<DayOfWeek> assignedDays) {
        this.assignedDays = assignedDays != null ? EnumSet.copyOf(assignedDays) : EnumSet.noneOf(DayOfWeek.class);
    }

    @Override
    public String toString() {
        return name + " -> " + assignedDays + " | " + allowedCategories;
    }
}
