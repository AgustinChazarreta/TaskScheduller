package com.AgsCh.task_scheduler.statistics.dto;

import java.time.LocalDate;
import java.util.List;

public class PersonStatisticsDTO {

    private Long personId;
    private String fullName;

    private long totalAssignments;
    private long totalLastMonth;

    private List<FunctionStatsDTO> functionBreakdown;

    private LocalDate lastAssignmentDate;

    public PersonStatisticsDTO(
            Long personId,
            String fullName,
            long totalAssignments,
            long totalLastMonth,
            List<FunctionStatsDTO> functionBreakdown,
            LocalDate lastAssignmentDate) {

        this.personId = personId;
        this.fullName = fullName;
        this.totalAssignments = totalAssignments;
        this.totalLastMonth = totalLastMonth;
        this.functionBreakdown = functionBreakdown;
        this.lastAssignmentDate = lastAssignmentDate;
    }

    public Long getPersonId() {
        return personId;
    }

    public String getFullName() {
        return fullName;
    }

    public long getTotalAssignments() {
        return totalAssignments;
    }

    public long getTotalLastMonth() {
        return totalLastMonth;
    }

    public List<FunctionStatsDTO> getFunctionBreakdown() {
        return functionBreakdown;
    }

    public LocalDate getLastAssignmentDate() {
        return lastAssignmentDate;
    }
}