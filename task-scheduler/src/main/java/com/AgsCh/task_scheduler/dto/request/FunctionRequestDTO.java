package com.AgsCh.task_scheduler.dto.request;

import java.time.DayOfWeek;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class FunctionRequestDTO {

    @NotNull
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @NotEmpty
    @JsonProperty("assignedDays")
    private Set<DayOfWeek> assignedDays;

    @NotNull
    private Integer requiredPersons;

    @NotNull
    private boolean sequential;

    public FunctionRequestDTO() {
    }

    // getters & setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<DayOfWeek> getAssignedDays() {
        return assignedDays;
    }

    public void setAssignedDays(Set<DayOfWeek> assignedDays) {
        this.assignedDays = assignedDays;
    }

    public Integer getRequiredPersons() {
        return requiredPersons;
    }

    public void setRequiredPersons(Integer requiredPersons) {
        this.requiredPersons = requiredPersons;
    }

    public boolean isSequential() {
        return sequential;
    }

    public void setSequential(boolean sequential) {
        this.sequential = sequential;
    }
}
