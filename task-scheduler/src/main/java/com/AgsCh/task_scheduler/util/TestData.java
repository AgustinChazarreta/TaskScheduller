package com.AgsCh.task_scheduler.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.AgsCh.task_scheduler.model.*;

public class TestData {

    public static Schedule buildSchedule() {

        /* ========= PERSONAS ========= */

        Person alice = new Person(
            "Alice",
            Category.CATEGORY_1,
            LocalDate.of(1995, 5, 10),
            Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
        );

        Person bob = new Person(
            "Bob",
            Category.CATEGORY_2,
            LocalDate.of(1990, 8, 20),
            Set.of(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY)
        );

        Person charlie = new Person(
            "Charlie",
            Category.CATEGORY_3,
            LocalDate.of(1988, 3, 3),
            Set.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        );

        List<Person> persons = List.of(alice, bob, charlie);

        /* ========= TASKS ========= */

        Task backend = new Task("Backend", Set.of(Category.CATEGORY_1), Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY));
        Task database = new Task("Database", Set.of(Category.CATEGORY_2), Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY));
        Task deploy = new Task("Deploy", Set.of(Category.CATEGORY_3), Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        List<Task> tasks = List.of(backend, database, deploy);

        /* ========= ASSIGNMENTS (mal armados a propósito) ========= */
        
        List<TaskAssignment> assignments = new ArrayList<>();

        for (Task task : tasks) {
            for (DayOfWeek day : task.getAssignedDays()) {
                assignments.add(new TaskAssignment(task, null));
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
