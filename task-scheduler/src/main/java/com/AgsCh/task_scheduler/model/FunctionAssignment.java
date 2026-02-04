package com.AgsCh.task_scheduler.model;

import java.time.LocalDate;
import java.util.UUID;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import jakarta.persistence.*;

@Entity
@Table(name = "function_assignments")
@PlanningEntity
public class FunctionAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID SOLO para OptaPlanner (no DB)
    @PlanningId
    @Column(nullable = false, updatable = false)
    private String planningId = UUID.randomUUID().toString();

    // ---- Qué función se está cubriendo ----
    @ManyToOne(optional = false)
    @JoinColumn(name = "function_id")
    private Function function;

    // ---- Quién la realiza (OptaPlanner decide) ----
    @ManyToOne
    @JoinColumn(name = "person_id")
    @PlanningVariable(valueRangeProviderRefs = "personRange")
    private Person person;

    // ---- Cuándo ocurre ----
    @Column(nullable = false)
    private LocalDate date;

    // ---- A qué planificación pertenece ----
    @ManyToOne(optional = false)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    // ---- Constructores ----

    public FunctionAssignment() {
    }

    public FunctionAssignment(Function function, LocalDate date) {
        this.function = function;
        this.date = date;
    }

    // ---- Getters ----

    public Long getId() {
        return id;
    }

    public Function getFunction() {
        return function;
    }

    public Person getPerson() {
        return person;
    }

    public LocalDate getDate() {
        return date;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    // ---- Setters ----

    public void setFunction(Function function) {
        this.function = function;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public boolean isAssigned() {
        return person != null;
    }

    @Override
    public String toString() {
        return function.getName() + " - " + date;
    }
}