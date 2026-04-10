package com.luscadevs.journeyorchestrator.domain.validation;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionValidationException;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.StateType;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;

import static org.junit.jupiter.api.Assertions.*;

class JourneyDefinitionValidatorInvalidTest {

        private JourneyDefinitionValidator validator;

        @BeforeEach
        void setUp() {
                validator = new JourneyDefinitionValidator();
        }

        @Test
        @DisplayName("Deve rejeitar definição nula")
        void shouldRejectNullDefinition() {
                // Act & Assert
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(null));

                assertTrue(exception.getErrorMessages()
                                .contains("Journey definition cannot be null"));
        }

        @Test
        @DisplayName("Deve rejeitar campos básicos inválidos")
        void shouldRejectInvalidBasicFields() {
                // Arrange
                JourneyDefinition definition =
                                JourneyDefinition.builder().id("test-id").journeyCode("") // Código
                                                                                          // vazio
                                                .name("") // Nome vazio
                                                .version(0) // Versão inválida
                                                .active(true).initialState(null).states(null)
                                                .transitions(null).createdAt(Instant.now()).build();

                // Act & Assert
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                List<String> errors = exception.getErrorMessages();
                assertTrue(errors.contains("Journey code is required"));
                assertTrue(errors.contains("Journey name is required"));
                assertTrue(errors.contains("Journey version must be a positive integer"));
        }

        @Test
        @DisplayName("Deve rejeitar estados duplicados")
        void shouldRejectDuplicateStates() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State duplicateState = State.builder().name("START").type(StateType.FINAL).build(); // Nome
                                                                                                    // duplicado

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .active(true).initialState(initialState)
                                .states(List.of(initialState, duplicateState)).transitions(null)
                                .createdAt(Instant.now()).build();

                // Act & Assert
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                assertTrue(exception.hasErrorsContaining("Duplicate state name"));
        }

        @Test
        @DisplayName("Deve rejeitar múltiplos estados iniciais")
        void shouldRejectMultipleInitialStates() {
                // Arrange
                State initialState1 =
                                State.builder().name("START1").type(StateType.INITIAL).build();
                State initialState2 =
                                State.builder().name("START2").type(StateType.INITIAL).build();
                State finalState = State.builder().name("END").type(StateType.FINAL).build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .active(true).initialState(initialState1)
                                .states(List.of(initialState1, initialState2, finalState))
                                .transitions(null).createdAt(Instant.now()).build();

                // Act & Assert
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                assertTrue(exception.hasErrorsContaining("exactly one INITIAL state"));
        }

        @Test
        @DisplayName("Deve rejeitar estado inicial inexistente")
        void shouldRejectNonExistentInitialState() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State finalState = State.builder().name("END").type(StateType.FINAL).build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .active(true)
                                .initialState(State.builder().name("UNKNOWN")
                                                .type(StateType.INITIAL).build()) // Estado não
                                                                                  // existe
                                .states(List.of(initialState, finalState)).transitions(null)
                                .createdAt(Instant.now()).build();

                // Act & Assert
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                assertTrue(exception.hasErrorsContaining("must be included in states list"));
        }

        @Test
        @DisplayName("Deve rejeitar transição com estado inexistente")
        void shouldRejectTransitionWithNonExistentState() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State finalState = State.builder().name("END").type(StateType.FINAL).build();
                State unknownState = State.builder().name("UNKNOWN").type(StateType.INTERMEDIATE)
                                .build();

                Event event = Event.builder().name("PROCEED").description("Proceed").build();

                Transition transition = Transition.builder().sourceState(initialState)
                                .targetState(unknownState) // Estado não existe na lista
                                .event(event).build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .active(true).initialState(initialState)
                                .states(List.of(initialState, finalState)) // unknownState não está
                                                                           // aqui
                                .transitions(List.of(transition)).createdAt(Instant.now()).build();

                // Act & Assert
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                assertTrue(exception.hasErrorsContaining("does not exist"));
        }

        @Test
        @DisplayName("Deve rejeitar eventos duplicados no mesmo estado sem conditions")
        void shouldRejectDuplicateEventsInSameStateWithoutConditions() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State finalState = State.builder().name("END").type(StateType.FINAL).build();

                Event event = Event.builder().name("PROCEED").description("Proceed").build();

                Transition transition1 = Transition.builder().sourceState(initialState)
                                .targetState(finalState).event(event).build(); // Sem condition
                Transition transition2 = Transition.builder().sourceState(initialState)
                                .targetState(finalState).event(event).build(); // Sem condition

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .active(true).initialState(initialState)
                                .states(List.of(initialState, finalState))
                                .transitions(List.of(transition1, transition2))
                                .createdAt(Instant.now()).build();

                // Act & Assert
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                assertTrue(exception.hasErrorsWithCode(ValidationError.Codes.AMBIGUOUS_TRANSITION));
        }

        @Test
        @DisplayName("Deve rejeitar estado inalcançável")
        void shouldRejectUnreachableState() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State reachableState = State.builder().name("REACHABLE")
                                .type(StateType.INTERMEDIATE).build();
                State unreachableState =
                                State.builder().name("UNREACHABLE").type(StateType.FINAL).build();

                Event event = Event.builder().name("PROCEED").description("Proceed").build();

                Transition transition = Transition.builder().sourceState(initialState)
                                .targetState(reachableState).event(event).build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .active(true).initialState(initialState)
                                .states(List.of(initialState, reachableState, unreachableState))
                                .transitions(List.of(transition)).createdAt(Instant.now()).build();

                // Act & Assert
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                assertTrue(exception.hasErrorsContaining("unreachable"));
        }

        @Test
        @DisplayName("Deve rejeitar ausência de estado final")
        void shouldRejectMissingFinalState() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State intermediateState =
                                State.builder().name("MIDDLE").type(StateType.INTERMEDIATE).build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .active(true).initialState(initialState)
                                .states(List.of(initialState, intermediateState)).transitions(null)
                                .createdAt(Instant.now()).build();

                // Act & Assert
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                assertTrue(exception.hasErrorsContaining("at least one FINAL state"));
        }

        @Test
        @DisplayName("Deve rejeitar transição de estado final")
        void shouldRejectTransitionFromFinalState() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State finalState = State.builder().name("END").type(StateType.FINAL).build();
                State anotherState = State.builder().name("ANOTHER").type(StateType.INTERMEDIATE)
                                .build();

                Event event = Event.builder().name("PROCEED").description("Proceed").build();

                Transition invalidTransition = Transition.builder().sourceState(finalState) // Estado
                                                                                            // final
                                                                                            // não
                                                                                            // deve
                                                                                            // ter
                                                                                            // transições
                                                                                            // de
                                                                                            // saída
                                .targetState(anotherState).event(event).build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .active(true).initialState(initialState)
                                .states(List.of(initialState, finalState, anotherState))
                                .transitions(List.of(invalidTransition)).createdAt(Instant.now())
                                .build();

                // Act & Assert
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                assertTrue(exception.hasErrorsContaining("FINAL state"));
                assertTrue(exception.hasErrorsContaining("should not have outgoing transitions"));
        }

        @Test
        @DisplayName("Deve rejeitar condição inválida")
        void shouldRejectInvalidCondition() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State finalState = State.builder().name("END").type(StateType.FINAL).build();

                Event event = Event.builder().name("PROCEED").description("Proceed").build();

                Transition transition = Transition.builder().sourceState(initialState)
                                .targetState(finalState).event(event)
                                .condition("/* invalid condition */") // Contém caracteres inválidos
                                .build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .active(true).initialState(initialState)
                                .states(List.of(initialState, finalState))
                                .transitions(List.of(transition)).createdAt(Instant.now()).build();

                // Act & Assert
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                assertTrue(exception.hasErrorsContaining("invalid characters"));
        }
}
