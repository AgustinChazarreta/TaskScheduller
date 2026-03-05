package com.AgsCh.task_scheduler.dto.response;

import com.AgsCh.task_scheduler.model.RuleType;

public class FunctionRuleResponseDTO {

    private Long id;

    private Long functionAId;
    private String functionAName;

    private Long functionBId;
    private String functionBName;

    private RuleType type;

    public FunctionRuleResponseDTO(
            Long id,
            Long functionAId,
            String functionAName,
            Long functionBId,
            String functionBName,
            RuleType type) {

        this.id = id;
        this.functionAId = functionAId;
        this.functionAName = functionAName;
        this.functionBId = functionBId;
        this.functionBName = functionBName;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public Long getFunctionAId() {
        return functionAId;
    }

    public String getFunctionAName() {
        return functionAName;
    }

    public Long getFunctionBId() {
        return functionBId;
    }

    public String getFunctionBName() {
        return functionBName;
    }

    public RuleType getType() {
        return type;
    }
}