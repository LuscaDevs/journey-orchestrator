package com.luscadevs.journeyorchestrator.domain.validation;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinitionStatus;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.StateType;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;

import static org.junit.jupiter.api.Assertions.*;

class JourneyDefinitionValidatorTest {

        private JourneyDefinitionValidator validator;

        @BeforeEach
        void setUp() {
                validator = new JourneyDefinitionValidator();
        }

        @Test
        @DisplayName("Deve validar DSL mínima válida")
        void shouldValidateMinimalValidDSL() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State finalState = State.builder().name("END").type(StateType.FINAL).build();

                Event completeEvent = Event.builder().name("COMPLETE")
                                .description("Complete journey").build();

                Transition transition = Transition.builder().sourceState(initialState)
                                .targetState(finalState).event(completeEvent).build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .status(JourneyDefinitionStatus.ATIVA).initialState(initialState)
                                .states(List.of(initialState, finalState))
                                .transitions(List.of(transition)).createdAt(Instant.now()).build();

                // Act & Assert
                assertDoesNotThrow(() -> validator.validate(definition));
        }

        @Test
        @DisplayName("Deve validar DSL completa válida")
        void shouldValidateCompleteValidDSL() {
                // Arrange
                State initialState =
                                State.builder().name("PENDING").type(StateType.INITIAL).build();
                State intermediateState = State.builder().name("PROCESSING")
                                .type(StateType.INTERMEDIATE).build();
                State finalState = State.builder().name("COMPLETED").type(StateType.FINAL).build();

                Event approveEvent = Event.builder().name("APPROVE").description("Approve request")
                                .build();
                Event processEvent = Event.builder().name("PROCESS").description("Process request")
                                .build();

                Transition transition1 = Transition.builder().sourceState(initialState)
                                .targetState(intermediateState).event(approveEvent)
                                .condition("#eventData.amount > 1000").build();

                Transition transition2 = Transition.builder().sourceState(intermediateState)
                                .targetState(finalState).event(processEvent).build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("LOAN_APPROVAL").name("Loan Approval Journey")
                                .version(1).status(JourneyDefinitionStatus.ATIVA)
                                .initialState(initialState)
                                .states(List.of(initialState, intermediateState, finalState))
                                .transitions(List.of(transition1, transition2))
                                .createdAt(Instant.now()).build();

                // Act & Assert
                assertDoesNotThrow(() -> validator.validate(definition));
        }

        @Test
        @DisplayName("Deve validar DSL com múltiplos caminhos")
        void shouldValidateDSLWithMultiplePaths() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State approvedState =
                                State.builder().name("APPROVED").type(StateType.FINAL).build();
                State rejectedState =
                                State.builder().name("REJECTED").type(StateType.FINAL).build();

                Event approveEvent = Event.builder().name("APPROVE").description("Approve").build();
                Event rejectEvent = Event.builder().name("REJECT").description("Reject").build();

                Transition approveTransition = Transition.builder().sourceState(initialState)
                                .targetState(approvedState).event(approveEvent).build();

                Transition rejectTransition = Transition.builder().sourceState(initialState)
                                .targetState(rejectedState).event(rejectEvent).build();

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
        @DisplayName("Deve validar DSL com transições condicionais complexas")
        void shouldValidateDSLWithComplexConditions() {
                // Arrange
                State initialState = State.builder().name("REVIEW").type(StateType.INITIAL).build();
                State approvedState =
                                State.builder().name("APPROVED").type(StateType.FINAL).build();
                State rejectedState =
                                State.builder().name("REJECTED").type(StateType.FINAL).build();

                Event approveEvent = Event.builder().name("APPROVE").description("Approve request")
                                .build();
                Event rejectEvent = Event.builder().name("REJECT").description("Reject request")
                                .build();

                Transition approveTransition = Transition.builder().sourceState(initialState)
                                .targetState(approvedState).event(approveEvent)
                                .condition("#eventData.score >= 700 AND #eventData.income > 50000")
                                .build();

                Transition rejectTransition = Transition.builder().sourceState(initialState)
                                .targetState(rejectedState).event(rejectEvent)
                                .condition("#eventData.score < 700 OR #eventData.income <= 50000")
                                .build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("CREDIT_ANALYSIS").name("Credit Analysis Journey")
                                .version(2).status(JourneyDefinitionStatus.ATIVA)
                                .initialState(initialState)
                                .states(List.of(initialState, approvedState, rejectedState))
                                .transitions(List.of(approveTransition, rejectTransition))
                                .createdAt(Instant.now()).build();

                // Act & Assert
                assertDoesNotThrow(() -> validator.validate(definition));
        }

        @Test
        @DisplayName("Deve validar DSL com estados intermediários múltiplos")
        void shouldValidateDSLWithMultipleIntermediateStates() {
                // Arrange
                State initialState =
                                State.builder().name("SUBMITTED").type(StateType.INITIAL).build();
                State reviewState =
                                State.builder().name("REVIEW").type(StateType.INTERMEDIATE).build();
                State analysisState = State.builder().name("ANALYSIS").type(StateType.INTERMEDIATE)
                                .build();
                State approvalState = State.builder().name("APPROVAL").type(StateType.INTERMEDIATE)
                                .build();
                State finalState = State.builder().name("COMPLETED").type(StateType.FINAL).build();

                Event submitEvent = Event.builder().name("SUBMIT").description("Submit for review")
                                .build();
                Event reviewEvent = Event.builder().name("REVIEW_COMPLETE")
                                .description("Review completed").build();
                Event analysisEvent = Event.builder().name("ANALYSIS_COMPLETE")
                                .description("Analysis completed").build();
                Event approvalEvent = Event.builder().name("APPROVAL_COMPLETE")
                                .description("Approval completed").build();

                List<Transition> transitions = List.of(
                                Transition.builder().sourceState(initialState)
                                                .targetState(reviewState).event(submitEvent)
                                                .build(),
                                Transition.builder().sourceState(reviewState)
                                                .targetState(analysisState).event(reviewEvent)
                                                .build(),
                                Transition.builder().sourceState(analysisState)
                                                .targetState(approvalState).event(analysisEvent)
                                                .build(),
                                Transition.builder().sourceState(approvalState)
                                                .targetState(finalState).event(approvalEvent)
                                                .build());

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("COMPLEX_JOURNEY").name("Complex Multi-step Journey")
                                .version(1).status(JourneyDefinitionStatus.ATIVA)
                                .initialState(initialState)
                                .states(List.of(initialState, reviewState, analysisState,
                                                approvalState, finalState))
                                .transitions(transitions).createdAt(Instant.now()).build();

                // Act & Assert
                assertDoesNotThrow(() -> validator.validate(definition));
        }
}
