package com.AgsCh.task_scheduler.dto.request.requestAdapter;

import java.util.List;
import java.util.stream.Collectors;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.dto.request.TaskRequestDTO;
import com.AgsCh.task_scheduler.model.Task;

public class ScheduleRequestDTOAdapter {

    private ScheduleRequestDTOAdapter() {
        // utility class
    }

    public static ScheduleRequestDTO adapt(
            PlanningPeriodRequestDTO period,
            List<PersonRequestDTO> persons,
            List<Task> tasks) {

        ScheduleRequestDTO dto = new ScheduleRequestDTO();
        dto.setPeriod(copyPeriod(period));
        dto.setPersons(persons);
        dto.setTasks(toTaskDTOs(tasks));

        return dto;
    }

    private static PlanningPeriodRequestDTO copyPeriod(PlanningPeriodRequestDTO period) {
        PlanningPeriodRequestDTO dto = new PlanningPeriodRequestDTO();
        dto.setStartDate(period.getStartDate());
        dto.setEndDate(period.getEndDate());
        return dto;
    }

    private static List<TaskRequestDTO> toTaskDTOs(List<Task> tasks) {
        return tasks.stream()
                .map(task -> {
                    TaskRequestDTO dto = new TaskRequestDTO();
                    dto.setName(task.getName());
                    dto.setAllowedCategories(task.getAllowedCategories());
                    dto.setAssignedDays(task.getAssignedDays());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
