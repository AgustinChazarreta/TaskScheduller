package com.AgsCh.task_scheduler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "function_rules", uniqueConstraints = @UniqueConstraint(columnNames = { "house_id", "function_a_id",
        "function_b_id" }))
public class FunctionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "function_a_id")
    private Function functionA;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "function_b_id")
    private Function functionB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType type;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id")
    private House house;

    protected FunctionRule() {
    }

    public FunctionRule(Function functionA, Function functionB, RuleType type, House house) {
        this.functionA = functionA;
        this.functionB = functionB;
        this.type = type;
        this.house = house;
    }

    public Long getId() {
        return id;
    }

    public Function getFunctionA() {
        return functionA;
    }

    public void setFunctionA(Function functionA) {
        this.functionA = functionA;
    }

    public Function getFunctionB() {
        return functionB;
    }

    public void setFunctionB(Function functionB) {
        this.functionB = functionB;
    }

    public RuleType getType() {
        return type;
    }

    public void setType(RuleType type) {
        this.type = type;
    }

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    public boolean matches(Function f1, Function f2) {
        return (functionA.equals(f1) && functionB.equals(f2)) ||
                (functionA.equals(f2) && functionB.equals(f1));
    }
}
