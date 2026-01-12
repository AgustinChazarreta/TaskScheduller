package com.AgsCh.task_scheduler.model;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.time.*;

import org.optaplanner.core.api.domain.lookup.PlanningId;

import jakarta.persistence.*;

@Entity
public class Task {

    @PlanningId @Id
    private String id = UUID.randomUUID().toString(); // Identificador único
    
    private String name;
    private Set<Category> allowedCategories;
    private Set<DayOfWeek> assignedDays;

    public Task() {}
    
    public Task(String name, Set<Category> allowedCategories, Set<DayOfWeek> assignedDays) {
        this.name = name;
        this.allowedCategories = EnumSet.copyOf(allowedCategories);
        this.assignedDays = EnumSet.copyOf(assignedDays);
    }

    @Override
    public String toString() {
        return name + "(" + id + ")";
    }

    // Getters y setters
    public String getId() { return id; }
    public String getName() { return name; }
    public Set<Category> getAllowedCategories() { return allowedCategories; }
    public Set<DayOfWeek> getAssignedDays() { return assignedDays; }

    public void setName(String name) { this.name = name; }
    public void setAllowedCategories(Set<Category> allowedCategories) { this.allowedCategories = allowedCategories; }
    public void setAssignedDays(Set<DayOfWeek> assignedDays) { this.assignedDays = assignedDays; }
}
