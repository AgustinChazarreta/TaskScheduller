package com.AgsCh.task_scheduler.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedule_runs")
public class ScheduleRun {

    // =========================
    // PERSISTENCE
    // =========================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * Score final del solver (ej: "0hard/-3soft")
     * Se guarda como String para desacoplar de OptaPlanner
     */
    @Column(nullable = false)
    private String score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    /**
     * Resultados concretos de esta corrida
     */
    @OneToMany(
        mappedBy = "scheduleRun",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<FunctionAssignment> assignments = new ArrayList<>();

    // =========================
    // CONSTRUCTORS
    // =========================

    protected ScheduleRun() {
        // requerido por JPA
    }

    public ScheduleRun(LocalDate startDate, LocalDate endDate, String score) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.score = score;
        this.status = Status.ARCHIVED;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // =========================
    // DOMAIN LOGIC
    // =========================

    public void activate() {
        this.status = Status.ACTIVE;
    }

    public void archive() {
        this.status = Status.ARCHIVED;
    }

    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }

    public void addAssignment(FunctionAssignment assignment) {
        assignment.setScheduleRun(this);
        this.assignments.add(assignment);
    }

    // =========================
    // GETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getScore() {
        return score;
    }

    public Status getStatus() {
        return status;
    }

    public List<FunctionAssignment> getAssignments() {
        return assignments;
    }

    // =========================
    // ENUM
    // =========================

    public enum Status {
        ACTIVE,
        ARCHIVED
    }
}
