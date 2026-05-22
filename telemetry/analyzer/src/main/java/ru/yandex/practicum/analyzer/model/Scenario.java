package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "scenarios",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hub_id", "name"})
)
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id")
    private String hubId;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ScenarioCondition> conditions = new HashSet<>();

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ScenarioAction> actions = new HashSet<>();

    public Scenario() {
    }

    public Scenario(String hubId, String name) {
        this.hubId = hubId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getHubId() {
        return hubId;
    }

    public String getName() {
        return name;
    }

    public Set<ScenarioCondition> getConditions() {
        return conditions;
    }

    public Set<ScenarioAction> getActions() {
        return actions;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setHubId(String hubId) {
        this.hubId = hubId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setConditions(Set<ScenarioCondition> conditions) {
        this.conditions = conditions;
    }

    public void setActions(Set<ScenarioAction> actions) {
        this.actions = actions;
    }
}