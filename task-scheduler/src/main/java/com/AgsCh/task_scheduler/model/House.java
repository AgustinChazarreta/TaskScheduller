package com.AgsCh.task_scheduler.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "houses")
public class House {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private boolean active = true;

    private LocalDateTime createdAt;

    // ========================
    // RELACIONES
    // ========================

    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Person> persons = new ArrayList<>();

    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Function> functions = new ArrayList<>();

    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleRun> scheduleRuns = new ArrayList<>();

    public void addPerson(Person person) {
        persons.add(person);
        person.setHouse(this);
    }

    public void removePerson(Person person) {
        persons.remove(person);
        person.setHouse(null);
    }

    /** Getters y Setters */
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public List<ScheduleRun> getScheduleRuns() {
        return scheduleRuns;
    }
}
