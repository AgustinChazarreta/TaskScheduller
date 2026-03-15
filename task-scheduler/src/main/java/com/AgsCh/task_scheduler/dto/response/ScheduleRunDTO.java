package com.AgsCh.task_scheduler.dto.response;

import com.AgsCh.task_scheduler.model.ScheduleRun;

public class ScheduleRunDTO {

    private Long id;
    private String status;
    private String score;
    private String createdAt;
    private String startDate;
    private String endDate;
    private HouseResponseDTO house;

    public ScheduleRunDTO(ScheduleRun run) {
        this.id = run.getId();
        this.status = run.getStatus() != null ? run.getStatus().name() : null;
        this.score = run.getScore() != null ? run.getScore().toString() : null;
        this.createdAt = run.getCreatedAt() != null ? run.getCreatedAt().toString() : null;
        this.startDate = run.getStartDate() != null ? run.getStartDate().toString() : null;
        this.endDate = run.getEndDate() != null ? run.getEndDate().toString() : null;
        this.house = run.getHouse() != null ? new HouseResponseDTO(run.getHouse()) : null;
    }

    // getters y setters
    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getScore() {
        return score;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public HouseResponseDTO getHouse() {
        return house;
    }
}