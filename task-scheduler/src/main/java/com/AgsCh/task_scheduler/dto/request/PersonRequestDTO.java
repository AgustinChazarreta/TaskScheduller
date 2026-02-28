package com.AgsCh.task_scheduler.dto.request;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;

public class PersonRequestDTO {

    private Long id;

    @NotBlank
    private String fullName;

    private String nickName;

    private LocalDate birthDate;

    private String email;

    private boolean emailNotificationsEnabled = false;

    private boolean active = true;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate exitDate;

    private Set<DayOfWeek> workingDays;

    private Set<Long> functionIds;

    private List<PersonUnavailabilityDTO> unavailabilities;

    public PersonRequestDTO() {
    }

    // -------- getters & setters --------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }

    public void setExitDate(LocalDate exitDate) {
        this.exitDate = exitDate;
    }

    public Set<DayOfWeek> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(Set<DayOfWeek> workingDays) {
        this.workingDays = workingDays;
    }

    public Set<Long> getFunctionIds() {
        return functionIds;
    }

    public void setFunctionIds(Set<Long> functionIds) {
        this.functionIds = functionIds;
    }

    public List<PersonUnavailabilityDTO> getUnavailabilities() {
        return unavailabilities;
    }

    public void setUnavailabilities(List<PersonUnavailabilityDTO> unavailabilities) {
        this.unavailabilities = unavailabilities;
    }
}
