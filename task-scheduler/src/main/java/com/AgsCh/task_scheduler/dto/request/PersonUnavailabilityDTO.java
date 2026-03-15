package com.AgsCh.task_scheduler.dto.request;

import java.time.LocalDate;

import com.AgsCh.task_scheduler.model.PersonUnavailability;

public class PersonUnavailabilityDTO {
    private LocalDate startDate;
    private LocalDate endDate; // opcional
    private String reason;

    public PersonUnavailabilityDTO() {
    }

    public PersonUnavailabilityDTO(LocalDate starDate, LocalDate endDate, String reason) {
        this.startDate = starDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    public PersonUnavailabilityDTO(PersonUnavailability u) {
        this.startDate = u.getStartDate();
        this.endDate = u.getEndDate();
        this.reason = u.getReason();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}