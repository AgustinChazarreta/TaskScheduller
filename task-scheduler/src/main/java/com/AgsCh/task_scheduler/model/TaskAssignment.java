package com.AgsCh.task_scheduler.model;

import java.time.LocalDate;
import java.util.UUID;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class TaskAssignment {

    @PlanningId
    private String id = UUID.randomUUID().toString();

    private Task task;           // Tarea fija
    private LocalDate date;      // Fecha fija

    @PlanningVariable(valueRangeProviderRefs = {"personRange"})
    private Person person;       // Variable que asigna OptaPlanner

    // --------- Constructores ---------
    public TaskAssignment() {}

    public TaskAssignment(Task task, LocalDate date) {
        this.task = task;
        this.date = date;
    }

    // --------- Getters y setters ---------
    public String getId() { return id; }
    public Task getTask() { return task; }
    public LocalDate getDate() { return date; }
    public Person getPerson() { return person; }

    public void setDate(LocalDate date) { this.date = date; }
    public void setPerson(Person person) { this.person = person; }

    @Override
    public String toString() {
        return "TaskAssignment{" +
                "task=" + (task != null ? task.getName() : "null") +
                ", date=" + date +
                ", person=" + (person != null ? person.getName() : "UNASSIGNED") +
                '}';
    }
}
