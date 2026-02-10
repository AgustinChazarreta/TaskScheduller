package com.AgsCh.task_scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgsCh.task_scheduler.model.Person;
import com.AgsCh.task_scheduler.model.PersonFunction;

public interface PersonFunctionRepository
        extends JpaRepository<PersonFunction, Long> {

    void deleteByPerson(Person person);
}
