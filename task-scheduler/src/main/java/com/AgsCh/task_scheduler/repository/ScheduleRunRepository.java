package com.AgsCh.task_scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.AgsCh.task_scheduler.model.ScheduleRun;

import jakarta.transaction.Transactional;

public interface ScheduleRunRepository extends JpaRepository<ScheduleRun, Long> {

    @Modifying
    @Transactional
    @Query("""
        update ScheduleRun r
        set r.status = :archived
        where r.status = :active
    """)
    void archiveActiveRun(
        @Param("archived") ScheduleRun.Status archived,
        @Param("active") ScheduleRun.Status active
    );
}
