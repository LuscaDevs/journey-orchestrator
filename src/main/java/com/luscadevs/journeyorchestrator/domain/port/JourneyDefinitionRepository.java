package com.luscadevs.journeyorchestrator.domain.port;

import java.util.Optional;

import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;

public interface JourneyDefinitionRepository {
    public JourneyDefinition save(JourneyDefinition journeyDefinition);

    public Optional<JourneyDefinition> findById(String id);

    public void deleteById(String id);
}
