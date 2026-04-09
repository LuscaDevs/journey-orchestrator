package com.luscadevs.journeyorchestrator.api.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.luscadevs.journey.api.generated.model.CreateJourneyDefinitionRequest;
import com.luscadevs.journey.api.generated.model.State;
import com.luscadevs.journey.api.generated.model.StateType;
import com.luscadevs.journey.api.generated.model.TransitionRequest;
import com.luscadevs.journeyorchestrator.application.port.ConditionEvaluatorPort;
import com.luscadevs.journeyorchestrator.domain.exception.InvalidConditionSyntaxException;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;

import static org.mockito.Mockito.*;

class JourneyDefinitionMapperTest {

        @Mock
        private ConditionEvaluatorPort conditionEvaluator;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                JourneyDefinitionMapper.setConditionEvaluator(conditionEvaluator);
        }

        @Test
        void shouldThrowExceptionForInvalidConditionSyntax() {
                // Given
                String invalidCondition =
                                "#eventData.riskScore <= asd 50 AND #eventData.income > 3000";
                when(conditionEvaluator.validateExpression(invalidCondition)).thenReturn(false);

                CreateJourneyDefinitionRequest request = new CreateJourneyDefinitionRequest()
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .states(List.of(new State().name("START").type(StateType.INITIAL),
                                                new State().name("END").type(StateType.FINAL)))
                                .transitions(List.of(new TransitionRequest().source("START")
                                                .target("END").event("COMPLETE")
                                                .condition(invalidCondition)));

                // When & Then
                InvalidConditionSyntaxException exception =
                                assertThrows(InvalidConditionSyntaxException.class,
                                                () -> JourneyDefinitionMapper.toDomain(request));

                assertEquals(invalidCondition, exception.getCondition());
                assertTrue(exception.getMessage().contains("Invalid condition syntax"));
                verify(conditionEvaluator).validateExpression(invalidCondition);
        }

        @Test
        void shouldAcceptValidConditionSyntax() {
                // Given
                String validCondition = "#eventData.riskScore <= 50 AND #eventData.income > 3000";
                when(conditionEvaluator.validateExpression(validCondition)).thenReturn(true);

                CreateJourneyDefinitionRequest request = new CreateJourneyDefinitionRequest()
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .states(List.of(new State().name("START").type(StateType.INITIAL),
                                                new State().name("END").type(StateType.FINAL)))
                                .transitions(List.of(new TransitionRequest().source("START")
                                                .target("END").event("COMPLETE")
                                                .condition(validCondition)));

                // When
                JourneyDefinition result = JourneyDefinitionMapper.toDomain(request);

                // Then
                assertNotNull(result);
                assertEquals("TEST_JOURNEY", result.getJourneyCode());
                assertEquals(1, result.getTransitions().size());
                assertEquals(validCondition, result.getTransitions().get(0).getCondition());
                verify(conditionEvaluator).validateExpression(validCondition);
        }

        @Test
        void shouldAcceptNullCondition() {
                // Given
                CreateJourneyDefinitionRequest request = new CreateJourneyDefinitionRequest()
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .states(List.of(new State().name("START").type(StateType.INITIAL),
                                                new State().name("END").type(StateType.FINAL)))
                                .transitions(List.of(new TransitionRequest().source("START")
                                                .target("END").event("COMPLETE").condition(null) // No
                                                                                                 // condition
                                ));

                // When
                JourneyDefinition result = JourneyDefinitionMapper.toDomain(request);

                // Then
                assertNotNull(result);
                assertEquals(1, result.getTransitions().size());
                assertNull(result.getTransitions().get(0).getCondition());
        }

        @Test
        void shouldAcceptEmptyCondition() {
                // Given
                CreateJourneyDefinitionRequest request = new CreateJourneyDefinitionRequest()
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .states(List.of(new State().name("START").type(StateType.INITIAL),
                                                new State().name("END").type(StateType.FINAL)))
                                .transitions(List.of(new TransitionRequest().source("START")
                                                .target("END").event("COMPLETE").condition("") // Empty
                                                                                               // condition
                                ));

                // When
                JourneyDefinition result = JourneyDefinitionMapper.toDomain(request);

                // Then
                assertNotNull(result);
                assertEquals(1, result.getTransitions().size());
                assertEquals("", result.getTransitions().get(0).getCondition());
        }
}
