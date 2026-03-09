package com.AgsCh.task_scheduler.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groups")
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

    @Override
    public String toString() {
        return name;
    }
}