package com.luscadevs.journeyorchestrator.domain.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JourneyDefinitionAlreadyExistsException extends DomainException {

    @EqualsAndHashCode.Include
    private final String journeyDefinitionId;

    public JourneyDefinitionAlreadyExistsException(String journeyDefinitionId) {
        super(ErrorCode.JOURNEY_DEFINITION_ALREADY_EXISTS,
                String.format("Journey definition already exists: %s", journeyDefinitionId));
        this.journeyDefinitionId = journeyDefinitionId;
    }
}
