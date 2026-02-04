package com.AgsCh.task_scheduler.service.solver;

import java.time.Duration;
import java.util.Objects;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.FunctionAssignment;
import com.AgsCh.task_scheduler.planner.ScheduleConstraintProvider;

@Service
public class ScheduleService {

    private final SolverFactory<Schedule> solverFactory;

    public ScheduleService(
            @Value("${solver.timeLimitSeconds:2}") long timeLimitSeconds) {

        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(Schedule.class)
                .withEntityClasses(FunctionAssignment.class)
                .withConstraintProviderClass(ScheduleConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(timeLimitSeconds));

        this.solverFactory = SolverFactory.create(solverConfig);
    }

    /**
     * Resuelve un Schedule usando OptaPlanner
     */
    public Schedule solve(Schedule problem) {
        validateProblem(problem);
        Solver<Schedule> solver = solverFactory.buildSolver();
        return solver.solve(problem);
    }

    /**
     * Validaciones básicas y consistencia de datos
     */
    private void validateProblem(Schedule problem) {

        if (problem == null) {
            throw new BusinessException("Schedule is null");
        }

        if (problem.getPersonList() == null || problem.getPersonList().isEmpty()) {
            throw new BusinessException("Persons list is empty");
        }

        if (problem.getFunctionList() == null || problem.getFunctionList().isEmpty()) {
            throw new BusinessException("Functions list is empty");
        }

        if (problem.getFunctionAssignmentList() == null || problem.getFunctionAssignmentList().isEmpty()) {
            throw new BusinessException("Function assignments list is empty");
        }

        problem.getFunctionAssignmentList().forEach(a -> {
            if (Objects.isNull(a.getFunction())) {
                throw new BusinessException("FunctionAssignment sin Function asignada");
            }
            if (Objects.isNull(a.getDate())) {
                throw new BusinessException("FunctionAssignment sin fecha asignada");
            }
        });
    }
}
