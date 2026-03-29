package com.luscadevs.journeyorchestrator.domain.journeyinstance;

import java.time.Instant;
import java.util.Map;

import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.State;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor
public class TransitionHistory {
    private final TransitionHistoryEventId id;
    private final String instanceId;
    private final State fromState; // Can be null for initial state
    private final State toState;
    private final Event event;
    private final Instant timestamp;
    private final Map<String, Object> metadata;

    // Domain behavior
    public boolean isAfter(TransitionHistory other) {
        return this.timestamp.isAfter(other.timestamp);
    }

    public boolean hasEventType(String eventType) {
        return this.event.getName().equals(eventType);
    }
}
