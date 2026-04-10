package com.luscadevs.journeyorchestrator.domain.journey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
public class Transition {

    private State sourceState;
    private UUID sourceStateId; // ID-based reference to source state (new, preferred)

    private Event event;

    private State targetState;
    private UUID targetStateId; // ID-based reference to target state (new, preferred)

    private String condition;

}
