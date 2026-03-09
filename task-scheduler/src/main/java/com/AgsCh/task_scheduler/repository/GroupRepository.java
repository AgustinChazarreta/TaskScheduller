package com.AgsCh.task_scheduler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgsCh.task_scheduler.model.Group;
import com.AgsCh.task_scheduler.model.House;

public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByHouse(House house);

}