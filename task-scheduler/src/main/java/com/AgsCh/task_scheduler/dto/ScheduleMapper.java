package com.AgsCh.task_scheduler.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.AgsCh.task_scheduler.dto.request.PlanningPeriodRequestDTO;
import com.AgsCh.task_scheduler.dto.request.PersonRequestDTO;
import com.AgsCh.task_scheduler.dto.request.ScheduleRequestDTO;
import com.AgsCh.task_scheduler.dto.request.TaskRequestDTO;
import com.AgsCh.task_scheduler.dto.response.ScheduleResponseDTO;
import com.AgsCh.task_scheduler.dto.response.TaskAssignmentResponseDTO;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.Task;
import com.AgsCh.task_scheduler.model.TaskAssignment;

public class ScheduleMapper {

    /*
     * ======================
     * REQUEST → MODEL
     * ======================
     */

    public static Schedule toModel(ScheduleRequestDTO request) {

        // -------- persons --------
        List<Person> persons = new ArrayList<>();
        for (PersonRequestDTO p : request.getPersons()) {
            persons.add(new Person(
                    p.getName(),
                    p.getCategory(),
                    p.getBirthDate(),
                    p.getAvailableDays()));
        }

        // -------- tasks --------
        List<Task> tasks = new ArrayList<>();
        for (TaskRequestDTO t : request.getTasks()) {
            tasks.add(new Task(
                    t.getName(),
                    t.getAllowedCategories(),
                    t.getAssignedDays()));
        }

        // -------- planning period --------
        PlanningPeriodRequestDTO period = request.getPeriod();
        LocalDate startDate = period.getStartDate();
        LocalDate endDate = period.getEndDate();

        // -------- task assignments (KEY PART) --------
        List<TaskAssignment> assignments = new ArrayList<>();

        for (Task task : tasks) {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

                DayOfWeek dayOfWeek = date.getDayOfWeek();

                if (task.getAssignedDays().contains(dayOfWeek)) {
                    TaskAssignment assignment = new TaskAssignment(task, date);
                    assignment.setDate(date); // 🔥 ESTA LÍNEA ES CLAVE
                    assignments.add(assignment);
                }
            }
        }





        System.out.println("==== DEBUG ASSIGNMENTS GENERADOS ====");
        for (TaskAssignment a : assignments) {
            System.out.println(
                    "task=" + a.getTask().getName() +
                            " | day=" + a.getDate().getDayOfWeek() +
                            " | date=" + a.getDate());
        }
        System.out.println("====================================");








        return new Schedule(persons, tasks, assignments);
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

            assignments.add(
                    new TaskAssignmentResponseDTO(
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
