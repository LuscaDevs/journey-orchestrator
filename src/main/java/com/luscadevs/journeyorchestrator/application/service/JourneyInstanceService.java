package com.luscadevs.journeyorchestrator.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.luscadevs.journeyorchestrator.application.port.out.JourneyDefinitionRepositoryPort;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyInstanceRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.engine.JourneyEngine;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionNotFoundException;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyInstanceNotFoundException;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

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
                                .findByJourneyCodeAndVersion(journeyCode, version)
                                .orElseThrow(() -> new JourneyDefinitionNotFoundException(
                                                journeyCode + ":" + version));

                // Check if initial state is set, if not, use the first state from the
                // definition
                State initialState = definition.getInitialState();
                if (initialState == null && definition.getStates() != null
                                && !definition.getStates().isEmpty()) {
                        initialState = definition.getStates().get(0);
                }

                if (initialState == null) {
                        throw new IllegalStateException("Journey definition '" + journeyCode
                                        + "' version " + version
                                        + " has no initial state defined and no states available");
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
                int maxRetries = 3;

                for (int attempt = 1; attempt <= maxRetries; attempt++) {
                        try {
                                // 1. Carregar instância (re-carregar a cada tentativa)
                                JourneyInstance instance = journeyInstanceRepository
                                                .findById(instanceId)
                                                .orElseThrow(() -> new JourneyInstanceNotFoundException(
                                                                instanceId));

                                // Validar status antes de buscar definição (otimização e mantém
                                // comportamento
                                // esperado)
                                instance.ensureCanReceiveEvents();

                                JourneyDefinition definition = journeyDefinitionRepository
                                                .findByJourneyCodeAndVersion(
                                                                instance.getJourneyDefinitionId(),
                                                                instance.getJourneyVersion())
                                                .orElseThrow(() -> new JourneyDefinitionNotFoundException(
                                                                instance.getJourneyDefinitionId()
                                                                                + ":"
                                                                                + instance.getJourneyVersion()));

                                State previousState = instance.getCurrentState();

                                // Delegar para a entidade - validação e lógica de negócio estão no
                                // domínio
                                journeyEngine.applyEvent(instance, definition, event, eventData);

                                JourneyInstance savedInstance =
                                                journeyInstanceRepository.save(instance);

                                // Record transition history after successful state change
                                State newState = savedInstance.getCurrentState();
                                if (!previousState.equals(newState)) {
                                        transitionHistoryService.recordTransition(instanceId,
                                                        previousState, newState, event,
                                                        Map.of("journeyCode", instance
                                                                        .getJourneyDefinitionId(),
                                                                        "version",
                                                                        instance.getJourneyVersion()));
                                }

                                return savedInstance;

                        } catch (java.util.ConcurrentModificationException e) {
                                if (attempt == maxRetries) {
                                        throw new RuntimeException("Failed to apply event after "
                                                        + maxRetries
                                                        + " attempts due to concurrent modification",
                                                        e);
                                }
                                // Pequeno delay antes de retry (exponential backoff simples)
                                try {
                                        Thread.sleep(50 * attempt); // 50ms, 100ms, 150ms
                                } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                        throw new RuntimeException("Interrupted during retry", ie);
                                }
                        }
                }

                throw new RuntimeException("Unexpected error in applyEvent retry logic");
        }

        public JourneyInstance getInstance(String instanceId) {
                return journeyInstanceRepository.findById(instanceId).orElseThrow(
                                () -> new JourneyInstanceNotFoundException(instanceId));
        }

        public List<JourneyInstance> getAllInstances() {
                return journeyInstanceRepository.findAll();
        }

}
