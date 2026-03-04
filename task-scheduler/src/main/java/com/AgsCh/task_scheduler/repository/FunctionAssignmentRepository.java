package com.AgsCh.task_scheduler.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select count(fa)
            from FunctionAssignment fa
            where fa.person.id = :personId
            """)
    long countAllByPersonId(Long personId);

    @Query("""
            select count(fa)
            from FunctionAssignment fa
            where fa.person.id = :personId
            and fa.scheduleRun.house.id = :houseId
            """)
    long countAllByPersonIdAndHouseId(Long personId, Long houseId);

    @Query("""
            select fa.function.name, count(fa)
            from FunctionAssignment fa
            where fa.person.id = :personId
            group by fa.function.name
            """)
    List<Object[]> countByFunctionGrouped(Long personId);

    @Query("""
            select max(fa.date)
            from FunctionAssignment fa
            where fa.person.id = :personId
            and fa.function.id = :functionId
            """)
    LocalDate findLastExecution(Long personId, Long functionId);

    @Query("""
            select max(fa.date)
            from FunctionAssignment fa
            where fa.person.id = :personId
            """)
    LocalDate findLastAssignmentDate(Long personId);

    @Query("""
            select count(fa)
            from FunctionAssignment fa
            where fa.person.id = :personId
            and fa.date >= :start
            """)
    long countByPersonIdFromDate(Long personId, LocalDate start);

    @Query("""
                    select fa.function.name, count(fa)
                    from FunctionAssignment fa
                    where fa.scheduleRun.house.id = :houseId
                    group by fa.function.name
            """)
    List<Object[]> countFunctionsByHouse(@Param("houseId") Long houseId);

    @Query("""
            select
                    YEAR(fa.date),
                    MONTH(fa.date),
                    count(fa)
            from FunctionAssignment fa
            where fa.scheduleRun.house.id = :houseId
            group by YEAR(fa.date), MONTH(fa.date)
            order by YEAR(fa.date), MONTH(fa.date)
            """)
    List<Object[]> countAssignmentsByMonth(@Param("houseId") Long houseId);

    @Query("""
                    select
                    YEAR(fa.date),
                    MONTH(fa.date),
                    count(fa)
                    from FunctionAssignment fa
                    where fa.scheduleRun.id = :runId
                    group by YEAR(fa.date), MONTH(fa.date)
                    order by YEAR(fa.date), MONTH(fa.date)
            """)
    List<Object[]> countAssignmentsByRun(@Param("runId") Long runId);

    @Query("""
                select fa.person.id, count(fa)
                from FunctionAssignment fa
                where fa.scheduleRun.id = :runId
                group by fa.person.id
            """)
    List<Object[]> countByRunGroupedByPerson(@Param("runId") Long runId);

    @Query("""
                select fa.person.id, fa.function.name, count(fa)
                from FunctionAssignment fa
                where fa.scheduleRun.id = :runId
                group by fa.person.id, fa.function.name
            """)
    List<Object[]> countByRunGroupedByPersonAndFunction(@Param("runId") Long runId);

    @Query("""
                select fa.person.id, max(fa.date)
                from FunctionAssignment fa
                where fa.scheduleRun.id = :runId
                group by fa.person.id
            """)
    List<Object[]> findLastAssignmentDateByRun(@Param("runId") Long runId);

    @Query("""
                select year(fa.date), month(fa.date), count(fa)
                from FunctionAssignment fa
                group by year(fa.date), month(fa.date)
                order by year(fa.date), month(fa.date)
            """)
    List<Object[]> countAssignmentsByMonthHouseNeutral();

    @Query("""
                SELECT COUNT(f)
                FROM FunctionAssignment f
                WHERE f.scheduleRun.house.id = :houseId
            """)
    long countByHouseId(@Param("houseId") Long houseId);

    // ================= KPI =================

    @Query("""
                select count(fa)
                from FunctionAssignment fa
                where fa.scheduleRun.house.id = :houseId
            """)
    long countAssignmentsByHouse(@Param("houseId") Long houseId);

    // ================= PERSON CHART =================

    @Query("""
                select fa.person.fullName, count(fa)
                from FunctionAssignment fa
                where (:houseId is null or fa.scheduleRun.house.id = :houseId)
                group by fa.person.fullName
            """)
    List<Object[]> countAssignmentsGroupedByPerson(@Param("houseId") Long houseId);

    // ================= FUNCTION CHART =================

    @Query("""
                select fa.function.name, count(fa)
                from FunctionAssignment fa
                where (:houseId is null or fa.scheduleRun.house.id = :houseId)
                group by fa.function.name
            """)
    List<Object[]> countAssignmentsGroupedByFunction(@Param("houseId") Long houseId);

    // ================= MONTHLY =================

    @Query("""
                select year(fa.date), month(fa.date), count(fa)
                from FunctionAssignment fa
                where (:houseId is null or fa.scheduleRun.house.id = :houseId)
                group by year(fa.date), month(fa.date)
                order by year(fa.date), month(fa.date)
            """)
    List<Object[]> countAssignmentsByMonthFiltered(@Param("houseId") Long houseId);
}
