package com.AgsCh.task_scheduler.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ScheduleRequestDTO {

    @Valid
    @NotNull
    private PlanningPeriodRequestDTO period;

    @Valid
    @NotEmpty
    private List<PersonRequestDTO> persons;

    @Valid
    @NotEmpty
    private List<TaskRequestDTO> tasks;

    // getters & setters

    public PlanningPeriodRequestDTO getPeriod() {
        return period;
    }

    public void setPeriod(PlanningPeriodRequestDTO period) {
        this.period = period;
    }

    public List<PersonRequestDTO> getPersons() {
        return persons;
    }

    public void setPersons(List<PersonRequestDTO> persons) {
        this.persons = persons;
    }

    public List<TaskRequestDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskRequestDTO> tasks) {
        this.tasks = tasks;
    }
}
