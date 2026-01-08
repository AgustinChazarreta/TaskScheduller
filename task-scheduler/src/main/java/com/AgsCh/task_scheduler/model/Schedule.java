package com.AgsCh.task_scheduler.model;
import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverStatus;

@PlanningSolution
public class Schedule {

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "personRange")
    private List<Person> personList;
    
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "taskRange")
    private List<Task> taskList;
    
    @PlanningEntityCollectionProperty
    private List<TaskAssignment> taskAssignmentList;

    @PlanningScore
    private HardSoftScore score;

    private SolverStatus solverStatus;
        
    public Schedule() {}

    public Schedule(List<Person> personList, List<Task> taskList, List<TaskAssignment> taskAssignmentList) {
        this.personList = personList;
        this.taskList = taskList;
        this.taskAssignmentList = taskAssignmentList;
    }

    public List<Person> getPersonList() { return personList; }
    public List<Task> getTaskList() { return taskList; }
    public List<TaskAssignment> getTaskAssignmentList() { return taskAssignmentList; }
    public HardSoftScore getScore() { return score; }
    public SolverStatus getSolverStatus() { return solverStatus; }
    
    public void setSolverStatus(SolverStatus solverStatus) { this.solverStatus = solverStatus; }
}
