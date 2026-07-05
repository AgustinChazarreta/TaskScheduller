package com.AgsCh.task_scheduler.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.AgsCh.task_scheduler.model.Function;

public interface FunctionRepository extends JpaRepository<Function, Long> {
    List<Function> findByHouseId(Long houseId);

    List<Function> findByHouseIdAndActiveTrue(Long id);

    Optional<Function> findByIdAndActiveTrue(Long id);

    List<Function> findByHouseIdAndIdIn(Long houseId, Iterable<Long> ids);
}
