package com.AgsCh.task_scheduler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgsCh.task_scheduler.model.FunctionAssignment;
import com.AgsCh.task_scheduler.model.ScheduleRun;

public interface FunctionAssignmentRepository
                extends JpaRepository<FunctionAssignment, Long> {

        List<FunctionAssignment> findByScheduleRun_Id(Long runId);

        List<FunctionAssignment> findByScheduleRun_House_Id(Long houseId);

        List<FunctionAssignment> findByScheduleRun_House_IdAndScheduleRun_Status(
                        Long houseId,
                        ScheduleRun.Status status);

        List<FunctionAssignment> findByScheduleRun_IdAndPerson_Id(Long runId, Long personId);
}
