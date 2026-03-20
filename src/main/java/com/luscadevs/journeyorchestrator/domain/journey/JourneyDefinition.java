package com.luscadevs.journeyorchestrator.domain.journey;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class JourneyDefinition {
    private String id;
    private String name;
    private Integer version;
    private Boolean active;
    private State initialState;
    private List<State> states;
    private List<Transition> transitions;

    public Transition findTransition(State fromState, Event event) {
        for (Transition transition : transitions) {
            if (transition.getSourceState().equals(fromState) && transition.getEvent().equals(event)) {
                return transition;
            }
        }
        return null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean isActive() {
        return active;
    }

    public void setInitialState(State initialState) {
        this.initialState = initialState;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }
}
