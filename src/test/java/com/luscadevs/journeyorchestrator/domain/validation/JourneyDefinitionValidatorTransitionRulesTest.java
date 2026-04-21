package com.luscadevs.journeyorchestrator.domain.validation;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionValidationException;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinitionStatus;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.StateType;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;

import static org.junit.jupiter.api.Assertions.*;

class JourneyDefinitionValidatorTransitionRulesTest {

        private JourneyDefinitionValidator validator;

        @BeforeEach
        void setUp() {
                validator = new JourneyDefinitionValidator();
        }

        @Test
        @DisplayName("Deve aceitar múltiplas transições com conditions - Regra válida")
        void shouldAcceptMultipleTransitionsWithConditions() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State approvedState =
                                State.builder().name("APPROVED").type(StateType.FINAL).build();
                State rejectedState =
                                State.builder().name("REJECTED").type(StateType.FINAL).build();

                Event evaluateEvent = Event.builder().name("EVALUATE")
                                .description("Evaluate request").build();

                // Múltiplas transições com mesmo (state + event) mas com conditions diferentes
                Transition highScoreTransition = Transition.builder().sourceState(initialState)
                                .targetState(approvedState).event(evaluateEvent)
                                .condition("#eventData.score >= 700").build();

                Transition lowScoreTransition = Transition.builder().sourceState(initialState)
                                .targetState(rejectedState).event(evaluateEvent)
                                .condition("#eventData.score < 700").build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("EVALUATION_JOURNEY").name("Evaluation Journey")
                                .version(1).status(JourneyDefinitionStatus.ATIVA)
                                .initialState(initialState)
                                .states(List.of(initialState, approvedState, rejectedState))
                                .transitions(List.of(highScoreTransition, lowScoreTransition))
                                .createdAt(Instant.now()).build();

