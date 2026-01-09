package com.AgsCh.task_scheduler.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.AgsCh.task_scheduler.model.*;

public class TestData {

    public static Schedule buildSchedule() {

        /* ========= PERSONAS ========= */

        Person alice = new Person(
            "Alice",
            "JAVA",
            LocalDate.of(1995, 5, 10),
            List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
        );

        Person bob = new Person(
            "Bob",
            "SQL",
            LocalDate.of(1990, 8, 20),
            List.of(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY)
        );

        Person charlie = new Person(
            "Charlie",
            "DEVOPS",
            LocalDate.of(1988, 3, 3),
            List.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        );

        List<Person> persons = List.of(alice, bob, charlie);

        /* ========= TASKS ========= */

        Task backend = new Task("Backend", "JAVA", List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY));
        Task database = new Task("Database", "SQL", List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY));
        Task deploy = new Task("Deploy", "DEVOPS", List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        List<Task> tasks = List.of(backend, database, deploy);

        /* ========= ASSIGNMENTS (mal armados a propósito) ========= */
        
        List<TaskAssignment> assignments = new ArrayList<>();

        for (Task task : tasks) {
            for (DayOfWeek day : task.getAssignedDays()) {
                assignments.add(new TaskAssignment(task, day));
            }
        }

        /* ========= SCHEDULE ========= */

        return new Schedule(
            persons,
            tasks,
            assignments
        );
    }
}
