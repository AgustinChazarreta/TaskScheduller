package com.AgsCh.task_scheduler.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgsCh.task_scheduler.model.WordTemplate;

public interface WordTemplateRepository
        extends JpaRepository<WordTemplate, Long> {

    Optional<WordTemplate> findByHouseId(Long houseId);
}