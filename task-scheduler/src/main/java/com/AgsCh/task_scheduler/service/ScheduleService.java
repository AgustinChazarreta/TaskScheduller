package com.AgsCh.task_scheduler.service;

import java.time.Duration;
import java.util.Collection;

import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolutionManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.TaskAssignment;
import com.AgsCh.task_scheduler.planner.ScheduleConstraintProvider;

@Service
public class ScheduleService {

    private final SolverFactory<Schedule> solverFactory;
    private final SolutionManager<Schedule, HardSoftScore> solutionManager;

    public ScheduleService() {

        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(Schedule.class)
                .withEntityClasses(TaskAssignment.class)
                .withConstraintProviderClass(ScheduleConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(2));

        this.solverFactory = SolverFactory.create(solverConfig);
        this.solutionManager = SolutionManager.create(solverFactory);
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
        Schedule solution = solver.solve(problem);




        System.out.println("==== DEBUG SOLUTION ASSIGNMENTS ====");
        for (TaskAssignment a : solution.getTaskAssignmentList()) {
            System.out.println(
                    "task=" + a.getTask().getName() +
                            " | day=" + a.getDate().getDayOfWeek() +
                            " | date=" + a.getDate() +
                            " | person=" + (a.getPerson() != null ? a.getPerson().getName() : "null"));
        }
        System.out.println("===================================");





        explainConstraints(solution);

        return solution;
    }

    private void explainConstraints(Schedule solution) {

        ScoreExplanation<Schedule, HardSoftScore> explanation = solutionManager.explain(solution);

        Collection<ConstraintMatchTotal<HardSoftScore>> totals = explanation.getConstraintMatchTotalMap().values();

        totals.stream()
                .filter(total -> !total.getScore().isZero())
                .forEach(total -> System.out.println(
                        total.getConstraintName() + " → " + total.getScore()));
    }
}
