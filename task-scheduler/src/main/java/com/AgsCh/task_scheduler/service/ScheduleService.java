package com.AgsCh.task_scheduler.service;

import java.time.Duration;
import java.util.Objects;

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

        // validaciones extra
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
}
