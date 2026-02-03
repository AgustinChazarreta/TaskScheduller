package com.AgsCh.task_scheduler.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "person_functions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"person_id", "function_id"}) //evita duplicados, hace el modelo consistente, te ahorra bugs y lógica extra
    }
)
public class PersonFunction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne(optional = false)
    @JoinColumn(name = "function_id")
    private Function function;

    @Column(nullable = false)
    private boolean active = true;

    // ---- Constructores ----

    public PersonFunction() {
    }

    public PersonFunction(Person person, Function function) {
        this.person = person;
        this.function = function;
    }

    // ---- Getters ----

    public Long getId() {
        return id;
    }

    public Person getPerson() {
        return person;
    }

    public Function getFunction() {
        return function;
    }

    public boolean isActive() {
        return active;
    }

    // ---- Setters ----

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
