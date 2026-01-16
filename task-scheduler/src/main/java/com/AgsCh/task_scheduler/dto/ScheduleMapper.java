package com.AgsCh.task_scheduler.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.dto.request.TaskRequestDTO;
import com.AgsCh.task_scheduler.dto.request.requestAdapter.PlanningPeriodRequestDTO;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.dto.response.TaskAssignmentResponseDTO;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.Task;
import com.AgsCh.task_scheduler.model.TaskAssignment;

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

        PlanningPeriodRequestDTO period = request.getPeriod();

        List<TaskAssignment> assignments = createAssignments(
                tasks,
                period.getStartDate(),
                period.getEndDate());

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
                    assignment.getDate().getDayOfWeek(),
                    assignment.getTask().getName(),
                    personName));
        }

        String score = solution.getScore() != null
                ? solution.getScore().toString()
                : "NO_SCORE";

        return new ScheduleResponseDTO(assignmentResponses, score);
    }
}
