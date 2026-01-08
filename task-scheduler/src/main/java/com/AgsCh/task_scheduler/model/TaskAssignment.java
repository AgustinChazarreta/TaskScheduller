package com.AgsCh.task_scheduler.model;

import java.time.DayOfWeek;
import java.util.UUID;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;


@PlanningEntity
public class TaskAssignment {

    @PlanningId
    private String id = UUID.randomUUID().toString();

    private Task task;

    @PlanningVariable(valueRangeProviderRefs = {"personRange"})
    private Person person;
    
    private DayOfWeek day;
        
    public TaskAssignment() {}

    public TaskAssignment(Task task, DayOfWeek day) {
        this.task = task;
        this.day = day;
    }

    public String getId() { return id; }
    public Task getTask() { return task; }
    public Person getPerson() { return person; }
    public DayOfWeek getDay() { return day; }

    public void setPerson(Person person) { this.person = person; }  
    public void setDay(DayOfWeek day) { this.day = day; }
}