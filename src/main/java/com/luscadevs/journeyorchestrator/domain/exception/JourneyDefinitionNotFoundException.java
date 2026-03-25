package com.luscadevs.journeyorchestrator.domain.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Exception thrown when a journey definition cannot be found.
 * Maps to HTTP 404 Not Found status.
 */
@Getter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JourneyDefinitionNotFoundException extends DomainException {
    @EqualsAndHashCode.Include
    private final String journeyDefinitionId;

    public JourneyDefinitionNotFoundException(String journeyDefinitionId) {
        super(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND,
                String.format("Journey definition with ID '%s' not found", journeyDefinitionId));
        this.journeyDefinitionId = journeyDefinitionId;
        withContext("journeyDefinitionId", journeyDefinitionId);
    }
}
