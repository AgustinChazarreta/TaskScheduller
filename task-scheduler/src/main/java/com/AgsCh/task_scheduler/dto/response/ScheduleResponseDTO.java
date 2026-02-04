package com.AgsCh.task_scheduler.dto.response;

import java.util.List;

public class ScheduleResponseDTO {

    private List<PersonResponseDTO> persons;
    private List<FunctionResponseDTO> tasks;
    private List<FunctionAssignmentResponseDTO> assignments;
    private String score;

    public ScheduleResponseDTO(List<FunctionAssignmentResponseDTO> assignments,
            String score) {
        this.assignments = assignments;
        this.score = score;
    }

    public ScheduleResponseDTO(List<PersonResponseDTO> persons,
            List<FunctionResponseDTO> tasks,
            List<FunctionAssignmentResponseDTO> assignments,
            String score) {
        this.persons = persons;
        this.tasks = tasks;
        this.assignments = assignments;
        this.score = score;
    }

    public List<PersonResponseDTO> getPersons() { return persons; }
    public List<FunctionResponseDTO> getTasks() { return tasks; }
    public List<FunctionAssignmentResponseDTO> getAssignments() { return assignments; }
    public String getScore() { return score; }
    
}