package com.luscadevs.journeyorchestrator.adapters.out.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.luscadevs.journeyorchestrator.application.port.out.JourneyDefinitionRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;

@Repository
public class InMemoryJourneyDefinitionRepository implements JourneyDefinitionRepositoryPort {
    private final Map<String, JourneyDefinition> storage = new HashMap<>();

    @Override
    public Optional<JourneyDefinition> findByIdAndVersion(String id, Integer version) {
        return Optional.ofNullable(storage.get(key(id, version)));
    }

    @Override
    public void register(JourneyDefinition journeyDefinition) {
        storage.put(key(journeyDefinition.getId(), journeyDefinition.getVersion()), journeyDefinition);
    }

    private String key(String id, Integer version) {
        return id + ":" + version;
    }
}
