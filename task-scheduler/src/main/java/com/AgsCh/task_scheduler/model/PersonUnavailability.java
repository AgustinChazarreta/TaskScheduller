package com.AgsCh.task_scheduler.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "person_unavailabilities")
public class PersonUnavailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    /**
     * Fecha desde la cual NO puede trabajar (inclusive)
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * Fecha hasta la cual NO puede trabajar (inclusive)
     * Puede ser null para indisponibilidad de un solo día
     */
    private LocalDate endDate;

    private String reason;

    // -------- Constructores --------

    protected PersonUnavailability() {
        // JPA
    }

    public PersonUnavailability(LocalDate startDate, LocalDate endDate, String reason) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    public PersonUnavailability(LocalDate date, String reason) {
        this.startDate = date;
        this.endDate = date;
        this.reason = reason;
    }

    // -------- Getters --------

    public Long getId() {
        return id;
    }

    public Person getPerson() {
        return person;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getReason() {
        return reason;
    }

    // -------- Setters --------

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // -------- Lógica de dominio (CLAVE) --------

    /**
     * Devuelve true si la fecha está dentro del rango de indisponibilidad
     */
    public boolean includes(LocalDate date) {
        if (date == null || startDate == null) {
            return false;
        }

        if (endDate == null) {
            return date.equals(startDate);
        }

        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    @Override
    public String toString() {
        return "Unavailable from " + startDate +
                (endDate != null && !endDate.equals(startDate)
                        ? " to " + endDate
                        : "");
    }
}
