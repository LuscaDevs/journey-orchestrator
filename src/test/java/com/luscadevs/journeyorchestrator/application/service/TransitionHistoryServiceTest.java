package com.luscadevs.journeyorchestrator.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.luscadevs.journeyorchestrator.application.port.TransitionHistoryRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistory;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistoryEventId;

@ExtendWith(MockitoExtension.class)
class TransitionHistoryServiceTest {

    @Mock
    private TransitionHistoryRepositoryPort repositoryPort;

    private TransitionHistoryService service;

    @BeforeEach
    void setUp() {
        service = new TransitionHistoryService(repositoryPort);
    }

    @Test
    void shouldRecordTransitionWithoutMetadata() {
        // Given
        String instanceId = "test-instance";
        State fromState = State.builder().name("START").build();
        State toState = State.builder().name("PROCESSING").build();
        Event event = Event.builder().name("PROCESS").build();

        // When
        service.recordTransition(instanceId, fromState, toState, event);

        // Then
        verify(repositoryPort).save(argThat(history -> 
            history.getInstanceId().equals(instanceId) &&
            history.getFromState().equals(fromState) &&
            history.getToState().equals(toState) &&
            history.getEvent().equals(event) &&
            history.getMetadata().isEmpty()
        ));
    }

    @Test
    void shouldRecordTransitionWithMetadata() {
        // Given
        String instanceId = "test-instance";
        State fromState = State.builder().name("START").build();
        State toState = State.builder().name("PROCESSING").build();
        Event event = Event.builder().name("PROCESS").build();
        Map<String, Object> metadata = Map.of("key", "value");

        // When
        service.recordTransition(instanceId, fromState, toState, event, metadata);

        // Then
        verify(repositoryPort).save(argThat(history -> 
            history.getInstanceId().equals(instanceId) &&
            history.getFromState().equals(fromState) &&
            history.getToState().equals(toState) &&
            history.getEvent().equals(event) &&
            history.getMetadata().equals(metadata)
        ));
    }

    @Test
    void shouldGetTransitionHistory() {
        // Given
        String instanceId = "test-instance";
        List<TransitionHistory> expectedHistory = List.of(
            createTestHistory("event1"),
            createTestHistory("event2")
        );
        when(repositoryPort.findByInstanceIdOrderByTimestampAsc(instanceId)).thenReturn(expectedHistory);

        // When
        List<TransitionHistory> result = service.getTransitionHistory(instanceId);

        // Then
        assertEquals(expectedHistory, result);
        verify(repositoryPort).findByInstanceIdOrderByTimestampAsc(instanceId);
    }

    @Test
    void shouldGetTransitionHistoryWithDateRange() {
        // Given
        String instanceId = "test-instance";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();
        List<TransitionHistory> expectedHistory = List.of(createTestHistory("event1"));
        when(repositoryPort.findByInstanceIdAndTimestampBetween(instanceId, from, to))
            .thenReturn(expectedHistory);

        // When
        List<TransitionHistory> result = service.getTransitionHistory(instanceId, from, to);

        // Then
        assertEquals(expectedHistory, result);
        verify(repositoryPort).findByInstanceIdAndTimestampBetween(instanceId, from, to);
    }

    @Test
    void shouldGetTransitionHistoryByEventType() {
        // Given
        String instanceId = "test-instance";
        String eventType = "PROCESS";
        List<TransitionHistory> expectedHistory = List.of(createTestHistory(eventType));
        when(repositoryPort.findByInstanceIdAndEventType(instanceId, eventType))
            .thenReturn(expectedHistory);

        // When
        List<TransitionHistory> result = service.getTransitionHistoryByEventType(instanceId, eventType);

        // Then
        assertEquals(expectedHistory, result);
        verify(repositoryPort).findByInstanceIdAndEventType(instanceId, eventType);
    }

    @Test
    void shouldCheckIfTransitionHistoryExists() {
        // Given
        String instanceId = "test-instance";
        when(repositoryPort.existsByInstanceId(instanceId)).thenReturn(true);

        // When
        boolean result = service.hasTransitionHistory(instanceId);

        // Then
        assertTrue(result);
        verify(repositoryPort).existsByInstanceId(instanceId);
    }

    @Test
    void shouldDeleteTransitionHistory() {
        // Given
        String instanceId = "test-instance";

        // When
        service.deleteTransitionHistory(instanceId);

        // Then
        verify(repositoryPort).deleteByInstanceId(instanceId);
    }

    private TransitionHistory createTestHistory(String eventType) {
        return TransitionHistory.builder()
                .id(TransitionHistoryEventId.generate())
                .instanceId("test-instance")
                .fromState(State.builder().name("FROM").build())
                .toState(State.builder().name("TO").build())
                .event(Event.builder().name(eventType).build())
                .timestamp(Instant.now())
                .metadata(Map.of())
                .build();
    }
}
