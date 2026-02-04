package com.AgsCh.task_scheduler.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.AgsCh.task_scheduler.dto.request.*;
import com.AgsCh.task_scheduler.dto.response.*;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.*;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.repository.TaskRepository;

public final class ScheduleMapper {

    private ScheduleMapper() {
        // utility class
    }

    /*
     * ======================
     * REQUEST → MODEL
     * ======================
     */

    public static Schedule toModel(
            ScheduleRequestDTO request,
            TaskRepository taskRepository,
            PersonRepository personRepository) {

        // 1️⃣ Traer Tasks y Persons existentes de la DB
        List<Function> tasks = loadTasks(request.getTasks(), taskRepository);
        List<Person> persons = loadPersons(request.getPersons(), personRepository);

        // 2️⃣ Fechas
        LocalDate start = request.getPeriod().getStartDate();
        LocalDate end = request.getPeriod().getEndDate();

        // 3️⃣ Crear TaskAssignments con planningId
        List<FunctionAssignment> assignments = createAssignments(tasks, start, end);

        return new Schedule(persons, tasks, assignments, start, end);
    }

    private static List<Function> loadTasks(List<TaskRequestDTO> dtos, TaskRepository repo) {
        List<Function> tasks = new ArrayList<>();
        for (TaskRequestDTO dto : dtos) {
            // Asume que cada TaskRequestDTO tiene un campo id que ya existe en la DB
            Function task = repo.findById(dto.getId())
                    .orElseThrow(() -> new BusinessException(
                            "Task no encontrada en DB: " + dto.getId()));
            tasks.add(task);
        }
        return tasks;
    }

    private static List<Person> loadPersons(List<PersonRequestDTO> dtos, PersonRepository repo) {
        List<Person> persons = new ArrayList<>();
        for (PersonRequestDTO dto : dtos) {
            // Asume que cada PersonRequestDTO tiene un id válido
            Person person = repo.findById(dto.getId())
                    .orElseThrow(() -> new BusinessException(
                            "Person no encontrada en DB: " + dto.getId()));
            persons.add(person);
        }
        return persons;
    }

    private static List<FunctionAssignment> createAssignments(
            List<Function> tasks,
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date is after end date");
        }

        List<FunctionAssignment> assignments = new ArrayList<>();

        for (Function task : tasks) {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                if (task.getAssignedDays().contains(date.getDayOfWeek())) {
                    FunctionAssignment ta = new FunctionAssignment(task, date); // planningId generado automáticamente
                    ta.setPerson(null); // UNASSIGNED
                    assignments.add(ta);
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

        for (FunctionAssignment assignment : solution.getFunctionAssignmentList()) {

            String personName = assignment.getPerson() != null
                    ? assignment.getPerson().getFullName()
                    : "UNASSIGNED";

            assignmentResponses.add(new TaskAssignmentResponseDTO(
                    assignment.getDate(),
                    assignment.getFunction().getName(),
                    personName));
        }

        String score = solution.getScore() != null
                ? solution.getScore().toString()
                : "NO_SCORE";

        return new ScheduleResponseDTO(assignmentResponses, score);
    }
}
