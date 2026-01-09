package com.AgsCh.task_scheduler.service;

import java.time.Duration;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.TaskAssignment;
import com.AgsCh.task_scheduler.planner.ScheduleConstraintProvider;

@Service
public class ScheduleService {

    private final SolverFactory<Schedule> solverFactory;
    private final ScheduleValidator validator;

    public ScheduleService(ScheduleValidator validator) {

        this.validator = validator;

        SolverConfig solverConfig = new SolverConfig()
            .withSolutionClass(Schedule.class)
            .withEntityClasses(TaskAssignment.class)
            .withConstraintProviderClass(ScheduleConstraintProvider.class)
            .withTerminationSpentLimit(Duration.ofSeconds(2));

        this.solverFactory = SolverFactory.create(solverConfig);
    }

    public Schedule solve(Schedule problem) {

        // 🔒 VALIDACIONES DE NEGOCIO (antes del solver)
        validator.validate(problem);

        // 🧠 RESOLUCIÓN
        Solver<Schedule> solver = solverFactory.buildSolver();
        return solver.solve(problem);
    }
}
