package com.luscadevs.journeyorchestrator.adapters.config;

import org.springframework.stereotype.Component;

import com.luscadevs.journeyorchestrator.adapters.out.memory.InMemoryJourneyDefinitionRepository;
import jakarta.annotation.PostConstruct;

@Component
public class JourneyDefinitionLoader {
    private final InMemoryJourneyDefinitionRepository repository;

    public JourneyDefinitionLoader(InMemoryJourneyDefinitionRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void loadDefinitions() {

    }
}
