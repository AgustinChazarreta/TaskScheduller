package com.AgsCh.task_scheduler;

//import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


//////////////////////////////////////////////////////////////
import java.time.Duration;

import org.optaplanner.core.api.solver.SolverFactory;

import com.AgsCh.task_scheduler.model.*;
import com.AgsCh.task_scheduler.planner.ScheduleConstraintProvider;
import com.AgsCh.task_scheduler.util.TestData;
///////////////////////////////////////////////////////////////


@SpringBootApplication
public class TaskSchedulerApplication {
/*
	public static void main(String[] args) {
		SpringApplication.run(TaskSchedulerApplication.class, args);
	}
*/

	public static void main(String[] args) {

        Schedule problem = TestData.buildSchedule();

        SolverFactory<Schedule> solverFactory =
            SolverFactory.create(
                new org.optaplanner.core.config.solver.SolverConfig()
                    .withSolutionClass(Schedule.class)
                    .withEntityClasses(TaskAssignment.class)
                    .withConstraintProviderClass(ScheduleConstraintProvider.class)
                    .withTerminationSpentLimit(Duration.ofSeconds(2))
            );

        Schedule solution = solverFactory.buildSolver().solve(problem);

        System.out.println("=== RESULTADO ===");
        solution.getTaskAssignmentList().forEach(a -> System.out.println(
            a.getDay() + " || " + a.getTask().getName() + " -> " + a.getPerson().getName())
		);

        System.out.println("SCORE: " + solution.getScore());
    }

}
