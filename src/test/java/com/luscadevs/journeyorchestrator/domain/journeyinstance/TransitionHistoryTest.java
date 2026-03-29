package com.luscadevs.journeyorchestrator.domain.journeyinstance;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.State;

class TransitionHistoryTest {

    @Test
    void shouldCreateTransitionHistoryWithBuilder() {
        // Given
        TransitionHistoryEventId id = TransitionHistoryEventId.generate();
        String instanceId = "test-instance-id";
        State fromState = State.builder().name("START").build();
        State toState = State.builder().name("PROCESSING").build();
        Event event = Event.builder().name("PROCESS").description("Process event").build();
        Instant timestamp = Instant.now();
        Map<String, Object> metadata = Map.of("key", "value");

        // When
        TransitionHistory history = TransitionHistory.builder()
                .id(id)
                .instanceId(instanceId)
                .fromState(fromState)
                .toState(toState)
                .event(event)
                .timestamp(timestamp)
                .metadata(metadata)
                .build();

        // Then
        assertNotNull(history);
        assertEquals(id, history.getId());
        assertEquals(instanceId, history.getInstanceId());
        assertEquals(fromState, history.getFromState());
        assertEquals(toState, history.getToState());
        assertEquals(event, history.getEvent());
        assertEquals(timestamp, history.getTimestamp());
        assertEquals(metadata, history.getMetadata());
    }

    @Test
    void shouldCompareTimestampsCorrectly() {
        // Given
        Instant earlier = Instant.now().minusSeconds(10);
        Instant later = Instant.now();
        
        TransitionHistory earlierEvent = TransitionHistory.builder()
                .id(TransitionHistoryEventId.generate())
                .instanceId("test")
                .fromState(State.builder().name("A").build())
                .toState(State.builder().name("B").build())
                .event(Event.builder().name("EVENT1").build())
                .timestamp(earlier)
                .metadata(Map.of())
                .build();
                
        TransitionHistory laterEvent = TransitionHistory.builder()
                .id(TransitionHistoryEventId.generate())
                .instanceId("test")
                .fromState(State.builder().name("B").build())
                .toState(State.builder().name("C").build())
                .event(Event.builder().name("EVENT2").build())
                .timestamp(later)
                .metadata(Map.of())
                .build();

        // When & Then
        assertTrue(laterEvent.isAfter(earlierEvent));
        assertFalse(earlierEvent.isAfter(laterEvent));
        assertFalse(earlierEvent.isAfter(earlierEvent));
    }

    @Test
    void shouldCheckEventTypeCorrectly() {
        // Given
        TransitionHistory history = TransitionHistory.builder()
                .id(TransitionHistoryEventId.generate())
                .instanceId("test")
                .fromState(State.builder().name("A").build())
                .toState(State.builder().name("B").build())
                .event(Event.builder().name("PROCESS").build())
                .timestamp(Instant.now())
                .metadata(Map.of())
                .build();

        // When & Then
        assertTrue(history.hasEventType("PROCESS"));
        assertFalse(history.hasEventType("INVALID"));
        assertFalse(history.hasEventType(""));
    }

    @Test
    void shouldHandleNullMetadata() {
        // Given
        TransitionHistory history = TransitionHistory.builder()
                .id(TransitionHistoryEventId.generate())
                .instanceId("test")
                .fromState(State.builder().name("A").build())
                .toState(State.builder().name("B").build())
                .event(Event.builder().name("PROCESS").build())
                .timestamp(Instant.now())
                .metadata(null)
                .build();

        // When & Then
        assertNull(history.getMetadata());
    }
}
