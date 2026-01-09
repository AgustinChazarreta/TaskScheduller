package com.AgsCh.task_scheduler.service;

import java.time.Duration;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.TaskAssignment;
import com.AgsCh.task_scheduler.planner.ScheduleConstraintProvider;

@Service
public class ScheduleService {

    private final SolverFactory<Schedule> solverFactory;

    public ScheduleService() {

        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(Schedule.class)
                .withEntityClasses(TaskAssignment.class)
                .withConstraintProviderClass(ScheduleConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(2));

        this.solverFactory = SolverFactory.create(solverConfig);
    }

    public Schedule solve(Schedule problem) {

        if (problem.getPersonList() == null || problem.getPersonList().isEmpty()) {
            throw new BusinessException("Persons list is empty");
        }

        if (problem.getTaskList() == null || problem.getTaskList().isEmpty()) {
            throw new BusinessException("Tasks list is empty");
        }

        if (problem.getTaskAssignmentList() == null || problem.getTaskAssignmentList().isEmpty()) {
            throw new BusinessException("Task assignments list is empty");
        }

        Solver<Schedule> solver = solverFactory.buildSolver();
        return solver.solve(problem);
    }
}
