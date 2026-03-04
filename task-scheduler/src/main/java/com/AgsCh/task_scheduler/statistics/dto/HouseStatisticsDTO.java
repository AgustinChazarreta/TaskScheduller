package com.AgsCh.task_scheduler.statistics.dto;

import java.util.List;

public class HouseStatisticsDTO {

    private Long houseId;
    private long totalAssignments;
    private long historicalRuns;
    private List<PersonStatisticsDTO> peopleStats;
    private List<FunctionStatsDTO> functionStats;
    private List<MonthlyStatsDTO> monthlyStats;

    public HouseStatisticsDTO(
            Long houseId,
            long totalAssignments,
            long historicalRuns,
            List<PersonStatisticsDTO> peopleStats,
            List<FunctionStatsDTO> functionStats,
            List<MonthlyStatsDTO> monthlyStats) {

        this.houseId = houseId;
        this.totalAssignments = totalAssignments;
        this.historicalRuns = historicalRuns;
        this.peopleStats = peopleStats;
        this.functionStats = functionStats;
        this.monthlyStats = monthlyStats;
    }

    public Long getHouseId() {
        return houseId;
    }

    public long getTotalAssignments() {
        return totalAssignments;
    }

    public long getHistoricalRuns() {
        return historicalRuns;
    }

    public List<PersonStatisticsDTO> getPeopleStats() {
        return peopleStats;
    }

    public List<FunctionStatsDTO> getFunctionStats() {
        return functionStats;
    }

    public List<MonthlyStatsDTO> getMonthlyStats() {
        return monthlyStats;
    }
}