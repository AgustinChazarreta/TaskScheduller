package com.AgsCh.task_scheduler.planner;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.*;

import java.time.LocalDate;
import java.time.YearMonth;

import com.AgsCh.task_scheduler.model.FunctionAssignment;
import com.AgsCh.task_scheduler.model.FunctionRule;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.RuleType;

public class ScheduleConstraintProvider implements ConstraintProvider {

        private static final int ROTATION_WEIGHT = 20;
        private static final int CONSECUTIVE_WEIGHT = 5;

        @Override
        public Constraint[] defineConstraints(ConstraintFactory factory) {
                return new Constraint[] {

                                // =========================
                                // HARD CONSTRAINTS
                                // =========================
                                functionMustHavePerson(factory),
                                personMustBeAbleToPerformFunction(factory),
                                personMustBeAssignable(factory),
                                functionMustBeScheduledOnAllowedDay(factory),
                                noDoubleBooking(factory),
                                personCannotWorkOnBirthday(factory),

                                incompatibleFunctionsSameDay(factory), // 👈 NUEVA

                                // =========================
                                // MEDIUM CONSTRAINTS
                                // =========================
                                rotateSharedFunctionsMonthly(factory),

                                // =========================
                                // SOFT CONSTRAINTS
                                // =========================
                                avoidConsecutiveAssignments(factory),
                                softIncompatibleFunctionsSameDay(factory) // 👈 NUEVA
                };
        }

        // =========================
        // HARD CONSTRAINTS
        // =========================

        private Constraint functionMustHavePerson(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() == null)
                                .penalize(HardMediumSoftScore.ONE_HARD)
                                .asConstraint("Function must have an assigned person");
        }

        private Constraint personMustBeAbleToPerformFunction(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() != null && fa.getFunction() != null)
                                .filter(fa -> fa.getPerson()
                                                .getPersonFunctions()
                                                .stream()
                                                .noneMatch(pf -> pf.getFunction().equals(fa.getFunction())))
                                .penalize(HardMediumSoftScore.ONE_HARD)
                                .asConstraint("Person must be able to perform function");
        }

        private Constraint personMustBeAssignable(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() != null && fa.getDate() != null)
                                .filter(fa -> !isPersonAssignable(fa.getPerson(), fa.getDate()))
                                .penalize(HardMediumSoftScore.ONE_HARD)
                                .asConstraint("Person must be assignable");
        }

        private boolean isPersonAssignable(Person person, LocalDate date) {

                if (person == null || date == null) {
                        return true;
                }

                if (!person.worksOn(date.getDayOfWeek())) {
                        return false;
                }

                boolean unavailable = person.getUnavailabilities()
                                .stream()
                                .anyMatch(u -> u.includes(date));

                if (unavailable) {
                        return false;
                }

                if (!person.isActive()) {
                        return false;
                }

                if (person.getEntryDate() != null && date.isBefore(person.getEntryDate())) {
                        return false;
                }

                if (person.getExitDate() != null && date.isAfter(person.getExitDate())) {
                        return false;
                }

                return true;
        }

        private Constraint functionMustBeScheduledOnAllowedDay(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getFunction() != null && fa.getDate() != null)
                                .filter(fa -> !fa.getFunction()
                                                .getAssignedDays()
                                                .contains(fa.getDate().getDayOfWeek()))
                                .penalize(HardMediumSoftScore.ONE_HARD)
                                .asConstraint("Function must be scheduled on allowed day");
        }

        private Constraint noDoubleBooking(ConstraintFactory factory) {
                return factory.forEachUniquePair(FunctionAssignment.class,
                                Joiners.equal(FunctionAssignment::getPerson),
                                Joiners.equal(FunctionAssignment::getDate))
                                .penalize(HardMediumSoftScore.ONE_HARD)
                                .asConstraint("No double booking per day");
        }

        private Constraint personCannotWorkOnBirthday(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() != null && fa.getDate() != null)
                                .filter(fa -> isBirthday(fa.getPerson().getBirthDate(), fa.getDate()))
                                .penalize(HardMediumSoftScore.ONE_HARD)
                                .asConstraint("Person cannot work on birthday");
        }

        private boolean isBirthday(LocalDate birthDate, LocalDate assignmentDate) {
                return birthDate != null
                                && assignmentDate != null
                                && birthDate.getMonth() == assignmentDate.getMonth()
                                && birthDate.getDayOfMonth() == assignmentDate.getDayOfMonth();
        }

        private Constraint incompatibleFunctionsSameDay(ConstraintFactory factory) {
                return factory
                                .forEachUniquePair(FunctionAssignment.class,
                                                Joiners.equal(FunctionAssignment::getPerson),
                                                Joiners.equal(FunctionAssignment::getDate))
                                .filter((a1, a2) -> a1.getFunction() != null &&
                                                a2.getFunction() != null)
                                .join(FunctionRule.class,
                                                Joiners.filtering((a1, a2,
                                                                rule) -> rule.getType() == RuleType.INCOMPATIBLE &&
                                                                                rule.matches(a1.getFunction(),
                                                                                                a2.getFunction())))
                                .penalize(HardMediumSoftScore.ONE_HARD)
                                .asConstraint("Incompatible functions same day");
        }
        // =========================
        // MEDIUM CONSTRAINTS
        // =========================

        /**
         * Rota funciones compartidas dentro del mismo mes.
         * No penaliza funciones que solo puede hacer una persona.
         */
        private Constraint rotateSharedFunctionsMonthly(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() != null)
                                // Solo funciones que tienen más de un candidato posible
                                .filter(fa -> fa.getFunction()
                                                .getPersonFunctions()
                                                .size() > 1)
                                .groupBy(
                                                fa -> fa.getPerson(),
                                                fa -> fa.getFunction(),
                                                fa -> YearMonth.from(fa.getDate()),
                                                ConstraintCollectors.count())
                                .filter((person, function, month, count) -> count > 1)
                                .penalize(
                                                HardMediumSoftScore.ofMedium(ROTATION_WEIGHT),
                                                (person, function, month, count) -> (count - 1) * (count - 1))
                                .asConstraint("Rotate shared functions monthly");
        }

        // =========================
        // SOFT CONSTRAINTS
        // =========================

        private Constraint avoidConsecutiveAssignments(ConstraintFactory factory) {
                return factory.forEachUniquePair(FunctionAssignment.class,
                                Joiners.equal(FunctionAssignment::getPerson))
                                .filter((a, b) -> a.getDate() != null &&
                                                b.getDate() != null &&
                                                Math.abs(a.getDate().toEpochDay()
                                                                - b.getDate().toEpochDay()) == 1)
                                .penalize(HardMediumSoftScore.ofSoft(CONSECUTIVE_WEIGHT))
                                .asConstraint("Avoid consecutive assignments");
        }

        private Constraint softIncompatibleFunctionsSameDay(ConstraintFactory factory) {
                return factory
                                .forEachUniquePair(FunctionAssignment.class,
                                                Joiners.equal(FunctionAssignment::getPerson),
                                                Joiners.equal(FunctionAssignment::getDate))
                                .filter((a1, a2) -> a1.getFunction() != null &&
                                                a2.getFunction() != null)
                                .join(FunctionRule.class,
                                                Joiners.filtering((a1, a2,
                                                                rule) -> rule.getType() == RuleType.SOFT_INCOMPATIBLE &&
                                                                                rule.matches(a1.getFunction(),
                                                                                                a2.getFunction())))
                                .penalize(HardMediumSoftScore.ONE_SOFT)
                                .asConstraint("Soft incompatible functions same day");
        }
}