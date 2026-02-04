package com.AgsCh.task_scheduler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgsCh.task_scheduler.model.FunctionAssignment;
import com.AgsCh.task_scheduler.model.ScheduleRun;

public interface FunctionAssignmentRepository 
        extends JpaRepository<FunctionAssignment, Long> {

    List<FunctionAssignment> findByScheduleRun_Status(
            ScheduleRun.Status status);

    List<FunctionAssignment> findByScheduleRun_Id(Long runId);
}
