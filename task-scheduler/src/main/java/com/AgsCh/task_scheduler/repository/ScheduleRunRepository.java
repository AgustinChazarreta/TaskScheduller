package com.AgsCh.task_scheduler.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.AgsCh.task_scheduler.model.ScheduleRun;
import com.AgsCh.task_scheduler.model.ScheduleRun.Status;

import jakarta.transaction.Transactional;

public interface ScheduleRunRepository extends JpaRepository<ScheduleRun, Long> {

        /*
         * =========================
         * ARCHIVAR ACTIVO POR HOUSE
         * =========================
         */
        @Modifying
        @Transactional
        @Query("""
                        update ScheduleRun r
                        set r.status = :archived
                        where r.status = :active
                        and r.house.id = :houseId
                                """)
        void archiveActiveRunByHouse(
                        @Param("archived") ScheduleRun.Status archived,
                        @Param("active") ScheduleRun.Status active,
                        @Param("houseId") Long houseId);

        /*
         * =========================
         * OBTENER RUN ACTIVO POR HOUSE
         * =========================
         */
        Optional<ScheduleRun> findByHouseIdAndStatus(
                        Long houseId,
                        ScheduleRun.Status status);

        /*
         * =========================
         * OBTENER RUNS POR HOUSE
         * =========================
         */
        List<ScheduleRun> findByHouse_IdOrderByCreatedAtDesc(Long houseId);

        Optional<ScheduleRun> findTopByHouse_IdOrderByCreatedAtDesc(Long houseId);

        List<ScheduleRun> findAllByOrderByCreatedAtDesc();

        Optional<ScheduleRun> findTopByOrderByCreatedAtDesc();

        long countByCreatedAtBetween(LocalDateTime startOfMonth, LocalDateTime startOfNextMonth);

        long countByStatus(Status active);

        long countByHouseIdAndStatus(Long houseId, Status active);

        long countByHouse_Id(Long houseId);

        @Query("""
                            select count(sr)
                            from ScheduleRun sr
                            where sr.house.id = :houseId
                        """)
        long countByHouse(@Param("houseId") Long houseId);

        @Query("""
                            select count(sr)
                            from ScheduleRun sr
                            where sr.house.id = :houseId
                            and sr.status = :status
                        """)
        long countByHouseAndStatus(@Param("houseId") Long houseId,
                        @Param("status") ScheduleRun.Status status);
}
