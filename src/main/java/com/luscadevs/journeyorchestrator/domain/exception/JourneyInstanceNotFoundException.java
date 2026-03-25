package com.luscadevs.journeyorchestrator.domain.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Exception thrown when a journey instance cannot be found.
 * Maps to HTTP 404 Not Found status.
 */
@Getter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JourneyInstanceNotFoundException extends DomainException {
    @EqualsAndHashCode.Include
    private final String journeyInstanceId;

    public JourneyInstanceNotFoundException(String journeyInstanceId) {
        super(ErrorCode.JOURNEY_INSTANCE_NOT_FOUND,
                String.format("Journey instance with ID '%s' not found", journeyInstanceId));
        this.journeyInstanceId = journeyInstanceId;
        withContext("journeyInstanceId", journeyInstanceId);
    }
}
