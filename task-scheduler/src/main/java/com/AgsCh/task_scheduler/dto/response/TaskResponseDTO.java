package com.AgsCh.task_scheduler.dto.response;

public class TaskResponseDTO {

    private String name;
    private String category;

    public TaskResponseDTO(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
