package com.AgsCh.task_scheduler.model;

import java.time.LocalDate;
import java.util.UUID;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class TaskAssignment {

    // ID SOLO para OptaPlanner (no DB)
    @PlanningId
    private String planningId = UUID.randomUUID().toString();

    // Datos de dominio (ya persistidos)
    private Function task;
    private LocalDate date;

    @PlanningVariable(valueRangeProviderRefs = { "personRange" })
    private Person person;

    public TaskAssignment() {}

    public TaskAssignment(Function task, LocalDate date) {
        this.task = task;
        this.date = date;
    }

    // getters & setters

    public String getPlanningId() {
        return planningId;
    }

    public Function getTask() {
        return task;
    }

    public LocalDate getDate() {
        return date;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}