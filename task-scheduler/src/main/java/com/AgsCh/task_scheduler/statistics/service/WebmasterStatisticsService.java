package com.AgsCh.task_scheduler.statistics.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.AgsCh.task_scheduler.model.ScheduleRun;
import com.AgsCh.task_scheduler.repository.FunctionAssignmentRepository;
import com.AgsCh.task_scheduler.repository.ScheduleRunRepository;
import com.AgsCh.task_scheduler.statistics.dto.MonthlyStatsDTO;
import com.AgsCh.task_scheduler.statistics.dto.SystemStatisticsDTO;

@Service
public class WebmasterStatisticsService {

    private final ScheduleRunRepository runRepo;
    private final FunctionAssignmentRepository assignmentRepo;

    public WebmasterStatisticsService(ScheduleRunRepository runRepo,
            FunctionAssignmentRepository assignmentRepo) {
        this.runRepo = runRepo;
        this.assignmentRepo = assignmentRepo;
    }

    // ================= ENTRY POINT =================

    public SystemStatisticsDTO getStatistics(Long houseId) {
        return buildStats(houseId);
    }

    // ================= CORE BUILDER =================

    private SystemStatisticsDTO buildStats(Long houseId) {

        long totalRuns;
        long activeRuns;
        long archivedRuns;
        long totalAssignments;

        if (houseId == null) {
            totalRuns = runRepo.count();
            activeRuns = runRepo.countByStatus(ScheduleRun.Status.ACTIVE);
            archivedRuns = runRepo.countByStatus(ScheduleRun.Status.ARCHIVED);
            totalAssignments = assignmentRepo.count();
        } else {
            totalRuns = runRepo.countByHouse(houseId);
            activeRuns = runRepo.countByHouseAndStatus(houseId, ScheduleRun.Status.ACTIVE);
            archivedRuns = runRepo.countByHouseAndStatus(houseId, ScheduleRun.Status.ARCHIVED);
            totalAssignments = assignmentRepo.countAssignmentsByHouse(houseId);
        }

        // PERSON CHART
        Map<String, Long> personStats = assignmentRepo
                .countAssignmentsGroupedByPerson(houseId)
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue()));

        // FUNCTION CHART
        Map<String, Long> functionStats = assignmentRepo
                .countAssignmentsGroupedByFunction(houseId)
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue()));

        // MONTHLY
        List<MonthlyStatsDTO> monthlyStats = assignmentRepo
                .countAssignmentsByMonthFiltered(houseId)
                .stream()
                .map(row -> {
                    int year = ((Number) row[0]).intValue();
                    int month = ((Number) row[1]).intValue();
                    long count = ((Number) row[2]).longValue();
                    String formatted = String.format("%04d-%02d", year, month);
                    return new MonthlyStatsDTO(formatted, count);
                })
                .toList();

        return new SystemStatisticsDTO(
                totalRuns,
                activeRuns,
                archivedRuns,
                totalAssignments,
                personStats,
                functionStats,
                monthlyStats);
    }
}