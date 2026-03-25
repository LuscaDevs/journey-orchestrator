package com.luscadevs.journeyorchestrator.domain.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Exception thrown when attempting to operate on a journey that has already
 * completed.
 * Maps to HTTP 422 Unprocessable Entity status.
 */
@Getter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JourneyAlreadyCompletedException extends DomainException {
    @EqualsAndHashCode.Include
    private final String journeyInstanceId;

    public JourneyAlreadyCompletedException(String journeyInstanceId) {
        super(ErrorCode.JOURNEY_ALREADY_COMPLETED,
                String.format("Journey instance '%s' has already completed and cannot be modified", journeyInstanceId));
        this.journeyInstanceId = journeyInstanceId;
        withContext("journeyInstanceId", journeyInstanceId);
        withContext("completedState", "COMPLETED");
    }
}
