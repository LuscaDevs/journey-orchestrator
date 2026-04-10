package com.luscadevs.journeyorchestrator.domain.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Estrutura de erro de validação pronta para frontend.
 * Contém código, mensagem e campo para facilitar exibição e tratamento.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {

    /**
     * Código do erro para tratamento programático.
     */
    private String code;

    /**
     * Mensagem descritiva para exibição ao usuário.
     */
    private String message;

    /**
     * Campo relacionado ao erro (opcional).
     */
    private String field;

    /**
     * Códigos de erro padrão para validação de JourneyDefinition.
     */
    public static class Codes {
        public static final String INVALID_BASIC_FIELDS = "INVALID_BASIC_FIELDS";
        public static final String DUPLICATE_STATE_NAME = "DUPLICATE_STATE_NAME";
        public static final String INVALID_STATE_TYPE = "INVALID_STATE_TYPE";
        public static final String MISSING_INITIAL_STATE = "MISSING_INITIAL_STATE";
        public static final String MULTIPLE_INITIAL_STATES = "MULTIPLE_INITIAL_STATES";
        public static final String INVALID_INITIAL_STATE = "INVALID_INITIAL_STATE";
        public static final String INVALID_TRANSITION_REFERENCE = "INVALID_TRANSITION_REFERENCE";
        public static final String INVALID_EVENT = "INVALID_EVENT";
        public static final String DUPLICATE_EVENT = "DUPLICATE_EVENT";
        public static final String AMBIGUOUS_TRANSITION = "AMBIGUOUS_TRANSITION";
        public static final String MISSING_FINAL_STATE = "MISSING_FINAL_STATE";
        public static final String FINAL_STATE_WITH_OUTGOING = "FINAL_STATE_WITH_OUTGOING";
        public static final String UNREACHABLE_STATE = "UNREACHABLE_STATE";
        public static final String INVALID_CONDITION = "INVALID_CONDITION";
        public static final String NULL_DEFINITION = "NULL_DEFINITION";
    }

    /**
     * Cria erro de campo básico inválido.
     */
    public static ValidationError ofInvalidBasicField(String field, String message) {
        return ValidationError.builder()
                .code(Codes.INVALID_BASIC_FIELDS)
                .message(message)
                .field(field)
                .build();
    }

    /**
     * Cria erro de estado duplicado.
     */
    public static ValidationError ofDuplicateState(String stateName) {
        return ValidationError.builder()
                .code(Codes.DUPLICATE_STATE_NAME)
                .message("Duplicate state name: " + stateName)
                .field("states")
                .build();
    }

    /**
     * Cria erro de tipo de estado inválido.
     */
    public static ValidationError ofInvalidStateType(String stateName) {
        return ValidationError.builder()
                .code(Codes.INVALID_STATE_TYPE)
                .message("State '" + stateName + "' must have a valid type")
                .field("states")
                .build();
    }

    /**
     * Cria erro de estado inicial ausente.
     */
    public static ValidationError ofMissingInitialState() {
        return ValidationError.builder()
                .code(Codes.MISSING_INITIAL_STATE)
                .message("Journey must have exactly one INITIAL state")
                .field("states")
                .build();
    }

    /**
     * Cria erro de múltiplos estados iniciais.
     */
    public static ValidationError ofMultipleInitialStates(int count) {
        return ValidationError.builder()
                .code(Codes.MULTIPLE_INITIAL_STATES)
                .message("Journey must have exactly one INITIAL state, found: " + count)
                .field("states")
                .build();
    }

    /**
     * Cria erro de estado inicial inválido.
     */
    public static ValidationError ofInvalidInitialState(String message) {
        return ValidationError.builder()
                .code(Codes.INVALID_INITIAL_STATE)
                .message(message)
                .field("initialState")
                .build();
    }

    /**
     * Cria erro de referência de transição inválida.
     */
    public static ValidationError ofInvalidTransitionReference(int transitionIndex, String message) {
        return ValidationError.builder()
                .code(Codes.INVALID_TRANSITION_REFERENCE)
                .message("Transition " + (transitionIndex + 1) + ": " + message)
                .field("transitions[" + transitionIndex + "]")
                .build();
    }

    /**
     * Cria erro de evento inválido.
     */
    public static ValidationError ofInvalidEvent(int transitionIndex, String message) {
        return ValidationError.builder()
                .code(Codes.INVALID_EVENT)
                .message("Transition " + (transitionIndex + 1) + ": " + message)
                .field("transitions[" + transitionIndex + "].event")
                .build();
    }

    /**
     * Cria erro de transição ambígua.
     */
    public static ValidationError ofAmbiguousTransition(String stateName, String eventName) {
        return ValidationError.builder()
                .code(Codes.AMBIGUOUS_TRANSITION)
                .message("Ambiguous transition: state '" + stateName + "' with event '" + eventName + "' has multiple paths without clear conditions")
                .field("transitions")
                .build();
    }

    /**
     * Cria erro de estado final ausente.
     */
    public static ValidationError ofMissingFinalState() {
        return ValidationError.builder()
                .code(Codes.MISSING_FINAL_STATE)
                .message("Journey must have at least one FINAL state")
                .field("states")
                .build();
    }

    /**
     * Cria erro de estado final com transições de saída.
     */
    public static ValidationError ofFinalStateWithOutgoing(String stateName) {
        return ValidationError.builder()
                .code(Codes.FINAL_STATE_WITH_OUTGOING)
                .message("FINAL state '" + stateName + "' should not have outgoing transitions")
                .field("transitions")
                .build();
    }

    /**
     * Cria erro de estado inalcançável.
     */
    public static ValidationError ofUnreachableState(String stateName) {
        return ValidationError.builder()
                .code(Codes.UNREACHABLE_STATE)
                .message("State '" + stateName + "' is unreachable from initial state")
                .field("states")
                .build();
    }

    /**
     * Cria erro de condição inválida.
     */
    public static ValidationError ofInvalidCondition(int transitionIndex, String message) {
        return ValidationError.builder()
                .code(Codes.INVALID_CONDITION)
                .message("Transition " + (transitionIndex + 1) + ": " + message)
                .field("transitions[" + transitionIndex + "].condition")
                .build();
    }

    /**
     * Cria erro de definição nula.
     */
    public static ValidationError ofNullDefinition() {
        return ValidationError.builder()
                .code(Codes.NULL_DEFINITION)
                .message("Journey definition cannot be null")
                .field("definition")
                .build();
    }
}
