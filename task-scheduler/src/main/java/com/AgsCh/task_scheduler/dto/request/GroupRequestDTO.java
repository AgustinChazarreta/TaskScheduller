package com.AgsCh.task_scheduler.dto.request;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;

public class GroupRequestDTO {

    @NotBlank
    private String name;
    private List<Long> personIds;
    private Set<DayOfWeek> workingDays;
    private Set<Long> functionIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getPersonIds() {
        return personIds;
    }

    public void setPersonIds(List<Long> personIds) {
        this.personIds = personIds;
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
}