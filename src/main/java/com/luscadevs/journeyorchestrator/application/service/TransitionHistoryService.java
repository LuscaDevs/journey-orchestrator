package com.luscadevs.journeyorchestrator.application.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.luscadevs.journeyorchestrator.application.port.TransitionHistoryRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistory;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistoryEventId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransitionHistoryService {

    private final TransitionHistoryRepositoryPort transitionHistoryRepository;

    public void recordTransition(String instanceId, State fromState, State toState, Event event) {
        log.debug("Recording transition for instance {}: {} -> {} triggered by {}",
                instanceId, fromState.getName(), toState.getName(), event.getName());

        TransitionHistory historyEvent = TransitionHistory.builder()
                .id(TransitionHistoryEventId.generate())
                .instanceId(instanceId)
                .fromState(fromState)
                .toState(toState)
                .event(event)
                .timestamp(Instant.now())
                .metadata(java.util.Map.of())
                .build();

        transitionHistoryRepository.save(historyEvent);

        log.debug("Successfully recorded transition history event: {}", historyEvent.getId().getValue());
    }

    public void recordTransition(String instanceId, State fromState, State toState, Event event,
            java.util.Map<String, Object> metadata) {
        String fromStateName = fromState != null ? fromState.getName() : "NONE";

        log.debug("Recording transition with metadata for instance {}: {} -> {} triggered by {}",
                instanceId, fromStateName, toState.getName(), event.getName());

        TransitionHistory historyEvent = TransitionHistory.builder()
                .id(TransitionHistoryEventId.generate())
                .instanceId(instanceId)
                .fromState(fromState)
                .toState(toState)
                .event(event)
                .timestamp(Instant.now())
                .metadata(metadata != null ? metadata : java.util.Map.of())
                .build();

        transitionHistoryRepository.save(historyEvent);

        log.debug("Successfully recorded transition history event with metadata: {}", historyEvent.getId().getValue());
    }

    public List<TransitionHistory> getTransitionHistory(String instanceId) {
        log.debug("Retrieving transition history for instance: {}", instanceId);

        List<TransitionHistory> history = transitionHistoryRepository.findByInstanceIdOrderByTimestampAsc(instanceId);

        log.debug("Found {} transition events for instance: {}", history.size(), instanceId);
        return history;
    }

    public List<TransitionHistory> getTransitionHistory(String instanceId, Instant from, Instant to) {
        log.debug("Retrieving transition history for instance: {} from {} to {}", instanceId, from, to);

        List<TransitionHistory> history = transitionHistoryRepository.findByInstanceIdAndTimestampBetween(instanceId,
                from, to);

        log.debug("Found {} transition events for instance: {} in date range", history.size(), instanceId);
        return history;
    }

    public List<TransitionHistory> getTransitionHistoryByEventType(String instanceId, String eventType) {
        log.debug("Retrieving transition history for instance: {} filtered by event type: {}", instanceId, eventType);

        List<TransitionHistory> history = transitionHistoryRepository.findByInstanceIdAndEventType(instanceId,
                eventType);

        log.debug("Found {} transition events for instance: {} with event type: {}", history.size(), instanceId,
                eventType);
        return history;
    }

    public TransitionHistory getTransitionEvent(TransitionHistoryEventId eventId) {
        log.debug("Retrieving transition event: {}", eventId.getValue());

        TransitionHistory event = transitionHistoryRepository.findById(eventId);

        if (event != null) {
            log.debug("Found transition event: {}", eventId.getValue());
        } else {
            log.debug("Transition event not found: {}", eventId.getValue());
        }

        return event;
    }

    public boolean hasTransitionHistory(String instanceId) {
        log.debug("Checking if instance {} has transition history", instanceId);
        return transitionHistoryRepository.existsByInstanceId(instanceId);
    }

    public void deleteTransitionHistory(String instanceId) {
        log.warn("Deleting transition history for instance: {}", instanceId);
        transitionHistoryRepository.deleteByInstanceId(instanceId);
        log.debug("Successfully deleted transition history for instance: {}", instanceId);
    }
}
