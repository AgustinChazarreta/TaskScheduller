package com.AgsCh.task_scheduler.dto.request;

import com.AgsCh.task_scheduler.model.RuleType;

public class FunctionRuleRequestDTO {

    private Long functionAId;
    private Long functionBId;
    private RuleType type;

    public FunctionRuleRequestDTO() {
    }

    public Long getFunctionAId() {
        return functionAId;
    }

    public void setFunctionAId(Long functionAId) {
        this.functionAId = functionAId;
    }

    public Long getFunctionBId() {
        return functionBId;
    }

    public void setFunctionBId(Long functionBId) {
        this.functionBId = functionBId;
    }

    public RuleType getType() {
        return type;
    }

    public void setType(RuleType type) {
        this.type = type;
    }
}