                // Act & Assert
                assertDoesNotThrow(() -> validator.validate(definition));
        }

        @Test
        @DisplayName("Deve rejeitar múltiplas transições sem condition - Regra inválida")
        void shouldRejectMultipleTransitionsWithoutCondition() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State approvedState =
                                State.builder().name("APPROVED").type(StateType.FINAL).build();
                State rejectedState =
                                State.builder().name("REJECTED").type(StateType.FINAL).build();

                Event evaluateEvent = Event.builder().name("EVALUATE")
                                .description("Evaluate request").build();

                // Múltiplas transições com mesmo (state + event) sem conditions
                Transition transition1 = Transition.builder().sourceState(initialState)
                                .targetState(approvedState).event(evaluateEvent).build(); // Sem
                                                                                          // condition

                Transition transition2 = Transition.builder().sourceState(initialState)
                                .targetState(rejectedState).event(evaluateEvent).build(); // Sem
                                                                                          // condition

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("INVALID_JOURNEY").name("Invalid Journey").version(1)
                                .status(JourneyDefinitionStatus.ATIVA).initialState(initialState)
                                .states(List.of(initialState, approvedState, rejectedState))
                                .transitions(List.of(transition1, transition2))
                                .createdAt(Instant.now()).build();

                // Act & Assert
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                assertTrue(exception.hasErrorsWithCode(ValidationError.Codes.AMBIGUOUS_TRANSITION));
                assertTrue(exception.getErrors().get(0).getMessage()
                                .contains("Ambiguous transition"));
        }

        @Test
        @DisplayName("Deve rejeitar mix de transições com e sem condition - Regra inválida")
        void shouldRejectMixedTransitionsWithAndWithoutCondition() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State approvedState =
                                State.builder().name("APPROVED").type(StateType.FINAL).build();
                State rejectedState =
                                State.builder().name("REJECTED").type(StateType.FINAL).build();

                Event evaluateEvent = Event.builder().name("EVALUATE")
                                .description("Evaluate request").build();

                // Mix: 1 sem condition + N com condition
                Transition defaultTransition = Transition.builder().sourceState(initialState)
                                .targetState(approvedState).event(evaluateEvent).build(); // Sem
                                                                                          // condition
                                                                                          // (fallback)

                Transition conditionalTransition = Transition.builder().sourceState(initialState)
                                .targetState(rejectedState).event(evaluateEvent)
                                .condition("#eventData.score < 500").build(); // Com condition

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("INVALID_JOURNEY").name("Invalid Journey").version(1)
                                .status(JourneyDefinitionStatus.ATIVA).initialState(initialState)
                                .states(List.of(initialState, approvedState, rejectedState))
                                .transitions(List.of(defaultTransition, conditionalTransition))
                                .createdAt(Instant.now()).build();

                // Act & Assert
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                assertTrue(exception.hasErrorsWithCode(ValidationError.Codes.AMBIGUOUS_TRANSITION));
                assertTrue(exception.getErrors().get(0).getMessage()
                                .contains("Ambiguous transition"));
        }

        @Test
        @DisplayName("Deve aceitar única transição sem condition")
        void shouldAcceptSingleTransitionWithoutCondition() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State finalState = State.builder().name("END").type(StateType.FINAL).build();

                Event proceedEvent = Event.builder().name("PROCEED").description("Proceed").build();

                Transition transition = Transition.builder().sourceState(initialState)
                                .targetState(finalState).event(proceedEvent).build(); // Única
                                                                                      // transição,
                                                                                      // sem
                                                                                      // condition é
                                                                                      // OK

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("SIMPLE_JOURNEY").name("Simple Journey").version(1)
                                .status(JourneyDefinitionStatus.ATIVA).initialState(initialState)
                                .states(List.of(initialState, finalState))
                                .transitions(List.of(transition)).createdAt(Instant.now()).build();

                // Act & Assert
                assertDoesNotThrow(() -> validator.validate(definition));
        }

        @Test
        @DisplayName("Deve aceitar múltiplas transições com eventos diferentes")
        void shouldAcceptMultipleTransitionsWithDifferentEvents() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State approvedState =
                                State.builder().name("APPROVED").type(StateType.FINAL).build();
                State rejectedState =
                                State.builder().name("REJECTED").type(StateType.FINAL).build();

                Event approveEvent = Event.builder().name("APPROVE").description("Approve").build();
                Event rejectEvent = Event.builder().name("REJECT").description("Reject").build();

                // Transições com eventos diferentes no mesmo estado
                Transition approveTransition = Transition.builder().sourceState(initialState)
                                .targetState(approvedState).event(approveEvent).build(); // Sem
                                                                                         // condition

                Transition rejectTransition = Transition.builder().sourceState(initialState)
                                .targetState(rejectedState).event(rejectEvent).build(); // Sem
                                                                                        // condition

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("DECISION_JOURNEY").name("Decision Journey").version(1)
                                .status(JourneyDefinitionStatus.ATIVA).initialState(initialState)
                                .states(List.of(initialState, approvedState, rejectedState))
                                .transitions(List.of(approveTransition, rejectTransition))
                                .createdAt(Instant.now()).build();

                // Act & Assert
                assertDoesNotThrow(() -> validator.validate(definition));
        }

        @Test
        @DisplayName("Deve aceitar transições complexas com múltiplas conditions")
        void shouldAcceptComplexTransitionsWithMultipleConditions() {
                // Arrange
                State initialState =
                                State.builder().name("PENDING").type(StateType.INITIAL).build();
                State approvedState =
                                State.builder().name("APPROVED").type(StateType.FINAL).build();
                State rejectedState =
                                State.builder().name("REJECTED").type(StateType.FINAL).build();
                State manualState = State.builder().name("MANUAL_REVIEW")
                                .type(StateType.INTERMEDIATE).build();

                Event evaluateEvent = Event.builder().name("EVALUATE")
                                .description("Evaluate request").build();

                // 4 transições com mesmo (state + event), todas com conditions
                Transition highScoreTransition = Transition.builder().sourceState(initialState)
                                .targetState(approvedState).event(evaluateEvent)
                                .condition("#eventData.score >= 800 AND #eventData.income > 100000")
                                .build();

                Transition mediumScoreTransition = Transition.builder().sourceState(initialState)
                                .targetState(approvedState).event(evaluateEvent)
                                .condition("#eventData.score >= 700 AND #eventData.income <= 100000")
                                .build();

                Transition lowScoreTransition = Transition.builder().sourceState(initialState)
                                .targetState(rejectedState).event(evaluateEvent)
                                .condition("#eventData.score < 500").build();

                Transition manualTransition = Transition.builder().sourceState(initialState)
                                .targetState(manualState).event(evaluateEvent)
                                .condition("#eventData.score >= 500 AND #eventData.score < 700")
                                .build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("COMPLEX_JOURNEY").name("Complex Journey").version(1)
                                .status(JourneyDefinitionStatus.ATIVA).initialState(initialState)
                                .states(List.of(initialState, approvedState, rejectedState,
                                                manualState))
                                .transitions(List.of(highScoreTransition, mediumScoreTransition,
                                                lowScoreTransition, manualTransition))
                                .createdAt(Instant.now()).build();

                // Act & Assert
                assertDoesNotThrow(() -> validator.validate(definition));
        }
}
