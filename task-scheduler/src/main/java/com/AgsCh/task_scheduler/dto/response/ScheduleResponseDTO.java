package com.AgsCh.task_scheduler.dto.response;

import java.util.List;

public class ScheduleResponseDTO {

    private List<PersonResponseDTO> persons;
    private List<TaskResponseDTO> tasks;
    private List<TaskAssignmentResponseDTO> assignments;
    private String score;

    public ScheduleResponseDTO(List<TaskAssignmentResponseDTO> assignments,
            String score) {
        this.assignments = assignments;
        this.score = score;
        this.persons = null;
        this.tasks = null;
    }

    public ScheduleResponseDTO(List<PersonResponseDTO> persons,
            List<TaskResponseDTO> tasks,
            List<TaskAssignmentResponseDTO> assignments,
            String score) {
        this.persons = persons;
        this.tasks = tasks;
        this.assignments = assignments;
        this.score = score;
    }

    public List<PersonResponseDTO> getPersons() { return persons; }
    public List<TaskResponseDTO> getTasks() { return tasks; }
    public List<TaskAssignmentResponseDTO> getAssignments() { return assignments; }
    public String getScore() { return score; }
    
}