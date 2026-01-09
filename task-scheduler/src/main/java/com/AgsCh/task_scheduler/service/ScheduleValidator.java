package com.AgsCh.task_scheduler.service;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.stereotype.Component;

import com.AgsCh.task_scheduler.exception.BusinessException;
import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.Schedule;
import com.AgsCh.task_scheduler.model.Task;

@Component
public class ScheduleValidator {

    public void validate(Schedule schedule) {

        if (schedule.getPersonList().isEmpty()) {
            throw new BusinessException("At least one person is required");
        }

        if (schedule.getTaskList().isEmpty()) {
            throw new BusinessException("At least one task is required");
        }

        for (Task task : schedule.getTaskList()) {
            if (task.getAssignedDays().isEmpty()) {
                throw new BusinessException(
                    "Task '" + task.getName() + "' has no assigned days"
                );
            }

            validateTaskHasQualifiedPerson(task, schedule.getPersonList());
            validateTaskHasAvailablePerson(task, schedule.getPersonList());
        }
    }

    private void validateTaskHasQualifiedPerson(
            Task task, List<Person> persons) {

        boolean exists = persons.stream()
            .anyMatch(p -> p.getCategory().equals(task.getCategory()));

        if (!exists) {
            throw new BusinessException(
                "No person with category '" + task.getCategory() +
                "' for task '" + task.getName() + "'"
            );
        }
    }

    private void validateTaskHasAvailablePerson(
            Task task, List<Person> persons) {

        for (DayOfWeek day : task.getAssignedDays()) {

            boolean someoneAvailable = persons.stream()
                .anyMatch(p ->
                    p.getCategory().equals(task.getCategory()) &&
                    p.getAvailableDays().contains(day)
                );

            if (!someoneAvailable) {
                throw new BusinessException(
                    "No available person for task '" + task.getName() +
                    "' on " + day
                );
            }
        }
    }
}
