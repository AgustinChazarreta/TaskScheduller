package com.AgsCh.task_scheduler.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public class PersonResponseDTO {

    private Long id;
    private String fullName;
    private String nickName;
    private LocalDate birthDate;
    private boolean active;
    private Set<DayOfWeek> workingDays;

    public PersonResponseDTO(
            Long id,
            String fullName,
            LocalDate birthDate,
            boolean active,
            Set<DayOfWeek> workingDays) {

        this.id = id;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.active = active;
        this.workingDays = workingDays;
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
    
    public boolean isActive() {
        return active;
    }

    public Set<DayOfWeek> getWorkingDays() {
        return workingDays;
    }
}
