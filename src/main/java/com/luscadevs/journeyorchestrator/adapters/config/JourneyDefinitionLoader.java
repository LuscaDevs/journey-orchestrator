package com.luscadevs.journeyorchestrator.adapters.config;

import java.util.List;

import org.springframework.stereotype.Component;

import com.luscadevs.journeyorchestrator.adapters.out.memory.InMemoryJourneyDefinitionRepository;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.StateType;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;

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
