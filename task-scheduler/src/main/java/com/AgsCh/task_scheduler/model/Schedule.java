package com.AgsCh.task_scheduler.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.solver.SolverStatus;

@PlanningSolution
public class Schedule {

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "personRange")
    private List<Person> personList;

    @ProblemFactCollectionProperty
    private List<Function> functionList;

    @PlanningEntityCollectionProperty
    private List<FunctionAssignment> functionAssignmentList;

    private LocalDate startDate;
    private LocalDate endDate;

    @PlanningScore
    private HardMediumSoftScore score;

    private SolverStatus solverStatus;

    // --------- Constructores ---------
    public Schedule() {
        this.personList = new ArrayList<>();
        this.functionList = new ArrayList<>();
        this.functionAssignmentList = new ArrayList<>();
    }

    public Schedule(List<Person> personList, List<Function> functionList, List<FunctionAssignment> functionAssignmentList, LocalDate startDate, LocalDate endDate) {
        this.personList = personList != null ? personList : new ArrayList<>();
        this.functionList = functionList != null ? functionList : new ArrayList<>();
        this.functionAssignmentList = functionAssignmentList != null ? functionAssignmentList : new ArrayList<>();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // --------- Getters y setters ---------
    public List<Person> getPersonList() {
        return personList;
    }

    public List<Function> getFunctionList() {
        return functionList;
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

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
