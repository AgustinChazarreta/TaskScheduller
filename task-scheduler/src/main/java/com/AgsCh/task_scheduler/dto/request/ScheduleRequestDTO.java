package com.AgsCh.task_scheduler.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class ScheduleRequestDTO {

    @Valid
    @NotEmpty
    private List<PersonRequestDTO> persons;

    @Valid
    @NotEmpty
    private List<TaskRequestDTO> tasks;

    // getters & setters
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