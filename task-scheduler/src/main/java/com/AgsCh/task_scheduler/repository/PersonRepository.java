package com.AgsCh.task_scheduler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgsCh.task_scheduler.model.House;
import com.AgsCh.task_scheduler.model.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByHouse(House house);
}
