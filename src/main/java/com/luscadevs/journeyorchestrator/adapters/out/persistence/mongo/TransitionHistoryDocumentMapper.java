package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistory;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistoryEventId;

import lombok.extern.slf4j.Slf4j;

@Component("transitionHistoryDocumentMapper")
@Slf4j
public class TransitionHistoryDocumentMapper {

    public TransitionHistoryDocument toDocument(TransitionHistory event) {
        log.debug("Converting transition history event to document: {}", event.getId().getValue());

        TransitionHistoryDocument document = TransitionHistoryDocument.builder()
                .id(event.getId().getValue())
                .instanceId(event.getInstanceId())
                .fromState(event.getFromState() != null ? event.getFromState().getName() : null)
                .toState(event.getToState().getName())
                .eventType(event.getEvent().getName())
                .eventData(event.getEvent().getDescription()) // Using description as event data
                .timestamp(event.getTimestamp())
                .metadata(event.getMetadata())
                .createdAt(Instant.now())
                .build();

        log.debug("Successfully converted transition history event to document");
        return document;
    }

    public TransitionHistory toDomain(TransitionHistoryDocument document) {
        log.debug("Converting transition history document to domain: {}", document.getId());

        State fromState = document.getFromState() != null ? State.builder().name(document.getFromState()).build()
                : null;
        State toState = State.builder().name(document.getToState()).build();
        Event event = Event.builder()
                .name(document.getEventType())
                .description(document.getEventData())
                .build();

        TransitionHistory historyEvent = TransitionHistory.builder()
                .id(TransitionHistoryEventId.of(document.getId()))
                .instanceId(document.getInstanceId())
                .fromState(fromState)
                .toState(toState)
                .event(event)
                .timestamp(document.getTimestamp())
                .metadata(document.getMetadata())
                .build();

        log.debug("Successfully converted transition history document to domain");
        return historyEvent;
    }
}
