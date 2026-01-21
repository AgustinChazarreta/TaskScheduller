package com.AgsCh.task_scheduler.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.Task;

public class SolveRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Person> selectedPersons;  // Cambiado a List<Person>
    private List<Task> tasks;  // Agregado

    // Getters y setters
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<Person> getSelectedPersons() { return selectedPersons; }
    public void setSelectedPersons(List<Person> selectedPersons) { this.selectedPersons = selectedPersons; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }
}