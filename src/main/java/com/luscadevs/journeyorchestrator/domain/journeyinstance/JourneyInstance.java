package com.luscadevs.journeyorchestrator.domain.journeyinstance;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.luscadevs.journey.api.generated.model.JourneyStatus;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.State;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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
    private Long version;

    public void transitionTo(State newState, Event event) {

        TransitionHistory historyEntry =
                TransitionHistory.builder().id(TransitionHistoryEventId.generate())
                        .instanceId(this.id).fromState(this.currentState).toState(newState)
                        .event(event).timestamp(Instant.now()).metadata(Map.of()).build();

        Instant now = Instant.now();
        this.currentState = newState;
        this.updatedAt = now;
        this.history.add(historyEntry);
    }

    public void updateContext(Map<String, Object> newContextData) {
        if (newContextData != null && !newContextData.isEmpty()) {
            if (this.context == null) {
                this.context = new java.util.HashMap<>();
            }
            this.context.putAll(newContextData);
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Deep merges new data into the execution context.
     * Preserves existing data and merges nested maps recursively.
     * 
     * @param data The data to merge into the context
     */
    public void mergeData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        if (this.context == null) {
            this.context = new java.util.HashMap<>();
        }
        deepMerge(this.context, data);
        this.updatedAt = Instant.now();
    }

    @SuppressWarnings("unchecked")
    private void deepMerge(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map && target.get(key) instanceof Map) {
                // Both are maps, merge recursively
                Map<String, Object> targetMap = (Map<String, Object>) target.get(key);
                Map<String, Object> sourceMap = (Map<String, Object>) value;
                deepMerge(targetMap, sourceMap);
            } else {
                // Overwrite or add the value
                target.put(key, value);
            }
        }
    }

    /**
     * Retrieves a variable value from the context.
     * Supports dot notation for nested access (e.g., "customer.id").
     * 
     * @param key The variable key, supports dot notation for nested access
     * @return The variable value, or null if not found
     */
    public Object getVariable(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        if (this.context == null) {
            return null;
        }

        if (key.contains(".")) {
            return getNestedVariable(key.split("\\."));
        } else {
            return this.context.get(key);
        }
    }

    private Object getNestedVariable(String[] keys) {
        Object current = this.context;
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
                if (current == null) {
                    return null;
                }
            } else {
                return null;
            }
        }
        return current;
    }

    /**
     * Checks if a variable exists in the context.
     * Supports dot notation for nested access (e.g., "customer.id").
     * 
     * @param key The variable key, supports dot notation for nested access
     * @return true if the variable exists, false otherwise
     */
    public boolean hasVariable(String key) {
        return getVariable(key) != null;
    }

    public static JourneyInstance start(String definitionId, Integer version, State initialState,
            Map<String, Object> context) {

        return JourneyInstance.builder().id(UUID.randomUUID().toString())
                .journeyDefinitionId(definitionId).journeyVersion(version)
                .currentState(initialState).status(JourneyStatus.RUNNING).createdAt(Instant.now())
                .updatedAt(Instant.now()).context(context).history(new ArrayList<>()).build();

    }

    public void complete() {
        this.status = JourneyStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = JourneyStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /**
     * Verifica se a jornada pode receber eventos (não está completada).
     * 
     * @throws JourneyAlreadyCompletedException se a jornada já estiver completada
     */
    public void ensureCanReceiveEvents() {
        if (this.status == JourneyStatus.COMPLETED) {
            throw new com.luscadevs.journeyorchestrator.domain.exception.JourneyAlreadyCompletedException(
                    this.id);
        }
    }

}
