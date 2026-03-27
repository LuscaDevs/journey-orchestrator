package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper;

import com.luscadevs.journey.api.generated.model.JourneyStatus;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyInstanceDocument;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Mapper between JourneyInstance domain entity and MongoDB document.
 */
@Component
public class JourneyInstanceDocumentMapper {

    /**
     * Convert domain entity to MongoDB document.
     */
    public JourneyInstanceDocument toDocument(JourneyInstance journeyInstance) {
        if (journeyInstance == null) {
            return null;
        }

        JourneyInstanceDocument document = new JourneyInstanceDocument();
        document.setId(journeyInstance.getId());
        document.setJourneyDefinitionId(journeyInstance.getJourneyDefinitionId());
        document.setJourneyVersion(journeyInstance.getJourneyVersion());
        document.setCurrentState(
                journeyInstance.getCurrentState() != null ? journeyInstance.getCurrentState().getName() : null);
        document.setStatus(journeyInstance.getStatus() != null ? journeyInstance.getStatus().name() : null);
        document.setContext(journeyInstance.getContext());
        document.setMetadata(null); // metadata not available in current domain
        document.setStartedAt(
                journeyInstance.getCreatedAt() != null ? journeyInstance.getCreatedAt().toString() : null);
        document.setCompletedAt(null); // completedAt not available in current domain
        document.setLastActivityAt(
                journeyInstance.getUpdatedAt() != null ? journeyInstance.getUpdatedAt().toString() : null);

        return document;
    }

    /**
     * Convert MongoDB document to domain entity.
     */
    public JourneyInstance toDomain(JourneyInstanceDocument document) {
        if (document == null) {
            return null;
        }

        return JourneyInstance.builder()
                .id(document.getId())
                .journeyDefinitionId(document.getJourneyDefinitionId())
                .journeyVersion(document.getJourneyVersion())
                .currentState(
                        document.getCurrentState() != null ? State.builder().name(document.getCurrentState()).build()
                                : null)
                .status(document.getStatus() != null ? JourneyStatus.valueOf(document.getStatus()) : null)
                .createdAt(document.getStartedAt() != null ? Instant.parse(document.getStartedAt()) : null)
                .updatedAt(document.getLastActivityAt() != null ? Instant.parse(document.getLastActivityAt()) : null)
                .history(new ArrayList<>()) // history - initialize empty
                .context(document.getContext())
                .build();
    }
}
