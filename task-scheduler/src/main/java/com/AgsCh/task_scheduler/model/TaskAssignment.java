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

    private Task task;

    /** Fecha concreta: FIXA */
    private LocalDate date;

    /** Variable que decide OptaPlanner */
    @PlanningVariable(valueRangeProviderRefs = {"personRange"})
    private Person person;

    public TaskAssignment() {
    }

    public TaskAssignment(Task task, LocalDate date) {
        this.task = task;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public LocalDate getDate() {
        return date;
    }

    public Person getPerson() {
        return person;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public void setPerson(Person person) {
        this.person = person;
    }
}
