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

    // =========================
    // PERSISTENCE
    // =========================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID exclusivo para OptaPlanner
     * No representa identidad de dominio
     */
    @PlanningId
    @Column(nullable = false, updatable = false)
    private String planningId = UUID.randomUUID().toString();

    // =========================
    // DOMAIN
    // =========================

    /**
     * Función que debe cubrirse
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private Function function;

    /**
     * Persona asignada (decisión del solver)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    @PlanningVariable(valueRangeProviderRefs = "personRange")
    private Person person;

    /**
     * Día en el que ocurre la función
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Corrida del solver a la que pertenece
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_run_id", nullable = false)
    private ScheduleRun scheduleRun;

    // =========================
    // CONSTRUCTORS
    // =========================

    protected FunctionAssignment() {
        // requerido por JPA
    }

    /**
     * Constructor usado por el solver (en memoria)
     */
    public FunctionAssignment(Function function, LocalDate date) {
        this.function = function;
        this.date = date;
    }

    // =========================
    // GETTERS
    // =========================

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

    public ScheduleRun getScheduleRun() {
        return scheduleRun;
    }

    // =========================
    // SETTERS
    // =========================

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setScheduleRun(ScheduleRun scheduleRun) {
        this.scheduleRun = scheduleRun;
    }

    // =========================
    // DOMAIN HELPERS
    // =========================

    public boolean isAssigned() {
        return person != null;
    }

    @Override
    public String toString() {
        return function.getName() + " - " + date +
                (person != null ? " (" + person.getFullName() + ")" : " (UNASSIGNED)");
    }
}
