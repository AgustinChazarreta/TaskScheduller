package com.AgsCh.task_scheduler.dto;

import java.util.ArrayList;
import java.util.List;

import com.AgsCh.task_scheduler.dto.request.*;
import com.AgsCh.task_scheduler.dto.response.*;
import com.AgsCh.task_scheduler.model.*;

public class ScheduleMapper {

    /* ======================
        REQUEST → MODEL
       ====================== */

    public static Schedule toModel(ScheduleRequestDTO request) {

        // Personas
        List<Person> persons = new ArrayList<>();
        for (PersonRequestDTO p : request.getPersons()) {
            persons.add(new Person(
                p.getName(),
                p.getCategory(),
                p.getBirthDate(),
                p.getAvailableDays()
            ));
        }

        // Tareas
        List<Task> tasks = new ArrayList<>();
        for (TaskRequestDTO t : request.getTasks()) {
            tasks.add(new Task(
                t.getName(),
                t.getCategory(),
                t.getAssignedDays()
            ));
        }

        // Assignments (una por tarea y día)
        List<TaskAssignment> assignments = new ArrayList<>();
        for (Task task : tasks) {
            for (var day : task.getAssignedDays()) {
                assignments.add(new TaskAssignment(task, day));
            }
        }

        return new Schedule(persons, tasks, assignments);
    }

    /* ======================
        MODEL → RESPONSE
       ====================== */

    public static ScheduleResponseDTO toResponse(Schedule solution) {

        List<TaskAssignmentResponseDTO> assignments = new ArrayList<>();

        for (TaskAssignment a : solution.getTaskAssignmentList()) {
            assignments.add(new TaskAssignmentResponseDTO(
                a.getDay(),
                a.getTask().getName(),
                a.getPerson().getName()
            ));
        }

        return new ScheduleResponseDTO(
            assignments,
            solution.getScore().toString()
        );
    }
}
