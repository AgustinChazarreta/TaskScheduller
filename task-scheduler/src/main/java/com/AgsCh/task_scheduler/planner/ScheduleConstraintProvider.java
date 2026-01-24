package com.AgsCh.task_scheduler.planner;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

import java.time.LocalDate;

import com.AgsCh.task_scheduler.model.TaskAssignment;

public class ScheduleConstraintProvider implements ConstraintProvider {

        @Override
        public Constraint[] defineConstraints(ConstraintFactory factory) {
                return new Constraint[] {

                                // =========================
                                // HARD CONSTRAINTS
                                // =========================
                                taskMustHavePerson(factory),
                                personMustMatchTaskCategory(factory),
                                personMustBeAvailable(factory),
                                taskMustBeScheduledOnAllowedDay(factory),
                                noDoubleBooking(factory),
                                personCannotWorkOnBirthday(factory),

                                // =========================
                                // SOFT CONSTRAINTS
                                // =========================
                                balanceWorkload(factory)
                };
        }

        /*
         * =========================
         * HARD CONSTRAINTS
         * =========================
         */

        private Constraint taskMustHavePerson(ConstraintFactory factory) {
                return factory.forEach(TaskAssignment.class)
                                .filter(ta -> ta.getPerson() == null)
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Task must have an assigned person");
        }

        private Constraint personMustMatchTaskCategory(ConstraintFactory factory) {
                return factory.forEach(TaskAssignment.class)
                                .filter(ta -> ta.getPerson() != null && ta.getTask() != null)
                                .filter(ta -> !ta.getTask()
                                                .getAllowedCategories()
                                                .contains(ta.getPerson().getCategory()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person category must match task category");
        }

        private Constraint personMustBeAvailable(ConstraintFactory factory) {
                return factory.forEach(TaskAssignment.class)
                                .filter(ta -> ta.getPerson() != null && ta.getDate() != null)
                                .filter(ta -> !ta.getPerson()
                                                .getAvailableDays()
                                                .contains(ta.getDate().getDayOfWeek()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person must be available on assigned day");
        }

        private Constraint taskMustBeScheduledOnAllowedDay(ConstraintFactory factory) {
                return factory.forEach(TaskAssignment.class)
                                .filter(ta -> ta.getTask() != null && ta.getDate() != null)
                                .filter(ta -> !ta.getTask()
                                                .getAssignedDays()
                                                .contains(ta.getDate().getDayOfWeek()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Task must be scheduled on an allowed day");
        }

        private Constraint noDoubleBooking(ConstraintFactory factory) {
                return factory.forEachUniquePair(
                                TaskAssignment.class,
                                Joiners.equal(TaskAssignment::getPerson),
                                Joiners.equal(TaskAssignment::getDate))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person cannot be assigned to multiple tasks on the same day");
        }

        private Constraint personCannotWorkOnBirthday(ConstraintFactory factory) {
                return factory.forEach(TaskAssignment.class)
                                .filter(ta -> ta.getPerson() != null && ta.getDate() != null)
                                .filter(ta -> isBirthday(
                                                ta.getPerson().getBirthDate(),
                                                ta.getDate()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person cannot work on their birthday");
        }

        private boolean isBirthday(LocalDate birthDate, LocalDate assignmentDate) {
                return birthDate != null
                                && assignmentDate != null
                                && birthDate.getMonth() == assignmentDate.getMonth()
                                && birthDate.getDayOfMonth() == assignmentDate.getDayOfMonth();
        }

        /*
         * =========================
         * SOFT CONSTRAINTS
         * =========================
         */

        private Constraint balanceWorkload(ConstraintFactory factory) {
                return factory.forEach(TaskAssignment.class)
                                .filter(ta -> ta.getPerson() != null)
                                .groupBy(
                                                TaskAssignment::getPerson,
                                                ConstraintCollectors.count())
                                .filter((person, taskCount) -> taskCount > 3)
                                .penalize(
                                                HardSoftScore.ONE_SOFT,
                                                (person, taskCount) -> taskCount - 3)
                                .asConstraint("Balance workload (more than 3 tasks per person)");
        }
}
