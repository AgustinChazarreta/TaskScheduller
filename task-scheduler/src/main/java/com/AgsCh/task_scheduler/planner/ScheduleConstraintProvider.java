package com.AgsCh.task_scheduler.planner;

import org.optaplanner.core.api.score.stream.*;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.time.LocalDate;

import com.AgsCh.task_scheduler.model.*;

public class ScheduleConstraintProvider implements ConstraintProvider {

        @Override
        public Constraint[] defineConstraints(ConstraintFactory factory) {
                return new Constraint[] {
                                // Hard constraints
                                personMustMatchTaskCategory(factory),
                                personMustBeAvailable(factory),
                                noDoubleBooking(factory),
                                taskMustBeScheduledOnAllowedDay(factory),
                                taskMustHavePerson(factory),
                                personCannotWorkOnBirthday(factory),

                                // Soft constraints
                                balanceWorkload(factory),
                                preferLessLoadedPerson(factory)
                };
        }

        /*
         * =========================
         * HARD CONSTRAINTS
         * =========================
         */

        private Constraint personMustMatchTaskCategory(ConstraintFactory factory) {
                return factory.forEach(TaskAssignment.class)
                                .filter(ta -> ta.getPerson() != null &&
                                                ta.getTask() != null &&
                                                !ta.getTask()
                                                                .getAllowedCategories()
                                                                .contains(ta.getPerson().getCategory()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person category mismatch");
        }

        private Constraint personMustBeAvailable(ConstraintFactory factory) {
                return factory.forEach(TaskAssignment.class)
                                .filter(ta -> ta.getPerson() != null &&
                                                ta.getDate() != null &&
                                                !ta.getPerson()
                                                                .getAvailableDays()
                                                                .contains(ta.getDate().getDayOfWeek()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person not available that day");
        }

        private Constraint noDoubleBooking(ConstraintFactory factory) {
                return factory.forEachUniquePair(
                                TaskAssignment.class,
                                Joiners.equal(TaskAssignment::getPerson),
                                Joiners.equal(TaskAssignment::getDate))
                                .filter((ta1, ta2) -> ta1.getPerson() != null)
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person double booked on same day");
        }

        private Constraint taskMustHavePerson(ConstraintFactory factory) {
                return factory.forEach(TaskAssignment.class)
                                .filter(ta -> ta.getPerson() == null)
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Task has no assigned person");
        }

        private Constraint taskMustBeScheduledOnAllowedDay(ConstraintFactory factory) {
                return factory.forEach(TaskAssignment.class)
                                .filter(ta -> ta.getTask() != null &&
                                                ta.getDate() != null &&
                                                !ta.getTask()
                                                                .getAssignedDays()
                                                                .contains(ta.getDate().getDayOfWeek()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Task scheduled on invalid day");
        }

        private Constraint personCannotWorkOnBirthday(ConstraintFactory factory) {
                return factory.forEach(TaskAssignment.class)
                                .filter(ta -> ta.getPerson() != null &&
                                                ta.getDate() != null &&
                                                isBirthday(
                                                                ta.getPerson().getBirthDate(),
                                                                ta.getDate()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person cannot work on their birthday");
        }

        private boolean isBirthday(LocalDate birthDate, LocalDate assignmentDate) {
                return birthDate != null &&
                                assignmentDate != null &&
                                birthDate.getMonth() == assignmentDate.getMonth() &&
                                birthDate.getDayOfMonth() == assignmentDate.getDayOfMonth();
        }

        /*
         * =========================
         * SOFT CONSTRAINTS
         * =========================
         */

        private Constraint balanceWorkload(ConstraintFactory factory) {
                return factory.forEach(TaskAssignment.class)
                                .filter(ta -> ta.getPerson() != null)
                                .groupBy(TaskAssignment::getPerson, ConstraintCollectors.count())
                                .filter((person, taskCount) -> taskCount > 3)
                                .penalize(
                                                HardSoftScore.ONE_SOFT,
                                                (person, taskCount) -> taskCount - 3)
                                .asConstraint("Penalize workload over 3 tasks per person");
        }

        private Constraint preferLessLoadedPerson(ConstraintFactory factory) {
                return factory.forEach(TaskAssignment.class)
                                .filter(ta -> ta.getPerson() != null)
                                .groupBy(TaskAssignment::getPerson, ConstraintCollectors.count())
                                .penalize(
                                                HardSoftScore.ONE_SOFT,
                                                (person, count) -> count)
                                .asConstraint("Prefer less loaded person");
        }

}
