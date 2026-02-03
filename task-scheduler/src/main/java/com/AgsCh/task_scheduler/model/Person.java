package com.AgsCh.task_scheduler.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "persons")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador único

    @Column(nullable = false)
    private String fullName; // Nombre completo
    
    @Column(nullable = false)
    private String nickName; // Nombre de guerra

    private LocalDate birthDate; // Fecha de nacimiento

    private String email;

    @Column(nullable = false)
    private boolean emailNotificationsEnabled = false;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDate entryDate;

    private LocalDate exitDate;

    // ---- Relaciones ----

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonFunction> personFunctions = new ArrayList<>();

    @OneToMany(mappedBy = "person")
    private List<FunctionAssignment> functionAssignments = new ArrayList<>();

/*
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "person_available_days", joinColumns = @JoinColumn(name = "person_id"))
    @Column(name = "available_day")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> availableDays;
*/
    // --------- Constructores ---------
    public Person() {}

    public Person(String fullName, LocalDate birthDate) {
        this.fullName = fullName;
        this.birthDate = birthDate;
    }

    // ---- Getters ----

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDisplayName() {
        return nickName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }

    public List<PersonFunction> getPersonFunctions() {
        return personFunctions;
    }

    public List<FunctionAssignment> getFunctionAssignments() {
        return functionAssignments;
    }

    // ---- Setters ----

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailNotificationsEnabled(boolean enabled) {
        this.emailNotificationsEnabled = enabled;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public void setExitDate(LocalDate exitDate) {
        this.exitDate = exitDate;
    }

    // ---- Helpers de dominio (opcional pero elegante) ----

    public void addPersonFunction(PersonFunction pf) {
        personFunctions.add(pf);
        pf.setPerson(this);
    }

    public void removePersonFunction(PersonFunction pf) {
        personFunctions.remove(pf);
        pf.setPerson(null);
    }

    @Override
    public String toString() {
        return nickName;
    }
}