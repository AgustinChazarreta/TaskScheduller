package com.AgsCh.task_scheduler.planner;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

import java.time.LocalDate;

import com.AgsCh.task_scheduler.model.FunctionAssignment;
import com.AgsCh.task_scheduler.model.Person;

public class ScheduleConstraintProvider implements ConstraintProvider {

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

                                // =========================
                                // SOFT CONSTRAINTS
                                // =========================
                                balanceWorkload(factory)
                };
        }

        // =========================
        // HARD CONSTRAINTS
        // =========================

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

        /**
         * Nueva constraint centralizada que reemplaza personMustWorkThatDay +
         * personMustBeAvailable
         */
        private Constraint personMustBeAssignable(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() != null && fa.getDate() != null)
                                .filter(fa -> !isPersonAssignable(fa.getPerson(), fa.getDate()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person must be assignable (working day + unavailability + active + entry/exit)");
        }

        private boolean isPersonAssignable(Person person, LocalDate date) {
                if (person == null || date == null) {
                        return true; // no penalizamos null
                }

                // 1️⃣ Día de la semana
                if (!person.worksOn(date.getDayOfWeek())) {
                        return false;
                }

                // 2️⃣ Indisponibilidades puntuales
                boolean unavailable = person.getUnavailabilities()
                                .stream()
                                .anyMatch(u -> u.includes(date));
                if (unavailable) {
                        return false;
                }

                // 3️⃣ Activo
                if (!person.isActive()) {
                        return false;
                }

                // 4️⃣ Entry / Exit
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
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Function must be scheduled on an allowed day");
        }

        private Constraint noDoubleBooking(ConstraintFactory factory) {
                return factory.forEachUniquePair(FunctionAssignment.class,
                                Joiners.equal(FunctionAssignment::getPerson),
                                Joiners.equal(FunctionAssignment::getDate))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person cannot be assigned to multiple functions on the same day");
        }

        private Constraint personCannotWorkOnBirthday(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() != null && fa.getDate() != null)
                                .filter(fa -> isBirthday(fa.getPerson().getBirthDate(), fa.getDate()))
                                .penalize(HardSoftScore.ONE_HARD)
                                .asConstraint("Person cannot work on their birthday");
        }

        private boolean isBirthday(LocalDate birthDate, LocalDate assignmentDate) {
                return birthDate != null
                                && assignmentDate != null
                                && birthDate.getMonth() == assignmentDate.getMonth()
                                && birthDate.getDayOfMonth() == assignmentDate.getDayOfMonth();
        }

        // =========================
        // SOFT CONSTRAINTS
        // =========================

        private Constraint balanceWorkload(ConstraintFactory factory) {
                return factory.forEach(FunctionAssignment.class)
                                .filter(fa -> fa.getPerson() != null)
                                .groupBy(FunctionAssignment::getPerson,
                                                ConstraintCollectors.count())
                                .filter((person, count) -> count > 3)
                                .penalize(HardSoftScore.ONE_SOFT, (person, count) -> count - 3)
                                .asConstraint("Balance workload");
        }
}