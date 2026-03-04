package com.AgsCh.task_scheduler.statistics.dto;

public class MonthlyStatsDTO {

    private String month;
    private long count;

    public MonthlyStatsDTO(String month, long count) {
        this.month = month;
        this.count = count;
    }

    public String getMonth() {
        return month;
    }

    public long getCount() {
        return count;
    }
}