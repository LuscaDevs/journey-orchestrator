package com.luscadevs.journeyorchestrator.application.port.out;

import java.util.Optional;

import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;

public interface JourneyDefinitionRepositoryPort {
    public Optional<JourneyDefinition> findByIdAndVersion(String id, Integer version);

    public void register(JourneyDefinition journeyDefinition);
}
