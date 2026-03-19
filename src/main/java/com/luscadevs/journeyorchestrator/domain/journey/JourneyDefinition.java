package com.luscadevs.journeyorchestrator.domain.journey;

import java.util.List;

public class JourneyDefinition {
    private String id;
    private String name;
    private Integer version;
    private Boolean active;
    private String initialState;
    private List<State> states;
    private List<Transition> transitions;

    public List<Transition> getTransitions() {
        return transitions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getInitialState() {
        return initialState;
    }

    public void setInitialState(String initialState) {
        this.initialState = initialState;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }
}
