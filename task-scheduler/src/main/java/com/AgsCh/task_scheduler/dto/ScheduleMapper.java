package com.AgsCh.task_scheduler.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.AgsCh.task_scheduler.dto.request.*;
import com.AgsCh.task_scheduler.dto.response.*;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.*;

public final class ScheduleMapper {

    private ScheduleMapper() {
        // utility class
    }

    /*
     * ======================
     * REQUEST → MODEL
     * ======================
     */

    public static Schedule toModel(ScheduleRequestDTO request) {

        List<Person> persons = toPersons(request.getPersons());
        List<Task> tasks = toTasks(request.getTasks());

        LocalDate start = request.getPeriod().getStartDate();
        LocalDate end = request.getPeriod().getEndDate();

        List<TaskAssignment> assignments = createAssignments(tasks, start, end);

        return new Schedule(persons, tasks, assignments);
    }

    private static List<Person> toPersons(List<PersonRequestDTO> dtos) {
        List<Person> persons = new ArrayList<>();

        for (PersonRequestDTO dto : dtos) {
            persons.add(new Person(
                    dto.getName(),
                    dto.getCategory(),
                    dto.getBirthDate(),
                    dto.getAvailableDays()));
        }
        return persons;
    }

    private static List<Task> toTasks(List<TaskRequestDTO> dtos) {
        List<Task> tasks = new ArrayList<>();

        for (TaskRequestDTO dto : dtos) {
            tasks.add(new Task(
                    dto.getName(),
                    dto.getAllowedCategories(),
                    dto.getAssignedDays()));
        }
        return tasks;
    }

    private static List<TaskAssignment> createAssignments(
            List<Task> tasks,
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date is after end date");
        }

        List<TaskAssignment> assignments = new ArrayList<>();

        for (Task task : tasks) {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                if (task.getAssignedDays().contains(date.getDayOfWeek())) {
                    assignments.add(new TaskAssignment(task, date));
                }
            }
        }
        return assignments;
    }

    /*
     * ======================
     * MODEL → RESPONSE
     * ======================
     */

    public static ScheduleResponseDTO toResponse(Schedule solution) {

        List<TaskAssignmentResponseDTO> assignmentResponses = new ArrayList<>();

        for (TaskAssignment assignment : solution.getTaskAssignmentList()) {

            String personName = assignment.getPerson() != null
                    ? assignment.getPerson().getName()
                    : "UNASSIGNED";

            assignmentResponses.add(new TaskAssignmentResponseDTO(
                    assignment.getDate(),
                    assignment.getTask().getName(),
                    personName));
        }

        String score = solution.getScore() != null
                ? solution.getScore().toString()
                : "NO_SCORE";

        return new ScheduleResponseDTO(assignmentResponses, score);
    }
}
