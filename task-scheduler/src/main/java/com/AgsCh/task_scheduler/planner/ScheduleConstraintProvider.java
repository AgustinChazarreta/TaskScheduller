package com.AgsCh.task_scheduler.planner;

import org.optaplanner.core.api.score.stream.*;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import com.AgsCh.task_scheduler.model.*;

public class ScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
            // Hard constraints
            personMustBeAvailable(factory),
            personMustMatchTaskCategory(factory),
            noDoubleBooking(factory),
            taskMustBeScheduledOnAllowedDay(factory),
            taskMustHavePerson(factory),
            // Soft constraints
            balanceWorkload(factory),
            preferSamePersonForSameTask(factory)
        };
    }

    private Constraint personMustBeAvailable(ConstraintFactory factory) {
        return factory.forEach(TaskAssignment.class)
            .filter(ta ->
                ta.getPerson() != null &&
                ta.getDay() != null &&
                !ta.getPerson()
                    .getAvailableDays()
                    .contains(ta.getDay())
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Person not available that day");
    }

    private Constraint personMustMatchTaskCategory(ConstraintFactory factory) {
        return factory.forEach(TaskAssignment.class)
            .filter(ta ->
                ta.getPerson() != null &&
                ta.getTask() != null &&
                !ta.getPerson()
                    .getCategory()
                    .equals(ta.getTask().getCategory())
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Person category mismatch");
    }

    private Constraint noDoubleBooking(ConstraintFactory factory) {
        return factory.forEachUniquePair(
                TaskAssignment.class,
                Joiners.equal(TaskAssignment::getPerson),
                Joiners.equal(TaskAssignment::getDay)
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Person double booked");
    }

    private Constraint taskMustBeScheduledOnAllowedDay(ConstraintFactory factory) {
        return factory.forEach(TaskAssignment.class)
            .filter(ta ->
                ta.getTask() != null &&
                ta.getDay() != null &&
                !ta.getTask()
                    .getAssignedDays()
                    .contains(ta.getDay())
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Task scheduled on invalid day");
    }

    private Constraint taskMustHavePerson(ConstraintFactory factory) {
        return factory.forEach(TaskAssignment.class)
            .filter(ta -> ta.getPerson() == null)
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Task has no assigned person");
    }

    private Constraint balanceWorkload(ConstraintFactory factory) {
        return factory.forEach(TaskAssignment.class)
            .filter(ta -> ta.getPerson() != null)
            .groupBy(TaskAssignment::getPerson, ConstraintCollectors.count())
            .penalize(
                HardSoftScore.ONE_SOFT,
                (person, taskCount) -> taskCount * taskCount
            )
            .asConstraint("Balance workload between persons");
    }

    private Constraint preferSamePersonForSameTask(ConstraintFactory factory) {
        return factory.forEachUniquePair(
                TaskAssignment.class,
                Joiners.equal(TaskAssignment::getTask)
            )
            .filter((ta1, ta2) ->
                ta1.getPerson() != null &&
                ta2.getPerson() != null &&
                !ta1.getPerson().getId().equals(ta2.getPerson().getId())
            )
            .penalize(HardSoftScore.ONE_SOFT)
            .asConstraint("Prefer same person for same task");
    }   
}

