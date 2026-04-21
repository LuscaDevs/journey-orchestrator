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

class JourneyDefinitionValidatorStructuredErrorsTest {

        private JourneyDefinitionValidator validator;

        @BeforeEach
        void setUp() {
                validator = new JourneyDefinitionValidator();
        }

        @Test
        @DisplayName("Deve retornar erros estruturados com código, mensagem e campo")
        void shouldReturnStructuredErrorsWithCodeMessageAndField() {
                // Arrange
                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("") // Código vazio
                                .name("") // Nome vazio
                                .version(0) // Versão inválida
                                .status(JourneyDefinitionStatus.ATIVA).initialState(null)
                                .states(null).transitions(null).createdAt(Instant.now()).build();

                // Act
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                // Assert
                List<ValidationError> errors = exception.getErrors();
                assertEquals(3, errors.size());

                // Verificar erro de journeyCode
                ValidationError journeyCodeError = errors.stream()
                                .filter(e -> ValidationError.Codes.INVALID_BASIC_FIELDS
                                                .equals(e.getCode()))
                                .filter(e -> "journeyCode".equals(e.getField())).findFirst()
                                .orElse(null);

                assertNotNull(journeyCodeError);
                assertEquals("Journey code is required", journeyCodeError.getMessage());
                assertEquals("journeyCode", journeyCodeError.getField());
                assertEquals(ValidationError.Codes.INVALID_BASIC_FIELDS,
                                journeyCodeError.getCode());

                // Verificar erro de name
                ValidationError nameError = errors.stream()
                                .filter(e -> ValidationError.Codes.INVALID_BASIC_FIELDS
                                                .equals(e.getCode()))
                                .filter(e -> "name".equals(e.getField())).findFirst().orElse(null);

                assertNotNull(nameError);
                assertEquals("Journey name is required", nameError.getMessage());
                assertEquals("name", nameError.getField());

                // Verificar erro de version
                ValidationError versionError = errors.stream()
                                .filter(e -> ValidationError.Codes.INVALID_BASIC_FIELDS
                                                .equals(e.getCode()))
                                .filter(e -> "version".equals(e.getField())).findFirst()
                                .orElse(null);

                assertNotNull(versionError);
                assertEquals("Journey version must be a positive integer",
                                versionError.getMessage());
                assertEquals("version", versionError.getField());
        }

        @Test
        @DisplayName("Deve retornar erro estruturado para estado duplicado")
        void shouldReturnStructuredErrorForDuplicateState() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State duplicateState = State.builder().name("START").type(StateType.FINAL).build(); // Nome
                                                                                                    // duplicado
                State finalState = State.builder().name("END").type(StateType.FINAL).build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .status(JourneyDefinitionStatus.ATIVA).initialState(initialState)
                                .states(List.of(initialState, duplicateState, finalState))
                                .transitions(null).createdAt(Instant.now()).build();

                // Act
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                // Assert
                ValidationError error = exception.getErrors().get(0);
                assertEquals(ValidationError.Codes.DUPLICATE_STATE_NAME, error.getCode());
                assertEquals("Duplicate state name: START", error.getMessage());
                assertEquals("states", error.getField());
        }

        @Test
        @DisplayName("Deve retornar erro estruturado para transição ambígua")
        void shouldReturnStructuredErrorForAmbiguousTransition() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State approvedState =
                                State.builder().name("APPROVED").type(StateType.FINAL).build();
                State rejectedState =
                                State.builder().name("REJECTED").type(StateType.FINAL).build();

                Event evaluateEvent = Event.builder().name("EVALUATE")
                                .description("Evaluate request").build();

                // Múltiplas transições sem condition (ambíguo)
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

                // Act
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                // Assert
                ValidationError error = exception.getErrors().get(0);
                assertEquals(ValidationError.Codes.AMBIGUOUS_TRANSITION, error.getCode());
                assertTrue(error.getMessage().contains("Ambiguous transition"));
                assertTrue(error.getMessage().contains("START"));
                assertTrue(error.getMessage().contains("EVALUATE"));
                assertEquals("transitions", error.getField());
        }

        @Test
        @DisplayName("Deve retornar erro estruturado para estado inalcançável")
        void shouldReturnStructuredErrorForUnreachableState() {
                // Arrange
                State initialState = State.builder().name("START").type(StateType.INITIAL).build();
                State reachableState =
                                State.builder().name("REACHABLE").type(StateType.FINAL).build();
                State unreachableState =
                                State.builder().name("UNREACHABLE").type(StateType.FINAL).build();

                Event event = Event.builder().name("PROCEED").description("Proceed").build();

                Transition transition = Transition.builder().sourceState(initialState)
                                .targetState(reachableState).event(event).build();

                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("TEST_JOURNEY").name("Test Journey").version(1)
                                .status(JourneyDefinitionStatus.ATIVA).initialState(initialState)
                                .states(List.of(initialState, reachableState, unreachableState))
                                .transitions(List.of(transition)).createdAt(Instant.now()).build();

                // Act
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                // Assert
                ValidationError error = exception.getErrors().get(0);
                assertEquals(ValidationError.Codes.UNREACHABLE_STATE, error.getCode());
                assertEquals("State 'UNREACHABLE' is unreachable from initial state",
                                error.getMessage());
                assertEquals("states", error.getField());
        }

        @Test
        @DisplayName("Deve manter compatibilidade com métodos legados")
        void shouldMaintainLegacyCompatibility() {
                // Arrange
                JourneyDefinition definition = JourneyDefinition.builder().id("test-id")
                                .journeyCode("") // Código vazio
                                .name("") // Nome vazio
                                .version(0) // Versão inválida
                                .status(JourneyDefinitionStatus.ATIVA).initialState(null)
                                .states(null).transitions(null).createdAt(Instant.now()).build();

                // Act
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(definition));

                // Assert - Verificar métodos legados ainda funcionam
                List<String> errorMessages = exception.getErrorMessages();
                assertTrue(errorMessages.contains("Journey code is required"));
                assertTrue(errorMessages.contains("Journey name is required"));
                assertTrue(errorMessages.contains("Journey version must be a positive integer"));

                // Verificar hasErrorsContaining ainda funciona
                assertTrue(exception.hasErrorsContaining("Journey code"));
                assertTrue(exception.hasErrorsContaining("required"));

                // Verificar hasErrorsWithCode funciona
                assertTrue(exception.hasErrorsWithCode(ValidationError.Codes.INVALID_BASIC_FIELDS));

                // Verificar getFormattedErrors funciona
                String formattedErrors = exception.getFormattedErrors();
                assertTrue(formattedErrors.contains("Journey definition validation errors:"));
                assertTrue(formattedErrors.contains("[INVALID_BASIC_FIELDS]"));
        }

        @Test
        @DisplayName("Deve retornar erro estruturado para definição nula")
        void shouldReturnStructuredErrorForNullDefinition() {
                // Act
                JourneyDefinitionValidationException exception =
                                assertThrows(JourneyDefinitionValidationException.class,
                                                () -> validator.validate(null));

                // Assert
                ValidationError error = exception.getErrors().get(0);
                assertEquals(ValidationError.Codes.NULL_DEFINITION, error.getCode());
                assertEquals("Journey definition cannot be null", error.getMessage());
                assertEquals("definition", error.getField());
        }
}
