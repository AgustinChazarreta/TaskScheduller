package com.AgsCh.task_scheduler.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public class PersonResponseDTO {

    private Long id;
    private String fullName;
    private String nickName;
    private LocalDate birthDate;
    private String email;
    private boolean emailNotificationsEnabled;
    private boolean active;
    private LocalDate entryDate;
    private LocalDate exitDate;
    private Set<DayOfWeek> workingDays;
    private Set<FunctionResponseDTO> functions;
    private String profileImageUrl;

    public PersonResponseDTO(
            Long id,
            String fullName,
            String nickName,
            LocalDate birthDate,
            String email,
            boolean emailNotificationsEnabled,
            boolean active,
            LocalDate entryDate,
            LocalDate exitDate,
            Set<DayOfWeek> workingDays,
            Set<FunctionResponseDTO> functions,
            String profileImageUrl) {

        this.id = id;
        this.fullName = fullName;
        this.nickName = nickName;
        this.birthDate = birthDate;
        this.email = email;
        this.emailNotificationsEnabled = emailNotificationsEnabled;
        this.active = active;
        this.entryDate = entryDate;
        this.exitDate = exitDate;
        this.workingDays = workingDays;
        this.functions = functions;
        this.profileImageUrl = profileImageUrl;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getNickName() {
        return nickName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }

    public Set<DayOfWeek> getWorkingDays() {
        return workingDays;
    }

    public Set<FunctionResponseDTO> getFunctions() {
        return functions;
    }  
    
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
