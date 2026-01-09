package com.AgsCh.task_scheduler.dto;

import java.util.ArrayList;
import java.util.List;

import com.AgsCh.task_scheduler.model.*;

public class ScheduleMapper {

    public static Schedule toModel(ScheduleRequestDTO dto) {

        List<Person> persons = dto.persons.stream()
            .map(p -> new Person(
                p.name,
                p.category,
                null,
                p.availableDays
            ))
            .toList();

        List<Task> tasks = dto.tasks.stream()
            .map(t -> new Task(
                t.name,
                t.category,
                t.assignedDays
            ))
            .toList();

        List<TaskAssignment> assignments = new ArrayList<>();

        for (Task task : tasks) {
            for (var day : task.getAssignedDays()) {
                assignments.add(new TaskAssignment(task, day));
            }
        }

        return new Schedule(persons, tasks, assignments);
    }

    public static ScheduleResponseDTO toResponse(Schedule solution) {

        ScheduleResponseDTO response = new ScheduleResponseDTO();
        response.assignments = solution.getTaskAssignmentList().stream()
            .map(a -> {
                TaskAssignmentResultDTO dto = new TaskAssignmentResultDTO();
                dto.taskName = a.getTask().getName();
                dto.day = a.getDay();
                dto.personName = a.getPerson().getName();
                return dto;
            })
            .toList();

        response.score = solution.getScore().toString();
        return response;
    }
}
