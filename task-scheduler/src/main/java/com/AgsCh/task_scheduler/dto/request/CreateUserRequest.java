package com.AgsCh.task_scheduler.dto.request;

public class CreateUserRequest {
    private String fullName;
    private String email;
    private Long houseId;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getHouseId() { return houseId; }
    public void setHouseId(Long houseId) { this.houseId = houseId; }
}