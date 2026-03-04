package com.AgsCh.task_scheduler.statistics.dto;

import java.util.List;
import java.util.Map;

public class SystemStatisticsDTO {

    private long totalRuns;
    private long activeRuns;
    private long archivedRuns;
    private long totalAssignments;
    private Map<String, Long> personStats;
    private Map<String, Long> functionStats;
    private List<MonthlyStatsDTO> monthlyStats;

    public SystemStatisticsDTO(
            long totalRuns,
            long activeRuns,
            long archivedRuns,
            long totalAssignments,
            Map<String, Long> personStats,
            Map<String, Long> functionStats,
            List<MonthlyStatsDTO> monthlyStats) {

        this.totalRuns = totalRuns;
        this.activeRuns = activeRuns;
        this.archivedRuns = archivedRuns;
        this.totalAssignments = totalAssignments;
        this.personStats = personStats;
        this.functionStats = functionStats;
        this.monthlyStats = monthlyStats;
    }

    public long getTotalRuns() {
        return totalRuns;
    }

    public long getActiveRuns() {
        return activeRuns;
    }

    public long getArchivedRuns() {
        return archivedRuns;
    }

    public long getTotalAssignments() {
        return totalAssignments;
    }

    public Map<String, Long> getPersonStats() {
        return personStats;
    }

    public Map<String, Long> getFunctionStats() {
        return functionStats;
    }

    public List<MonthlyStatsDTO> getMonthlyStats() {
        return monthlyStats;
    }
}