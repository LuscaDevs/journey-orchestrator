package com.luscadevs.journeyorchestrator.application.port.out;

import java.util.List;
import java.util.Optional;

import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;

public interface JourneyDefinitionRepositoryPort {
    public Optional<JourneyDefinition> findByJourneyCodeAndVersion(String id, Integer version);

    public List<JourneyDefinition> findByCode(String code);

    public List<JourneyDefinition> findAll();

    public JourneyDefinition save(JourneyDefinition journeyDefinition);
}
