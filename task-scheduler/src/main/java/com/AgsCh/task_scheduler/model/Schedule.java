package com.AgsCh.task_scheduler.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.solver.SolverStatus;

@PlanningSolution
public class Schedule {

    // ================================
    // Problem Facts
    // ================================

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "personRange")
    private List<Person> personList;

    @ProblemFactCollectionProperty
    private List<Function> functionList;

    @ProblemFactCollectionProperty
    private List<FunctionRule> functionRuleList;

    // Lookup optimizado (no lo maneja OptaPlanner)
    private transient Map<String, RuleType> functionRuleMap;

    // ================================
    // Planning Entities
    // ================================

    @PlanningEntityCollectionProperty
    private List<FunctionAssignment> functionAssignmentList;

    // ================================
    // Metadata
    // ================================

    private LocalDate startDate;
    private LocalDate endDate;

    @PlanningScore
    private HardMediumSoftScore score;

    private SolverStatus solverStatus;

    // ================================
    // Constructor vacío (OBLIGATORIO)
    // ================================

    public Schedule() {
        this.personList = new ArrayList<>();
        this.functionList = new ArrayList<>();
        this.functionAssignmentList = new ArrayList<>();
        this.functionRuleList = new ArrayList<>();
        this.functionRuleMap = new HashMap<>();
    }

    // ================================
    // Constructor SIN rules
    // (para ScheduleMapper.toModel)
    // ================================

    public Schedule(List<Person> personList,
            List<Function> functionList,
            List<FunctionAssignment> functionAssignmentList,
            LocalDate startDate,
            LocalDate endDate) {

        this.personList = personList != null ? personList : new ArrayList<>();
        this.functionList = functionList != null ? functionList : new ArrayList<>();
        this.functionAssignmentList = functionAssignmentList != null ? functionAssignmentList : new ArrayList<>();
        this.functionRuleList = new ArrayList<>();
        this.functionRuleMap = new HashMap<>();

        this.startDate = startDate;
        this.endDate = endDate;
    }

    // ================================
    // Rule Map Builder
    // ================================

    private void buildRuleMap() {

        this.functionRuleMap = new HashMap<>();

        if (functionRuleList == null) {
            return;
        }

        for (FunctionRule rule : functionRuleList) {

            if (rule == null ||
                    rule.getFunctionA() == null ||
                    rule.getFunctionB() == null ||
                    rule.getType() == null) {
                continue;
            }

            Long idA = rule.getFunctionA().getId();
            Long idB = rule.getFunctionB().getId();

            if (idA == null || idB == null) {
                continue;
            }

            String key = buildKey(idA, idB);
            functionRuleMap.put(key, rule.getType());
        }
    }

    private String buildKey(Long a, Long b) {
        return a < b ? a + "-" + b : b + "-" + a;
    }

    // ================================
    // Getters
    // ================================

    public List<Person> getPersonList() {
        return personList;
    }

    public List<Function> getFunctionList() {
        return functionList;
    }

    public List<FunctionRule> getFunctionRuleList() {
        return functionRuleList;
    }

    public List<FunctionAssignment> getFunctionAssignmentList() {
        return functionAssignmentList;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public HardMediumSoftScore getScore() {
        return score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    // ================================
    // Setters importantes
    // ================================

    public void setFunctionRuleList(List<FunctionRule> functionRuleList) {
        this.functionRuleList = functionRuleList != null ? functionRuleList : new ArrayList<>();
        buildRuleMap(); // 🔥 reconstruye el mapa automáticamente
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    // ================================
    // Lookup optimizado para constraints
    // ================================

    public RuleType getRuleType(Long f1, Long f2) {

        if (functionRuleMap == null || f1 == null || f2 == null) {
            return null;
        }

        String key = buildKey(f1, f2);
        return functionRuleMap.get(key);
    }
}