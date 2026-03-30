package com.luscadevs.journeyorchestrator.domain.exception;

import lombok.Getter;

/**
 * Exception thrown when a journey definition contains invalid condition syntax. Maps to HTTP 400
 * Bad Request status.
 */
@Getter
public class InvalidConditionSyntaxException extends DomainException {

    private final String condition;

    public InvalidConditionSyntaxException(String condition, String detailedMessage) {
        super(ErrorCode.INVALID_CONDITION_SYNTAX,
                String.format("Invalid condition syntax: '%s'. %s", condition, detailedMessage));
        this.condition = condition;
        withContext("condition", condition);
        withContext("detailedError", detailedMessage);
    }
}
