package com.luscadevs.journeyorchestrator.application.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.luscadevs.journeyorchestrator.application.port.out.JourneyDefinitionRepositoryPort;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyInstanceRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.engine.JourneyEngine;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionNotFoundException;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyInstanceNotFoundException;
import com.luscadevs.journeyorchestrator.domain.exception.InvalidStateTransitionException;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyAlreadyCompletedException;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;
import com.luscadevs.journey.api.generated.model.JourneyStatus;

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
                .orElseThrow(() -> new JourneyDefinitionNotFoundException(
                        journeyCode + ":" + version));

        // Check if initial state is set, if not, use the first state from the
        // definition
        State initialState = definition.getInitialState();
        if (initialState == null && definition.getStates() != null && !definition.getStates().isEmpty()) {
            initialState = definition.getStates().get(0);
        }

        if (initialState == null) {
            throw new IllegalStateException("Journey definition '" + journeyCode + "' version " + version +
                    " has no initial state defined and no states available");
        }

        JourneyInstance instance = JourneyInstance.start(journeyCode, version, initialState, context);

        return journeyInstanceRepository.save(instance);
    }

    public JourneyInstance applyEvent(String instanceId, Event event) {
        JourneyInstance instance = journeyInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new JourneyInstanceNotFoundException(instanceId));

        // Check if journey is already completed
        if (instance.getStatus() == JourneyStatus.COMPLETED) {
            throw new JourneyAlreadyCompletedException(instanceId);
        }

        JourneyDefinition definition = journeyDefinitionRepository
                .findByJourneyCodeAndVersion(instance.getJourneyDefinitionId(), instance.getJourneyVersion())
                .orElseThrow(() -> new JourneyDefinitionNotFoundException(
                        instance.getJourneyDefinitionId() + ":" + instance.getJourneyVersion()));

        try {
            journeyEngine.applyEvent(instance, definition, event);
        } catch (Exception e) {
            // Wrap engine exceptions in our domain exception
            if (e.getMessage() != null && e.getMessage().contains("not allowed in state")) {
                String currentState = instance.getCurrentState() != null ? instance.getCurrentState().getName()
                        : "UNKNOWN";
                throw new InvalidStateTransitionException(instanceId, currentState, event.getName());
            }
            throw e;
        }

        return journeyInstanceRepository.save(instance);
    }

    public JourneyInstance getInstance(String instanceId) {
        return journeyInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new JourneyInstanceNotFoundException(instanceId));
    }

}
