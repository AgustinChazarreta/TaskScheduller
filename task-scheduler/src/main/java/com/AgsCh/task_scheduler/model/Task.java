package com.AgsCh.task_scheduler.model;

import java.util.List;
import java.util.UUID;
import java.time.*;

import org.optaplanner.core.api.domain.lookup.PlanningId;

import jakarta.persistence.*;

@Entity
public class Task {

    @PlanningId @Id
    private String id = UUID.randomUUID().toString(); // Identificador único
    
    private String name;
    private String category;
    private List<DayOfWeek> assignedDays;

    public Task() {}
    
    public Task(String name, String category, List<DayOfWeek> assignedDays) {
        this.name = name;
        this.category = category;
        this.assignedDays = assignedDays;
    }

    @Override
    public String toString() {
        return name + "(" + id + ")";
    }

    // Getters y setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public List<DayOfWeek> getAssignedDays() { return assignedDays; }

    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setAssignedDays(List<DayOfWeek> assignedDays) { this.assignedDays = assignedDays; }
}
