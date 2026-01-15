package com.AgsCh.task_scheduler.dto;

import java.time.DayOfWeek;
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

public class ScheduleMapper {

    private ScheduleMapper() {
        // utility class
    }

    /*
     * ======================
     * REQUEST → MODEL
     * ======================
     */

    public static Schedule toModel(ScheduleRequestDTO request) {

        List<Person> persons = mapPersons(request.getPersons());
        List<Task> tasks = mapTasks(request.getTasks());

        PlanningPeriodRequestDTO period = request.getPeriod();
        List<TaskAssignment> assignments = buildAssignments(
                tasks,
                period.getStartDate(),
                period.getEndDate());

        return new Schedule(persons, tasks, assignments);
    }

    private static List<Person> mapPersons(List<PersonRequestDTO> personDTOs) {
        List<Person> persons = new ArrayList<>();

        for (PersonRequestDTO p : personDTOs) {
            persons.add(new Person(
                    p.getName(),
                    p.getCategory(),
                    p.getBirthDate(),
                    p.getAvailableDays()));
        }

        return persons;
    }

    private static List<Task> mapTasks(List<TaskRequestDTO> taskDTOs) {
        List<Task> tasks = new ArrayList<>();

        for (TaskRequestDTO t : taskDTOs) {
            tasks.add(new Task(
                    t.getName(),
                    t.getAllowedCategories(),
                    t.getAssignedDays()));
        }

        return tasks;
    }

    private static List<TaskAssignment> buildAssignments(
            List<Task> tasks,
            LocalDate startDate,
            LocalDate endDate) {

        List<TaskAssignment> assignments = new ArrayList<>();

        for (Task task : tasks) {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

                DayOfWeek dayOfWeek = date.getDayOfWeek();

                if (task.getAssignedDays().contains(dayOfWeek)) {
                    TaskAssignment assignment = new TaskAssignment(task, date);
                    assignment.setDate(date);
                    assignments.add(assignment);
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

        List<TaskAssignmentResponseDTO> assignments = new ArrayList<>();

        for (TaskAssignment a : solution.getTaskAssignmentList()) {

            String personName = a.getPerson() != null
                    ? a.getPerson().getName()
                    : "UNASSIGNED";

            assignments.add(new TaskAssignmentResponseDTO(
                    a.getDate().getDayOfWeek(),
                    a.getTask().getName(),
                    personName));
        }

        String score = solution.getScore() != null
                ? solution.getScore().toString()
                : "NO_SCORE";

        return new ScheduleResponseDTO(assignments, score);
    }
}
