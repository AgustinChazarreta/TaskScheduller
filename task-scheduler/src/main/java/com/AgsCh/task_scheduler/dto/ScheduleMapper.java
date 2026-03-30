package com.AgsCh.task_scheduler.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

                    int required = Math.max(1, function.getRequiredPersons());

                    for (int i = 0; i < required; i++) {

                        FunctionAssignment assignment = new FunctionAssignment(function, date, i);

                        assignments.add(assignment);
                    }
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

        List<FunctionAssignment> sorted = solution.getFunctionAssignmentList()
                .stream()
                .sorted(Comparator
                        .comparing(FunctionAssignment::getDate)
                        .thenComparing(a -> a.getFunction().getName())
                        .thenComparing(FunctionAssignment::getIndex))
                .toList();

        Map<String, List<FunctionAssignment>> grouped = sorted.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getFunction().getName() + "|" + a.getDate(),
                        LinkedHashMap::new, // 🔥 mantiene orden
                        Collectors.toList()));

        List<FunctionAssignmentResponseDTO> responses = new ArrayList<>();

        for (List<FunctionAssignment> group : grouped.values()) {

            FunctionAssignment first = group.get(0);

            List<String> personNames = group.stream()
                    .sorted(Comparator.comparingInt(FunctionAssignment::getIndex))
                    .map(a -> a.getPerson() != null
                            ? a.getPerson().getFullName()
                            : "UNASSIGNED")
                    .toList();

            List<String> nicknames = group.stream()
                    .sorted(Comparator.comparingInt(FunctionAssignment::getIndex))
                    .map(a -> a.getPerson() != null
                            ? a.getPerson().getNickName()
                            : "")
                    .toList();

            responses.add(new FunctionAssignmentResponseDTO(
                    first.getDate(),
                    first.getFunction().getName(),
                    personNames,
                    nicknames));
        }

        return new ScheduleResponseDTO(
                responses,
                solution.getScore() != null ? solution.getScore().toString() : "NO_SCORE");
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

        Map<String, List<FunctionAssignment>> grouped = run.getAssignments()
                .stream()
                .collect(Collectors.groupingBy(a -> a.getFunction().getName() + "|" + a.getDate()));

        List<FunctionAssignmentResponseDTO> responses = new ArrayList<>();

        for (List<FunctionAssignment> group : grouped.values()) {

            FunctionAssignment first = group.get(0);

            List<String> personNames = group.stream()
                    .sorted(Comparator.comparingInt(FunctionAssignment::getIndex))
                    .map(a -> a.getPerson() != null
                            ? a.getPerson().getFullName()
                            : "UNASSIGNED")
                    .toList();

            List<String> nicknames = group.stream()
                    .sorted(Comparator.comparingInt(FunctionAssignment::getIndex))
                    .map(a -> a.getPerson() != null
                            ? a.getPerson().getNickName()
                            : "")
                    .toList();

            responses.add(new FunctionAssignmentResponseDTO(
                    first.getDate(),
                    first.getFunction().getName(),
                    personNames,
                    nicknames));
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
                    .orElseThrow(() -> new BusinessException("Función no encontrada: " + dto.getFunctionName()));

            // 🔹 Usar fecha directa (NO reconstruir con week/day)
            LocalDate date = dto.getDate();

            int index = 0;

            for (String personName : dto.getPersonNames()) {

                // 🔹 Buscar persona (puede ser null si es UNASSIGNED)
                Person person = personName.equals("UNASSIGNED")
                        ? null
                        : persons.stream()
                                .filter(p -> p.getFullName().equals(personName))
                                .findFirst()
                                .orElse(null);

                FunctionAssignment assignment = new FunctionAssignment(function, date, index++);

                assignment.setPerson(person);

                assignments.add(assignment);
            }
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
