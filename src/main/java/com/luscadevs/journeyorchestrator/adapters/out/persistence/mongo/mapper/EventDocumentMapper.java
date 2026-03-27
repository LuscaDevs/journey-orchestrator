package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.EventDocument;
import com.luscadevs.journeyorchestrator.domain.journey.Event;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;

/**
 * Mapper between Event domain entity and MongoDB document.
 */
@Component
public class EventDocumentMapper {

    /**
     * Convert domain entity to MongoDB document.
     */
    public EventDocument toDocument(Event event) {
        if (event == null) {
            return null;
        }

        EventDocument document = new EventDocument();
        document.setEventType(event.getName());
        document.setEventData(event.getMetadata() != null ? event.getMetadata() : new HashMap<>());
        document.setMetadata(event.getMetadata() != null ? event.getMetadata() : new HashMap<>());
        
        // Set timestamp to current time when converting to document
        document.setTimestamp(Instant.now().toString());
        
        // Other fields need to be set by the calling context
        document.setJourneyInstanceId(null);
        document.setPreviousState(null);
        document.setNewState(null);
        document.setContext(new HashMap<>());
        document.setUserId(null);

        return document;
    }

    /**
     * Convert MongoDB document to domain entity.
     */
    public Event toDomain(EventDocument document) {
        if (document == null) {
            return null;
        }

        return Event.builder()
                .name(document.getEventType())
                .description(null) // description not available in document
                .metadata(document.getEventData() != null ? document.getEventData() : new HashMap<>())
                .build();
    }
}
