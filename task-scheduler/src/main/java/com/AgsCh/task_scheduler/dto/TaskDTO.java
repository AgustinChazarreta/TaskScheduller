package com.AgsCh.task_scheduler.dto;

import java.time.DayOfWeek;
import java.util.List;

public class TaskDTO {

    public String name;
    public String category;
    public List<DayOfWeek> assignedDays;
}
