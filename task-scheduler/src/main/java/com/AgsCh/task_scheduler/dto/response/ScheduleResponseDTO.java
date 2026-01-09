package com.AgsCh.task_scheduler.dto.response;

import java.util.List;

public class ScheduleResponseDTO {

    private List<TaskAssignmentResponseDTO> assignments;
    private String score;

    public ScheduleResponseDTO() {}

    public ScheduleResponseDTO(
        List<TaskAssignmentResponseDTO> assignments,
        String score
    ) {
        this.assignments = assignments;
        this.score = score;
    }

    public List<TaskAssignmentResponseDTO> getAssignments() {
        return assignments;
    }

    public String getScore() {
        return score;
    }
}
