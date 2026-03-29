package com.luscadevs.journeyorchestrator.adapters.in.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import com.luscadevs.journeyorchestrator.api.controller.JourneyInstanceController;
import com.luscadevs.journeyorchestrator.api.dto.TransitionHistoryListResponse;
import com.luscadevs.journeyorchestrator.application.service.JourneyInstanceService;
import com.luscadevs.journeyorchestrator.application.service.TransitionHistoryService;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistory;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistoryEventId;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JourneyInstanceControllerTest {

        @Mock
        private JourneyInstanceService journeyInstanceService;

        @Mock
        private TransitionHistoryService transitionHistoryService;

        @Mock
        private com.luscadevs.journeyorchestrator.api.mapper.TransitionHistoryMapper transitionHistoryMapper;

        private JourneyInstanceController controller;

        @BeforeEach
        void setUp() {
                controller = new JourneyInstanceController(journeyInstanceService, transitionHistoryService,
                                transitionHistoryMapper);
        }

        @Test
        void shouldGetTransitionHistorySuccessfully() {
                // Given
                String instanceId = "test-instance-id";

                // Mock journey instance exists
                JourneyInstance mockInstance = JourneyInstance.builder()
                                .id(instanceId)
                                .journeyDefinitionId("test-journey")
                                .journeyVersion(1)
                                .currentState(State.builder().name("PROCESSING").build())
                                .build();
                when(journeyInstanceService.getInstance(instanceId)).thenReturn(mockInstance);

                // Mock transition history
                List<TransitionHistory> historyEvents = List.of(
                                TransitionHistory.builder()
                                                .id(TransitionHistoryEventId.generate())
                                                .instanceId(instanceId)
                                                .fromState(State.builder().name("START").build())
                                                .toState(State.builder().name("PROCESSING").build())
                                                .event(Event.builder().name("PROCESS").build())
                                                .timestamp(Instant.now().minusSeconds(60))
                                                .metadata(Map.of("key", "value"))
                                                .build());
                when(transitionHistoryService.getTransitionHistory(instanceId)).thenReturn(historyEvents);

                // Mock mapper response
                TransitionHistoryListResponse expectedResponse = TransitionHistoryListResponse.builder()
                                .instanceId(instanceId)
                                .events(List.of())
                                .totalCount(1L)
                                .build();
                when(transitionHistoryMapper.toListResponse(eq(instanceId), any(), anyInt(), anyInt(), anyLong()))
                                .thenReturn(expectedResponse);

                // When
                ResponseEntity<TransitionHistoryListResponse> response = controller
                                .getJourneyInstanceTransitionHistory(instanceId, null, null, null, 100, 0);

                // Then
                assertTrue(response.getStatusCode().is2xxSuccessful());
                assertEquals(expectedResponse, response.getBody());

                verify(journeyInstanceService).getInstance(instanceId);
                verify(transitionHistoryService).getTransitionHistory(instanceId);
                verify(transitionHistoryMapper).toListResponse(eq(instanceId), any(), eq(100), eq(0), eq(1L));
        }

        @Test
        void shouldThrowExceptionWhenInstanceNotFound() {
                // Given
                String instanceId = "non-existent-instance";
                when(journeyInstanceService.getInstance(instanceId))
                                .thenThrow(new RuntimeException("Journey instance not found: " + instanceId));

                // When & Then
                assertThrows(RuntimeException.class,
                                () -> controller.getJourneyInstanceTransitionHistory(instanceId, null, null, null, 100,
                                                0));

                verify(journeyInstanceService).getInstance(instanceId);
                verifyNoInteractions(transitionHistoryService);
                verifyNoInteractions(transitionHistoryMapper);
        }

        @Test
        void shouldGetTransitionHistoryWithDateRangeFilter() {
                // Given
                String instanceId = "test-instance-id";
                Instant from = Instant.now().minusSeconds(3600);
                Instant to = Instant.now();

                // Mock journey instance exists
                JourneyInstance mockInstance = JourneyInstance.builder()
                                .id(instanceId)
                                .journeyDefinitionId("test-journey")
                                .journeyVersion(1)
                                .currentState(State.builder().name("PROCESSING").build())
                                .build();
                when(journeyInstanceService.getInstance(instanceId)).thenReturn(mockInstance);

                // Mock filtered transition history
                List<TransitionHistory> historyEvents = List.of(
                                TransitionHistory.builder()
                                                .id(TransitionHistoryEventId.generate())
                                                .instanceId(instanceId)
                                                .fromState(State.builder().name("START").build())
                                                .toState(State.builder().name("PROCESSING").build())
                                                .event(Event.builder().name("PROCESS").build())
                                                .timestamp(Instant.now().minusSeconds(1800))
                                                .metadata(Map.of())
                                                .build());
                when(transitionHistoryService.getTransitionHistory(instanceId, from, to)).thenReturn(historyEvents);

                // Mock mapper response
                TransitionHistoryListResponse expectedResponse = TransitionHistoryListResponse.builder()
                                .instanceId(instanceId)
                                .events(List.of())
                                .totalCount(1L)
                                .build();
                when(transitionHistoryMapper.toListResponse(eq(instanceId), any(), anyInt(), anyInt(), anyLong()))
                                .thenReturn(expectedResponse);

                // When
                ResponseEntity<TransitionHistoryListResponse> response = controller
                                .getJourneyInstanceTransitionHistory(instanceId, from, to, null, 50, 0);

                // Then
                assertTrue(response.getStatusCode().is2xxSuccessful());
                assertEquals(expectedResponse, response.getBody());

                verify(journeyInstanceService).getInstance(instanceId);
                verify(transitionHistoryService).getTransitionHistory(instanceId, from, to);
                verify(transitionHistoryMapper).toListResponse(eq(instanceId), any(), eq(50), eq(0), eq(1L));
        }

        @Test
        void shouldGetTransitionHistoryWithEventTypeFilter() {
                // Given
                String instanceId = "test-instance-id";
                String eventType = "PROCESS";

                // Mock journey instance exists
                JourneyInstance mockInstance = JourneyInstance.builder()
                                .id(instanceId)
                                .journeyDefinitionId("test-journey")
                                .journeyVersion(1)
                                .currentState(State.builder().name("PROCESSING").build())
                                .build();
                when(journeyInstanceService.getInstance(instanceId)).thenReturn(mockInstance);

                // Mock filtered transition history
                List<TransitionHistory> historyEvents = List.of(
                                TransitionHistory.builder()
                                                .id(TransitionHistoryEventId.generate())
                                                .instanceId(instanceId)
                                                .fromState(State.builder().name("START").build())
                                                .toState(State.builder().name("PROCESSING").build())
                                                .event(Event.builder().name(eventType).build())
                                                .timestamp(Instant.now().minusSeconds(1800))
                                                .metadata(Map.of())
                                                .build());
                when(transitionHistoryService.getTransitionHistoryByEventType(instanceId, eventType))
                                .thenReturn(historyEvents);

                // Mock mapper response
                TransitionHistoryListResponse expectedResponse = TransitionHistoryListResponse.builder()
                                .instanceId(instanceId)
                                .events(List.of())
                                .totalCount(1L)
                                .build();
                when(transitionHistoryMapper.toListResponse(eq(instanceId), any(), anyInt(), anyInt(), anyLong()))
                                .thenReturn(expectedResponse);

                // When
                ResponseEntity<TransitionHistoryListResponse> response = controller
                                .getJourneyInstanceTransitionHistory(instanceId, null, null, eventType, 25, 0);

                // Then
                assertTrue(response.getStatusCode().is2xxSuccessful());
                assertEquals(expectedResponse, response.getBody());

                verify(journeyInstanceService).getInstance(instanceId);
                verify(transitionHistoryService).getTransitionHistoryByEventType(instanceId, eventType);
                verify(transitionHistoryMapper).toListResponse(eq(instanceId), any(), eq(25), eq(0), eq(1L));
        }
}
