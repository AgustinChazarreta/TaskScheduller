package com.AgsCh.task_scheduler.dto.response;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

public class GroupResponseDTO {

    private Long id;
    private String name;
    private List<PersonResponseDTO> persons;
    private Set<DayOfWeek> workingDays;
    private Set<Long> functionIds;

    public GroupResponseDTO() {
    }

    public GroupResponseDTO(Long id, String name, List<PersonResponseDTO> persons, Set<DayOfWeek> workingDays,
            Set<Long> functionIds) {
        this.id = id;
        this.name = name;
        this.persons = persons;
        this.workingDays = workingDays;
        this.functionIds = functionIds;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<PersonResponseDTO> getPersons() {
        return persons;
    }

    public Set<DayOfWeek> getWorkingDays() {
        return workingDays;
    }

    public Set<Long> getFunctionIds() {
        return functionIds;
    }
}