package com.luscadevs.journeyorchestrator.domain.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Exception thrown when an invalid state transition is attempted.
 * Maps to HTTP 422 Unprocessable Entity status.
 */
@Getter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class InvalidStateTransitionException extends DomainException {
    @EqualsAndHashCode.Include
    private final String journeyInstanceId;
    @EqualsAndHashCode.Include
    private final String fromState;
    @EqualsAndHashCode.Include
    private final String toState;

    public InvalidStateTransitionException(String journeyInstanceId, String fromState, String toState) {
        super(ErrorCode.INVALID_STATE_TRANSITION,
                String.format("Invalid state transition from '%s' to '%s' for journey instance '%s'",
                        fromState, toState, journeyInstanceId));
        this.journeyInstanceId = journeyInstanceId;
        this.fromState = fromState;
        this.toState = toState;
        withContext("journeyInstanceId", journeyInstanceId);
        withContext("fromState", fromState);
        withContext("toState", toState);
    }
}
