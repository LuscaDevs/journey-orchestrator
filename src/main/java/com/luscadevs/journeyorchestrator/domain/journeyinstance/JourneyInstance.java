package com.luscadevs.journeyorchestrator.domain.journeyinstance;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.luscadevs.journey.api.generated.model.JourneyStatus;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.State;

import lombok.Getter;

@Getter
public class JourneyInstance {
    private String id;
    private String journeyDefinitionId;
    private Integer journeyVersion;
    private State currentState;
    private JourneyStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<TransitionHistory> history;
    private Map<String, Object> context;

    public void transitionTo(State newState, Event event) {

        TransitionHistory historyEntry = new TransitionHistory(
                this.currentState,
                newState,
                event,
                Instant.now(),
                null);

        Instant now = Instant.now();
        this.currentState = newState;
        this.updatedAt = now;
        this.history.add(historyEntry);
        this.updatedAt = now;
    }

    public static JourneyInstance start(
            String definitionId,
            Integer version,
            State initialState,
            Map<String, Object> context) {

        JourneyInstance instance = new JourneyInstance();

        instance.id = UUID.randomUUID().toString();

        instance.journeyDefinitionId = definitionId;
        instance.journeyVersion = version;
        instance.currentState = initialState;
        instance.status = JourneyStatus.RUNNING;
        instance.createdAt = Instant.now();
        instance.updatedAt = Instant.now();

        instance.context = context;
        instance.history = new ArrayList<>();

        return instance;
    }

    public void complete() {
        this.status = JourneyStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = JourneyStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }
}
