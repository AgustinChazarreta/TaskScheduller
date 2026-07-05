package com.AgsCh.task_scheduler.model;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user_groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // opcional pero muy útil
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    private House house;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<Person> persons = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "group_working_days", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> workingDays = EnumSet.noneOf(DayOfWeek.class);

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "group_functions", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "function_id"))
    private Set<Function> functions = new HashSet<>();

    public Group() {
    }

    public Group(String name, House house) {
        this.name = name;
        this.house = house;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public House getHouse() {
        return house;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    public void addPersons(List<Person> persons) {
        persons.forEach(person -> {
            person.setGroup(this);
        });
        this.persons.addAll(persons);
    }

    public void removePersons(List<Person> persons) {
        persons.forEach(person -> {
            this.persons.remove(person);
            person.setGroup(null);
        });
    }

    public Set<DayOfWeek> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(Set<DayOfWeek> workingDays) {
        this.workingDays = workingDays != null
                ? EnumSet.copyOf(workingDays)
                : EnumSet.noneOf(DayOfWeek.class);
    }

    public Set<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(Set<Function> functions) {
        this.functions = functions != null
                ? new HashSet<>(functions)
                : new HashSet<>();
    }

    @Override
    public String toString() {
        return name;
    }
}