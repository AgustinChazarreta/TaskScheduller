package com.AgsCh.task_scheduler.dto.response;

import java.util.List;

public class ScheduleResponseDTO {

    private List<PersonResponseDTO> persons;
    private List<FunctionResponseDTO> functions;
    private List<FunctionAssignmentResponseDTO> assignments;
    private String score;

    public ScheduleResponseDTO(List<FunctionAssignmentResponseDTO> assignments,
            String score) {
        this.assignments = assignments;
        this.score = score;
    }

    public ScheduleResponseDTO(List<PersonResponseDTO> persons,
            List<FunctionResponseDTO> functions,
            List<FunctionAssignmentResponseDTO> assignments,
            String score) {
        this.persons = persons;
        this.functions = functions;
        this.assignments = assignments;
        this.score = score;
    }

    public List<PersonResponseDTO> getPersons() { return persons; }
    public List<FunctionResponseDTO> getFunctions() { return functions; }
    public List<FunctionAssignmentResponseDTO> getAssignments() { return assignments; }
    public String getScore() { return score; }
    
}