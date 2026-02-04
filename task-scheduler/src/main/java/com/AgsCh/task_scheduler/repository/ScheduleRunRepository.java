package com.AgsCh.task_scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.AgsCh.task_scheduler.model.ScheduleRun;

import jakarta.transaction.Transactional;

public interface ScheduleRunRepository extends JpaRepository<ScheduleRun, Long> {

    @Modifying
    @Transactional
    @Query("""
        update ScheduleRun r
        set r.status = com.AgsCh.task_scheduler.model.ScheduleRun.Status.ARCHIVED
        where r.status = com.AgsCh.task_scheduler.model.ScheduleRun.Status.ACTIVE
    """)
    void archiveActiveRun();
}
