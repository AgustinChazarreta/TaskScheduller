package com.AgsCh.task_scheduler.dto.response;

import java.time.DayOfWeek;
import java.util.Set;

public class FunctionResponseDTO {

    private Long id;
    private String name;
    private boolean sequential;
    private Set<DayOfWeek> assignedDays;
    private Integer requiredPersons;

    public FunctionResponseDTO(Long id, String name, boolean sequential, Set<DayOfWeek> assignedDays,
            Integer requiredPersons) {
        this.id = id;
        this.name = name;
        this.sequential = sequential;
        this.assignedDays = assignedDays;
        this.requiredPersons = requiredPersons;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSequential() {
        return sequential;
    }

    public Set<DayOfWeek> getAssignedDays() {
        return assignedDays;
    }

    public Integer getRequiredPersons() {
        return requiredPersons;
    }
}
