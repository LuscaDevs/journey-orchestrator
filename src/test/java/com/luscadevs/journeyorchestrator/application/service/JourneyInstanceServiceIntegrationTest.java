package com.luscadevs.journeyorchestrator.application.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests to verify that domain exceptions are properly used in JourneyInstanceService
 * methods.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JourneyInstanceServiceIntegrationTest {

        @Mock
        private JourneyInstanceRepositoryPort journeyInstanceRepository;

        @Mock
        private JourneyDefinitionRepositoryPort journeyDefinitionRepository;

        @Mock
        private JourneyEngine journeyEngine;

        @Mock
        private TransitionHistoryService transitionHistoryService;

        private JourneyInstanceService journeyInstanceService;

        @BeforeEach
        void setUp() {
                journeyInstanceService = new JourneyInstanceService(journeyInstanceRepository,
                                journeyDefinitionRepository, journeyEngine,
                                transitionHistoryService);
        }

        @Test
        void shouldThrowJourneyDefinitionNotFoundExceptionWhenStartingJourneyWithNonExistentDefinition() {
                // Given
                String journeyCode = "NON_EXISTENT";
                Integer version = 1;
                Map<String, Object> context = Map.of("key", "value");

                when(journeyDefinitionRepository.findByJourneyCodeAndVersion(journeyCode, version))
                                .thenReturn(Optional.empty());

                // When & Then
                JourneyDefinitionNotFoundException exception = assertThrows(
                                JourneyDefinitionNotFoundException.class,
                                () -> journeyInstanceService.startJourney(journeyCode, version,
                                                context));

                assertEquals("NON_EXISTENT:1", exception.getJourneyDefinitionId());
                verify(journeyDefinitionRepository).findByJourneyCodeAndVersion(journeyCode,
                                version);
                verify(journeyInstanceRepository, never()).save(any());
        }

        @Test
        void shouldThrowJourneyInstanceNotFoundExceptionWhenGettingNonExistentInstance() {
                // Given
                String instanceId = "non-existent-instance";

                when(journeyInstanceRepository.findById(instanceId)).thenReturn(Optional.empty());

                // When & Then
                JourneyInstanceNotFoundException exception = assertThrows(
                                JourneyInstanceNotFoundException.class,
                                () -> journeyInstanceService.getInstance(instanceId));

                assertEquals(instanceId, exception.getJourneyInstanceId());
                verify(journeyInstanceRepository).findById(instanceId);
        }

        @Test
        void shouldThrowJourneyAlreadyCompletedExceptionWhenApplyingEventToCompletedInstance() {
                // Given
                String instanceId = "completed-instance";
                Event event = Event.of("TEST_EVENT");

                JourneyInstance completedInstance =
                                createMockJourneyInstance(instanceId, JourneyStatus.COMPLETED);

                // Mock do método ensureCanReceiveEvents para lançar exceção
                doThrow(new com.luscadevs.journeyorchestrator.domain.exception.JourneyAlreadyCompletedException(
                                instanceId)).when(completedInstance).ensureCanReceiveEvents();

                when(journeyInstanceRepository.findById(instanceId))
                                .thenReturn(Optional.of(completedInstance));

                // When & Then
                JourneyAlreadyCompletedException exception = assertThrows(
                                JourneyAlreadyCompletedException.class,
                                () -> journeyInstanceService.applyEvent(instanceId, event, null));

                assertEquals(instanceId, exception.getJourneyInstanceId());
                verify(journeyInstanceRepository).findById(instanceId);
                verify(journeyEngine, never()).applyEvent(any(JourneyInstance.class),
                                any(JourneyDefinition.class), any(Event.class), any());
        }

        @Test
        void shouldThrowInvalidStateTransitionExceptionWhenEventNotAllowedInCurrentState() {
                // Given
                String instanceId = "instance-with-invalid-transition";
                Event event = Event.of("INVALID_EVENT");

                JourneyInstance instance =
                                createMockJourneyInstance(instanceId, JourneyStatus.RUNNING);
                JourneyDefinition definition = createMockJourneyDefinition();

                when(journeyInstanceRepository.findById(instanceId))
                                .thenReturn(Optional.of(instance));

                when(journeyDefinitionRepository.findByJourneyCodeAndVersion(
                                instance.getJourneyDefinitionId(), instance.getJourneyVersion()))
                                                .thenReturn(Optional.of(definition));

                // Mock engine to throw exception for invalid transition
                doThrow(new InvalidStateTransitionException(instanceId, "CURRENT_STATE",
                                "INVALID_EVENT")).when(journeyEngine).applyEvent(eq(instance),
                                                eq(definition), eq(event), any());

                // When & Then
                InvalidStateTransitionException exception = assertThrows(
                                InvalidStateTransitionException.class,
                                () -> journeyInstanceService.applyEvent(instanceId, event, null));

                assertEquals(instanceId, exception.getJourneyInstanceId());
                assertEquals("CURRENT_STATE", exception.getFromState());
                assertEquals("INVALID_EVENT", exception.getToState());
                verify(journeyInstanceRepository).findById(instanceId);
                verify(journeyDefinitionRepository).findByJourneyCodeAndVersion(
                                instance.getJourneyDefinitionId(), instance.getJourneyVersion());
                verify(journeyEngine).applyEvent(eq(instance), eq(definition), eq(event), any());
        }

        // Helper methods
        private JourneyInstance createMockJourneyInstance(String instanceId, JourneyStatus status) {
                JourneyInstance instance = mock(JourneyInstance.class);
                when(instance.getId()).thenReturn(instanceId);
                when(instance.getStatus()).thenReturn(status);
                when(instance.getJourneyDefinitionId()).thenReturn("TEST_JOURNEY");
                when(instance.getJourneyVersion()).thenReturn(1);

                State mockState = mock(State.class);
                when(mockState.getName()).thenReturn("CURRENT_STATE");
                when(instance.getCurrentState()).thenReturn(mockState);

                return instance;
        }

        private JourneyDefinition createMockJourneyDefinition() {
                JourneyDefinition definition = mock(JourneyDefinition.class);
                State initialState = mock(State.class);
                when(definition.getInitialState()).thenReturn(initialState);
                return definition;
        }
}
