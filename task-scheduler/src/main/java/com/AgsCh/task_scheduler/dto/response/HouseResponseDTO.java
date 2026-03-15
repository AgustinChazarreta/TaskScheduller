package com.AgsCh.task_scheduler.dto.response;

import com.AgsCh.task_scheduler.model.House;

public class HouseResponseDTO {

    private Long id;
    private String name;
    private boolean active;
    private String createdAt;

    public HouseResponseDTO(House house) {
        this.id = house.getId();
        this.name = house.getName();
        this.active = house.isActive();
        this.createdAt = house.getCreatedAt().toString();
    }

    // getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}