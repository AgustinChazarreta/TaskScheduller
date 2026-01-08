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
            noDoubleBooking(factory)
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
}

