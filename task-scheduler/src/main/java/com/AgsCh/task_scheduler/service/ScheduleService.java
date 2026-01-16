package com.AgsCh.task_scheduler.service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.TaskAssignment;
import com.AgsCh.task_scheduler.planner.ScheduleConstraintProvider;

@Service
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

    private final SolverFactory<Schedule> solverFactory;

    // tiempo límite del solver inyectable desde application.properties
    public ScheduleService(@Value("${solver.timeLimitSeconds:2}") long timeLimitSeconds) {

        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(Schedule.class)
                .withEntityClasses(TaskAssignment.class)
                .withConstraintProviderClass(ScheduleConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(timeLimitSeconds));

        this.solverFactory = SolverFactory.create(solverConfig);
    }

    /**
     * Resuelve un Schedule usando OptaPlanner
     */
    public Schedule solve(Schedule problem) {

        validateProblem(problem);

        log.info("Iniciando resolución del schedule: {} personas, {} tareas, {} asignaciones",
                problem.getPersonList().size(),
                problem.getTaskList().size(),
                problem.getTaskAssignmentList().size());

        Solver<Schedule> solver = solverFactory.buildSolver();
        Schedule solution = solver.solve(problem);

        log.info("Schedule resuelto correctamente");

        // --- Debug: mostrar mensajes de hard + soft constraints ---
        printConstraintViolations(solution);

        return solution;
    }

    /**
     * Validaciones básicas y consistencia de datos
     */
    private void validateProblem(Schedule problem) {

        if (problem.getPersonList() == null || problem.getPersonList().isEmpty()) {
            throw new BusinessException("Persons list is empty");
        }

        if (problem.getTaskList() == null || problem.getTaskList().isEmpty()) {
            throw new BusinessException("Tasks list is empty");
        }

        if (problem.getTaskAssignmentList() == null || problem.getTaskAssignmentList().isEmpty()) {
            throw new BusinessException("Task assignments list is empty");
        }

        problem.getTaskAssignmentList().forEach(a -> {
            if (Objects.isNull(a.getTask())) {
                throw new BusinessException("TaskAssignment sin Task asignada");
            }
            if (Objects.isNull(a.getDate())) {
                throw new BusinessException("TaskAssignment sin fecha asignada");
            }
        });

        log.debug("Validación del schedule completada con éxito");
    }

    /**
     * Método para imprimir mensajes de hard + soft constraints sin ScoreDirector
     */
    private void printConstraintViolations(Schedule solution) {
        System.out.println("=== Constraint violations ===");

        Map<String, List<TaskAssignment>> personAssignments = new HashMap<>();

        // --- HARD CONSTRAINTS ---
        for (TaskAssignment ta : solution.getTaskAssignmentList()) {

            if (ta.getPerson() == null) {
                System.out.println("[Task has no assigned person] Tarea: " + ta.getTask().getName()
                        + ", Fecha: " + ta.getDate());
                continue;
            }

            // Construir mapa persona → lista de tareas para soft constraints
            personAssignments.computeIfAbsent(ta.getPerson().getName(), k -> new ArrayList<>()).add(ta);

            if (!ta.getTask().getAllowedCategories().contains(ta.getPerson().getCategory())) {
                System.out.println("[Person category mismatch] Persona: " + ta.getPerson().getName()
                        + ", Tarea: " + ta.getTask().getName() + ", Fecha: " + ta.getDate());
            }

            if (!ta.getPerson().getAvailableDays().contains(ta.getDate().getDayOfWeek())) {
                System.out.println("[Person not available] Persona: " + ta.getPerson().getName()
                        + ", Fecha: " + ta.getDate() + ", Tarea: " + ta.getTask().getName());
            }

            if (isBirthday(ta.getPerson().getBirthDate(), ta.getDate())) {
                System.out.println("[Person cannot work on birthday] Persona: " + ta.getPerson().getName()
                        + ", Fecha: " + ta.getDate() + ", Tarea: " + ta.getTask().getName());
            }

            if (!ta.getTask().getAssignedDays().contains(ta.getDate().getDayOfWeek())) {
                System.out.println("[Task scheduled on invalid day] Tarea: " + ta.getTask().getName()
                        + ", Fecha: " + ta.getDate() + ", Persona: " + ta.getPerson().getName());
            }
        }

        // Double booking
        solution.getPersonList().forEach(person -> {
            Map<java.time.LocalDate, List<TaskAssignment>> assignmentsByDate = solution.getTaskAssignmentList().stream()
                    .filter(ta -> person.equals(ta.getPerson()))
                    .collect(Collectors.groupingBy(TaskAssignment::getDate));

            assignmentsByDate.forEach((date, tas) -> {
                if (tas.size() > 1) {
                    System.out.println("[Person double booked] Persona: " + person.getName()
                            + ", Fecha: " + date + ", Tareas asignadas: " + tas.size());
                }
            });
        });

        // --- SOFT CONSTRAINTS ---
        personAssignments.forEach((personName, tas) -> {
            int taskCount = tas.size();

            // balanceWorkload: penaliza tareas > 3
            if (taskCount > 3) {
                System.out.println("[Soft Constraint - workload > 3] Persona: " + personName
                        + ", Tareas asignadas: " + taskCount
                        + " (penalización: " + (taskCount - 3) + ")");
            }

            // preferLessLoadedPerson: muestra cantidad de tareas total (penaliza según la
            // cantidad)
            System.out.println("[Soft Constraint - prefer less loaded] Persona: " + personName
                    + ", Tareas asignadas: " + taskCount);
        });

        System.out.println("=== End of violations ===");
    }

    /**
     * Helper para cumpleaños
     */
    private boolean isBirthday(java.time.LocalDate birthDate, java.time.LocalDate assignmentDate) {
        return birthDate != null &&
                assignmentDate != null &&
                birthDate.getMonth() == assignmentDate.getMonth() &&
                birthDate.getDayOfMonth() == assignmentDate.getDayOfMonth();
    }
}
