package com.AgsCh.task_scheduler.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public class GroupRequestDTO {

    @NotBlank
    private String name;
    private List<Long> personIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getPersonIds() {
        return personIds;
    }

    public void setPersonIds(List<Long> personIds) {
        this.personIds = personIds;
    }

}