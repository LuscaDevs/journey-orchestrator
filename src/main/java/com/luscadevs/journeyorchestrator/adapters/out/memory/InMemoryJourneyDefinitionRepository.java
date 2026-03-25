package com.luscadevs.journeyorchestrator.adapters.out.memory;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.luscadevs.journeyorchestrator.application.port.out.JourneyDefinitionRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;

@Repository
@Primary
public class InMemoryJourneyDefinitionRepository implements JourneyDefinitionRepositoryPort {
    private final Map<String, JourneyDefinition> storage = new HashMap<>();

    @Override
    public Optional<JourneyDefinition> findByIdAndVersion(String id, Integer version) {
        return Optional.ofNullable(storage.get(key(id, version)));
    }

    @Override
    public JourneyDefinition save(JourneyDefinition journeyDefinition) {
        if (journeyDefinition.getId() == null) {
            journeyDefinition = journeyDefinition.toBuilder()
                    .id(java.util.UUID.randomUUID().toString())
                    .build();
        }
        if (journeyDefinition.getCreatedAt() == null) {
            journeyDefinition = journeyDefinition.toBuilder()
                    .createdAt(Instant.now())
                    .build();
        }
        storage.put(key(journeyDefinition.getId(), journeyDefinition.getVersion()), journeyDefinition);
        return journeyDefinition;
    }

    private String key(String id, Integer version) {
        return id + ":" + version;
    }

    @Override
    public List<JourneyDefinition> findByCode(String code) {
        return storage.values().stream()
                .filter(journeyDefinition -> journeyDefinition.getJourneyCode() != null
                        && journeyDefinition.getJourneyCode().equals(code))
                .toList();
    }

    @Override
    public List<JourneyDefinition> findAll() {
        return storage.values().stream().toList();
    }
}
