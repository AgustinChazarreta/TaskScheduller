package com.AgsCh.task_scheduler.statistics.dto;

public class FunctionStatsDTO {

    private String functionName;
    private long count;

    public FunctionStatsDTO(String functionName, long count) {
        this.functionName = functionName;
        this.count = count;
    }

    public String getFunctionName() {
        return functionName;
    }

    public long getCount() {
        return count;
    }
}