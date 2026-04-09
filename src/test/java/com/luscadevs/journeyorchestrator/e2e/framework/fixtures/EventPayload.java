package com.luscadevs.journeyorchestrator.e2e.framework.fixtures;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
import java.util.HashMap;

/**
 * Event payload class with Lombok builder support for test fixtures.
 */
@Data
@Builder
public class EventPayload {
    private String event;
    private Map<String, Object> payload;

    public EventPayload() {
        this.payload = new HashMap<>();
    }

    public EventPayload(String event, Map<String, Object> payload) {
        this.event = event;
        this.payload = payload != null ? payload : new HashMap<>();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("event", event);
        result.put("payload", new HashMap<>(payload));
        return result;
    }

    public static EventPayloadBuilder builder() {
        return new EventPayloadBuilder().payload(new HashMap<>())
                .payload(Map.of("timestamp", java.time.Instant.now().toString()));
    }
}
