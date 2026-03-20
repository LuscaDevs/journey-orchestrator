package com.luscadevs.journeyorchestrator.domain.journey;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Transition {
    private State sourceState;
    private Event event;
    private State targetState;
    private Boolean automatic;

    public void setSourceState(State sourceState) {
        this.sourceState = sourceState;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Boolean isAutomatic() {
        return automatic;
    }

    public void setAutomatic(Boolean automatic) {
        this.automatic = automatic;
    }
}
