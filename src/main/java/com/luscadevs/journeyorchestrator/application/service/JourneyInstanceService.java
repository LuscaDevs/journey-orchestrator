package com.luscadevs.journeyorchestrator.application.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.luscadevs.journeyorchestrator.application.port.out.JourneyDefinitionRepositoryPort;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyInstanceRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.engine.JourneyEngine;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

@Service
public class JourneyInstanceService {
    private final JourneyInstanceRepositoryPort journeyInstanceRepository;
    private final JourneyDefinitionRepositoryPort journeyDefinitionRepository;
    private final JourneyEngine journeyEngine;

    public JourneyInstanceService(JourneyInstanceRepositoryPort journeyInstanceRepository,
            JourneyDefinitionRepositoryPort journeyDefinitionRepository, JourneyEngine journeyEngine) {
        this.journeyInstanceRepository = journeyInstanceRepository;
        this.journeyDefinitionRepository = journeyDefinitionRepository;
        this.journeyEngine = journeyEngine;
    }

    public JourneyInstance startJourney(String journeyCode, Integer version, Map<String, Object> context) {
        JourneyDefinition definition = journeyDefinitionRepository.findByJourneyCodeAndVersion(journeyCode, version)
                .orElseThrow(() -> new RuntimeException(
                        "Journey definition not found: " + journeyCode + " version: " + version));
        JourneyInstance instance = JourneyInstance.start(journeyCode, version, definition.getInitialState(), context);

        return journeyInstanceRepository.save(instance);
    }

    public JourneyInstance applyEvent(String instanceId, Event event) {
        JourneyInstance instance = journeyInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new RuntimeException("Journey instance not found: " + instanceId));

        JourneyDefinition definition = journeyDefinitionRepository
                .findByJourneyCodeAndVersion(instance.getJourneyDefinitionId(), instance.getJourneyVersion())
                .orElseThrow(() -> new RuntimeException(
                        "Journey definition not found: " + instance.getJourneyDefinitionId()));
        Event appliedEvent = event;

        journeyEngine.applyEvent(instance, definition, appliedEvent);

        return journeyInstanceRepository.save(instance);
    }

    public JourneyInstance getInstance(String instanceId) {
        return journeyInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new RuntimeException("Journey instance not found: " + instanceId));
    }

}
