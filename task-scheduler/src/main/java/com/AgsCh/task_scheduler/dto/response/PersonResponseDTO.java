package com.AgsCh.task_scheduler.dto.response;

import java.time.DayOfWeek;
import java.util.List;

public class PersonResponseDTO {

    private String name;
    private String category;
    private List<DayOfWeek> availableDays;

    public PersonResponseDTO(String name, String category, List<DayOfWeek> availableDays) {
        this.name = name;
        this.category = category;
        this.availableDays = availableDays;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public List<DayOfWeek> getAvailableDays() {
        return availableDays;
    }
}
