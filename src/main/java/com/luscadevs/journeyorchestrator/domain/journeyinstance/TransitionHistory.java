package com.luscadevs.journeyorchestrator.domain.journeyinstance;

import java.time.Instant;
import java.util.Map;

import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.State;

public class TransitionHistory {
    private State fromState;
    private State toState;
    private Event event;
    private Instant timestamp;
    private Map<String, Object> metadata;

    public TransitionHistory() {
    }

    public TransitionHistory(
            State fromState,
            State toState,
            Event event,
            Instant timestamp) {
        this.fromState = fromState;
        this.toState = toState;
        this.event = event;
        this.timestamp = timestamp;
    }

    public State getFromState() {
        return fromState;
    }

    public State getToState() {
        return toState;
    }

    public Event getEvent() {
        return event;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
