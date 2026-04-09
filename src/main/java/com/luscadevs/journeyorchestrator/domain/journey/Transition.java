package com.luscadevs.journeyorchestrator.domain.journey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Transition {

    private State sourceState;

    private Event event;

    private State targetState;

    private String condition;

}
