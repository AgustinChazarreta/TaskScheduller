package com.AgsCh.task_scheduler.planner;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

import java.time.LocalDate;

import com.AgsCh.task_scheduler.model.FunctionAssignment;

public class ScheduleConstraintProvider implements ConstraintProvider {

        @Override
        public Constraint[] defineConstraints(ConstraintFactory factory) {
                return new Constraint[] {

                                // =========================
                                // HARD CONSTRAINTS
                                // =========================
                                functionMustHavePerson(factory),
                                personMustBeAbleToPerformFunction(factory),
                                personMustWorkThatDay(factory),
                                personMustBeAvailable(factory),
                                functionMustBeScheduledOnAllowedDay(factory),
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

        private Constraint functionMustHavePerson(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() == null)
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Function must have an assigned person");
        }

        private Constraint personMustBeAbleToPerformFunction(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() != null && fa.getFunction() != null)
                                .filter(fa -> fa.getPerson()
                                                .getPersonFunctions()
                                                .stream()
                                                .noneMatch(pf -> pf.getFunction().equals(fa.getFunction())))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person must be able to perform function");
        }

        private Constraint personMustWorkThatDay(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() != null && fa.getDate() != null)
                                .filter(fa -> !fa.getPerson()
                                                .getWorkingDays()
                                                .contains(fa.getDate().getDayOfWeek()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person must work on that day of week");
        }

        private Constraint personMustBeAvailable(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() != null && fa.getDate() != null)
                                .filter(fa -> fa.getPerson()
                                                .getUnavailabilities()
                                                .stream()
                                                .anyMatch(u -> u.includes(fa.getDate())))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person must be available (no unavailability)");
        }

        private Constraint functionMustBeScheduledOnAllowedDay(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getFunction() != null && fa.getDate() != null)
                                .filter(fa -> !fa.getFunction()
                                                .getAssignedDays()
                                                .contains(fa.getDate().getDayOfWeek()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Function must be scheduled on an allowed day");
        }

        private Constraint noDoubleBooking(ConstraintFactory factory) {
                return factory.forEachUniquePair(
                                FunctionAssignment.class,
                                Joiners.equal(FunctionAssignment::getPerson),
                                Joiners.equal(FunctionAssignment::getDate))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person cannot be assigned to multiple functions on the same day");
        }

        private Constraint personCannotWorkOnBirthday(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() != null && fa.getDate() != null)
                                .filter(fa -> isBirthday(
                                                fa.getPerson().getBirthDate(),
                                                fa.getDate()))
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
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() != null)
                                .groupBy(
                                                FunctionAssignment::getPerson,
                                                ConstraintCollectors.count())
                                .filter((person, count) -> count > 3)
                                .penalize(
                                                HardSoftScore.ONE_SOFT,
                                                (person, count) -> count - 3)
                                .asConstraint("Balance workload");
        }
}
