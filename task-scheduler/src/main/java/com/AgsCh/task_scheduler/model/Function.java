package com.AgsCh.task_scheduler.model;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "functions")
public class Function {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador único

    @Column(nullable = false)
    private String name; // Nombre de la función

    /**
     * true -> se asigna respetando orden
     * false -> puede asignarse aleatoriamente
     */
    @Column(nullable = false)
    private boolean sequential;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "function_assigned_days", joinColumns = @JoinColumn(name = "function_id"))
    @Column(name = "assigned_day")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> assignedDays = EnumSet.noneOf(DayOfWeek.class); // Días asignados

    // ---- Relaciones ----

    @OneToMany(mappedBy = "function")
    private List<PersonFunction> personFunctions = new ArrayList<>();

    @OneToMany(mappedBy = "function")
    private List<FunctionAssignment> functionAssignments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "house_id", nullable = false)
    private House house;

    // --------- Constructores ---------
    public Function() {
    }

    public Function(String name, boolean sequential, Set<DayOfWeek> assignedDays) {
        this.name = name;
        this.sequential = sequential;
        this.assignedDays = assignedDays;
    }

    // --------- Getters y setters ---------
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSequential() {
        return sequential;
    }

    public Set<DayOfWeek> getAssignedDays() {
        return assignedDays;
    }

    public List<PersonFunction> getPersonFunctions() {
        return personFunctions;
    }

    public List<FunctionAssignment> getFunctionAssignments() {
        return functionAssignments;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSequential(boolean sequential) {
        this.sequential = sequential;
    }

    public void setAssignedDays(Set<DayOfWeek> assignedDays) {
        this.assignedDays = assignedDays;
    }

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    @Override
    public String toString() {
        return name;
    }
}
