package com.AgsCh.task_scheduler.dto.response;

import java.time.LocalDate;
import java.util.List;

public class ScheduleResponseDTO {

    private LocalDate startDate;
    private LocalDate endDate;

    private List<PersonResponseDTO> persons;
    private List<FunctionResponseDTO> functions;
    private List<FunctionAssignmentResponseDTO> assignments;
    private String score;

    public ScheduleResponseDTO(LocalDate startDate,
            LocalDate endDate,
            List<FunctionAssignmentResponseDTO> assignments,
            String score) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.assignments = assignments;
        this.score = score;
    }

    public ScheduleResponseDTO(LocalDate startDate,
            LocalDate endDate,
            List<PersonResponseDTO> persons,
            List<FunctionResponseDTO> functions,
            List<FunctionAssignmentResponseDTO> assignments,
            String score) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.persons = persons;
        this.functions = functions;
        this.assignments = assignments;
        this.score = score;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<PersonResponseDTO> getPersons() {
        return persons;
    }

    public List<FunctionResponseDTO> getFunctions() {
        return functions;
    }

    public List<FunctionAssignmentResponseDTO> getAssignments() {
        return assignments;
    }

    public String getScore() {
        return score;
    }

}