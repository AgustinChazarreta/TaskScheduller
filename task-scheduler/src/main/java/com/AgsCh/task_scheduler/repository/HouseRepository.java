package com.AgsCh.task_scheduler.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.AgsCh.task_scheduler.model.House;

public interface HouseRepository extends JpaRepository<House, Long> {
    Optional<House> findByName(String name);
}
