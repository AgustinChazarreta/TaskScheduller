package com.AgsCh.task_scheduler.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.AgsCh.task_scheduler.dto.request.*;
import com.AgsCh.task_scheduler.dto.response.*;
import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.*;
import com.AgsCh.task_scheduler.repository.PersonRepository;
import com.AgsCh.task_scheduler.repository.FunctionRepository;

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
            FunctionRepository functionRepository,
            PersonRepository personRepository) {

        // 1️⃣ Traer Functions y Persons existentes de la DB
        List<Function> functions = loadFunctions(request.getFunctionIds(), functionRepository);
        List<Person> persons = loadPersons(request.getPersonIds(), personRepository);

        // 2️⃣ Fechas
        LocalDate start = request.getPeriod().getStartDate();
        LocalDate end = request.getPeriod().getEndDate();

        // 3️⃣ Crear TaskAssignments con planningId
        List<FunctionAssignment> assignments = createAssignments(functions, start, end);

        return new Schedule(persons, functions, assignments, start, end);
    }

    private static List<Function> loadFunctions(List<Long> ids, FunctionRepository repo) {
        List<Function> functions = new ArrayList<>();
        for (Long id : ids) {
            // Asume que cada id ya existe en la DB
            Function function = repo.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            "Function no encontrada en DB: " + id));
            functions.add(function);
        }
        return functions;
    }

    private static List<Person> loadPersons(List<Long> ids, PersonRepository repo) {
        List<Person> persons = new ArrayList<>();
        for (Long id : ids) {
            // Asume que cada id ya existe en la DB
            Person person = repo.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            "Person no encontrada en DB: " + id));
            persons.add(person);
        }
        return persons;
    }

    private static List<FunctionAssignment> createAssignments(
            List<Function> functions,
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date is after end date");
        }

        List<FunctionAssignment> assignments = new ArrayList<>();

        for (Function function : functions) {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                if (function.getAssignedDays().contains(date.getDayOfWeek())) {
                    FunctionAssignment ta = new FunctionAssignment(function, date); // planningId generado
                                                                                    // automáticamente
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

        List<FunctionAssignmentResponseDTO> responses = new ArrayList<>();

        for (FunctionAssignment assignment : solution.getFunctionAssignmentList()) {

            Person person = assignment.getPerson();

            String personName = person != null
                    ? person.getFullName()
                    : "UNASSIGNED";

            String personNickname = person != null
                    ? person.getNickName()
                    : "";

            responses.add(new FunctionAssignmentResponseDTO(
                    assignment.getDate(),
                    assignment.getFunction().getName(),
                    personName,
                    personNickname));
        }

        String score = solution.getScore() != null
                ? solution.getScore().toString()
                : "NO_SCORE";

        return new ScheduleResponseDTO(responses, score);
    }

    /*
     * ======================
     * RUN → RESPONSE
     * ======================
     */

    public static ScheduleResponseDTO toResponse(ScheduleRun run) {

        if (run == null) {
            throw new BusinessException("ScheduleRun es null");
        }

        List<FunctionAssignmentResponseDTO> responses = new ArrayList<>();

        for (FunctionAssignment assignment : run.getAssignments()) {

            Person person = assignment.getPerson();

            String personName = person != null
                    ? person.getFullName()
                    : "UNASSIGNED";

            String personNickname = person != null
                    ? person.getNickName()
                    : "";

            responses.add(new FunctionAssignmentResponseDTO(
                    assignment.getDate(),
                    assignment.getFunction().getName(),
                    personName,
                    personNickname));
        }

        String score = run.getScore() != null
                ? run.getScore()
                : "NO_SCORE";

        return new ScheduleResponseDTO(responses, score);
    }

    public static Schedule toModelFromAssignments(
        List<FunctionAssignmentResponseDTO> dtos,
        List<Function> functions,
        List<Person> persons) {

    if (dtos == null || dtos.isEmpty()) {
        throw new BusinessException("No hay assignments para crear el Schedule");
    }

    List<FunctionAssignment> assignments = new ArrayList<>();

    for (FunctionAssignmentResponseDTO dto : dtos) {

        // 🔹 Buscar función por nombre
        Function function = functions.stream()
                .filter(f -> f.getName().equals(dto.getFunctionName()))
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException("Función no encontrada: " + dto.getFunctionName()));

        // 🔹 Buscar persona (puede ser null si es UNASSIGNED)
        Person person = persons.stream()
                .filter(p -> p.getFullName().equals(dto.getPersonName()))
                .findFirst()
                .orElse(null);

        // 🔹 Usar fecha directa (NO reconstruir con week/day)
        LocalDate date = dto.getDate();

        FunctionAssignment assignment = new FunctionAssignment(function, date);
        assignment.setPerson(person);

        assignments.add(assignment);
    }

    // 🔹 Calcular rango real del schedule
    LocalDate startDate = assignments.stream()
            .map(FunctionAssignment::getDate)
            .min(LocalDate::compareTo)
            .orElseThrow();

    LocalDate endDate = assignments.stream()
            .map(FunctionAssignment::getDate)
            .max(LocalDate::compareTo)
            .orElseThrow();

    return new Schedule(persons, functions, assignments, startDate, endDate);
}

}
