package com.AgsCh.task_scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import com.AgsCh.task_scheduler.model.FunctionRule;
import com.AgsCh.task_scheduler.model.RuleType;

public interface FunctionRuleRepository
        extends JpaRepository<FunctionRule, Long> {

    List<FunctionRule> findByHouseId(Long houseId);

    Optional<FunctionRule> findByIdAndHouse_Id(Long id, Long houseId);

    void deleteByIdAndHouse_Id(Long id, Long houseId);

    boolean existsByFunctionA_IdAndFunctionB_IdAndTypeAndHouse_Id(Long aId, Long bId, RuleType type, Long houseId);
}