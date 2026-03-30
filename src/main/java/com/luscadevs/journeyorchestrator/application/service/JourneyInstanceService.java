package com.luscadevs.journeyorchestrator.application.service;

import java.util.List;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JourneyInstanceService {
    private final JourneyInstanceRepositoryPort journeyInstanceRepository;
    private final JourneyDefinitionRepositoryPort journeyDefinitionRepository;
    private final JourneyEngine journeyEngine;
    private final TransitionHistoryService transitionHistoryService;

    public JourneyInstance startJourney(String journeyCode, Integer version,
            Map<String, Object> context) {
        JourneyDefinition definition = journeyDefinitionRepository
                .findByJourneyCodeAndVersion(journeyCode, version).orElseThrow(
                        () -> new JourneyDefinitionNotFoundException(journeyCode + ":" + version));

        // Check if initial state is set, if not, use the first state from the
        // definition
        State initialState = definition.getInitialState();
        if (initialState == null && definition.getStates() != null
                && !definition.getStates().isEmpty()) {
            initialState = definition.getStates().get(0);
        }

        if (initialState == null) {
            throw new IllegalStateException("Journey definition '" + journeyCode + "' version "
                    + version + " has no initial state defined and no states available");
        }

        JourneyInstance instance =
                JourneyInstance.start(journeyCode, version, initialState, context);

        // Record the initial state as a transition history event
        Event startEvent = Event.builder().name("JOURNEY_STARTED")
                .description("Journey instance started").build();

        JourneyInstance savedInstance = journeyInstanceRepository.save(instance);

        // Record transition history after saving the instance
        transitionHistoryService.recordTransition(savedInstance.getId(), null, initialState,
                startEvent, Map.of("journeyCode", journeyCode, "version", version));

        return savedInstance;
    }

    public JourneyInstance applyEvent(String instanceId, Event event, Object eventData) {
        JourneyInstance instance = journeyInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new JourneyInstanceNotFoundException(instanceId));

        // Check if journey is already completed
        if (instance.getStatus() == JourneyStatus.COMPLETED) {
            throw new JourneyAlreadyCompletedException(instanceId);
        }

        JourneyDefinition definition = journeyDefinitionRepository
                .findByJourneyCodeAndVersion(instance.getJourneyDefinitionId(),
                        instance.getJourneyVersion())
                .orElseThrow(() -> new JourneyDefinitionNotFoundException(
                        instance.getJourneyDefinitionId() + ":" + instance.getJourneyVersion()));

        State previousState = instance.getCurrentState();

        try {
            journeyEngine.applyEvent(instance, definition, event, eventData);
        } catch (Exception e) {
            // Wrap engine exceptions in our domain exception
            if (e.getMessage() != null) {
                String currentState =
                        instance.getCurrentState() != null ? instance.getCurrentState().getName()
                                : "UNKNOWN";

                if (e.getMessage().contains("not allowed in state")) {
                    throw new InvalidStateTransitionException(instanceId, currentState,
                            event.getName());
                } else if (e.getMessage().contains("conditions not met")) {
                    // Criar exceção específica para condições não atendidas
                    throw new InvalidStateTransitionException(instanceId, currentState,
                            event.getName() + " (conditions not met)");
                }
            }
            throw e;
        }

        JourneyInstance savedInstance = journeyInstanceRepository.save(instance);

        // Record transition history after successful state change
        State newState = savedInstance.getCurrentState();
        if (!previousState.equals(newState)) {
            transitionHistoryService.recordTransition(instanceId, previousState, newState, event,
                    Map.of("journeyCode", instance.getJourneyDefinitionId(), "version",
                            instance.getJourneyVersion()));
        }

        return savedInstance;
    }

    public JourneyInstance getInstance(String instanceId) {
        return journeyInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new JourneyInstanceNotFoundException(instanceId));
    }

    public List<JourneyInstance> getAllInstances() {
        return journeyInstanceRepository.findAll();
    }

}
