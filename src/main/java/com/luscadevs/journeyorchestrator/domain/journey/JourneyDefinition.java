package com.luscadevs.journeyorchestrator.domain.journey;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class JourneyDefinition {

    private String id;

    private String journeyCode;

    private String name;

    private Integer version;

    private boolean active;

    private State initialState;

    private List<State> states;

    private List<Transition> transitions;

    private Instant createdAt;


}
