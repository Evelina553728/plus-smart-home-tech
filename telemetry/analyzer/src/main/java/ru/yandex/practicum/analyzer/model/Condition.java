package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "conditions")
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String operation;

    @Column(name = "condition_value")
    private Integer value;

    public Condition() {
    }

    public Condition(String type, String operation, Integer value) {
        this.type = type;
        this.operation = operation;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getOperation() {
        return operation;
    }

    public Integer getValue() {
        return value;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}