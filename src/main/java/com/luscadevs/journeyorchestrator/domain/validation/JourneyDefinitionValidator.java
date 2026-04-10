package com.luscadevs.journeyorchestrator.domain.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionValidationException;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.StateType;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;

/**
 * Validador robusto para JourneyDefinition que garante consistência estrutural da DSL antes da
 * execução.
 */
@Component
public class JourneyDefinitionValidator {

    /**
     * Valida uma JourneyDefinition completa.
     * 
     * @param definition definição a ser validada
     * @throws JourneyDefinitionValidationException se houver erros de validação
     */
    public void validate(JourneyDefinition definition) {
        List<ValidationError> errors = new ArrayList<>();

        if (definition == null) {
            errors.add(ValidationError.ofNullDefinition());
            throw new JourneyDefinitionValidationException(errors);
        }

        // Validações básicas
        validateBasicFields(definition, errors);

        if (!errors.isEmpty()) {
            throw new JourneyDefinitionValidationException(errors);
        }

        // Validações estruturais
        validateStates(definition, errors);
        validateTransitions(definition, errors);
        validateInitialState(definition, errors);
        validateFinalStates(definition, errors);
        validateReachableStates(definition, errors);
        validateConditions(definition, errors);

        if (!errors.isEmpty()) {
            throw new JourneyDefinitionValidationException(errors);
        }
    }

    /**
     * Valida campos básicos obrigatórios.
     */
    private void validateBasicFields(JourneyDefinition definition, List<ValidationError> errors) {
        if (definition.getJourneyCode() == null || definition.getJourneyCode().trim().isEmpty()) {
            errors.add(
                    ValidationError.ofInvalidBasicField("journeyCode", "Journey code is required"));
        }

        if (definition.getName() == null || definition.getName().trim().isEmpty()) {
            errors.add(ValidationError.ofInvalidBasicField("name", "Journey name is required"));
        }

        if (definition.getVersion() == null || definition.getVersion() < 1) {
            errors.add(ValidationError.ofInvalidBasicField("version",
                    "Journey version must be a positive integer"));
        }
    }

    /**
     * Valida a lista de estados.
     */
    private void validateStates(JourneyDefinition definition, List<ValidationError> errors) {
        List<State> states = definition.getStates();

        if (states == null || states.isEmpty()) {
            errors.add(ValidationError.ofInvalidBasicField("states",
                    "Journey must have at least one state"));
            return;
        }

        // Verificar nomes duplicados
        Set<String> stateNames = new HashSet<>();
        Set<String> duplicateNames = new HashSet<>();

        // Verificar IDs duplicados
        Set<java.util.UUID> stateIds = new HashSet<>();
        Set<java.util.UUID> duplicateIds = new HashSet<>();

        for (State state : states) {
            if (state.getName() == null || state.getName().trim().isEmpty()) {
                errors.add(ValidationError.ofInvalidBasicField("states",
                        "State name cannot be null or empty"));
                continue;
            }

            if (!stateNames.add(state.getName())) {
                duplicateNames.add(state.getName());
            }

            // Validar tipo do estado
            if (state.getType() == null) {
                errors.add(ValidationError.ofInvalidStateType(state.getName()));
            }

            // Validar duplicidade de ID (se fornecido)
            if (state.getId() != null) {
                if (!stateIds.add(state.getId())) {
                    duplicateIds.add(state.getId());
                }
            }
        }

        // Reportar duplicatas de nome
        for (String duplicate : duplicateNames) {
            errors.add(ValidationError.ofDuplicateState(duplicate));
        }

        // Reportar duplicatas de ID
        for (java.util.UUID duplicate : duplicateIds) {
            errors.add(
                    ValidationError.builder().code("DUPLICATE_STATE_ID")
                            .message("Duplicate state ID: " + duplicate
                                    + ". Each state must have a unique ID.")
                            .field("states").build());
        }

        // Validar que há exatamente um estado inicial
        List<State> initialStates = states.stream()
                .filter(s -> StateType.INITIAL.equals(s.getType())).collect(Collectors.toList());

        if (initialStates.isEmpty()) {
            errors.add(ValidationError.ofMissingInitialState());
        } else if (initialStates.size() > 1) {
            errors.add(ValidationError.ofMultipleInitialStates(initialStates.size()));
        }
    }

    /**
     * Valida a lista de transições com regra state+event+condition.
     */
    private void validateTransitions(JourneyDefinition definition, List<ValidationError> errors) {
        List<Transition> transitions = definition.getTransitions();
        List<State> states = definition.getStates();

        if (transitions == null) {
            // Transições nulas são permitidas (jornada sem transições)
            return;
        }

        // Criar mapa de estados para busca rápida
        Map<String, State> stateMap = new HashMap<>();
        Map<java.util.UUID, State> stateIdMap = new HashMap<>();

        for (State state : states) {
            if (state.getName() != null) {
                stateMap.put(state.getName(), state);
            }
            if (state.getId() != null) {
                stateIdMap.put(state.getId(), state);
            }
        }

        // Agrupar transições por (state + event)
        Map<String, List<Transition>> transitionsByKey = new HashMap<>();

        // Primeira passada: validar estrutura básica e agrupar
        for (int i = 0; i < transitions.size(); i++) {
            Transition transition = transitions.get(i);

            // Validar estado de origem (name-based)
            if (transition.getSourceState() != null
                    && transition.getSourceState().getName() != null) {
                if (!stateMap.containsKey(transition.getSourceState().getName())) {
                    errors.add(ValidationError.ofInvalidTransitionReference(i, "source state '"
                            + transition.getSourceState().getName() + "' does not exist"));
                }
            }

            // Validar estado de origem (ID-based)
            if (transition.getSourceStateId() != null) {
                if (!stateIdMap.containsKey(transition.getSourceStateId())) {
                    errors.add(ValidationError.ofInvalidTransitionReference(i, "source state ID '"
                            + transition.getSourceStateId() + "' does not exist"));
                }
            }

            // Validar estado de destino (name-based)
            if (transition.getTargetState() != null
                    && transition.getTargetState().getName() != null) {
                if (!stateMap.containsKey(transition.getTargetState().getName())) {
                    errors.add(ValidationError.ofInvalidTransitionReference(i, "target state '"
                            + transition.getTargetState().getName() + "' does not exist"));
                }
            }

            // Validar estado de destino (ID-based)
            if (transition.getTargetStateId() != null) {
                if (!stateIdMap.containsKey(transition.getTargetStateId())) {
                    errors.add(ValidationError.ofInvalidTransitionReference(i, "target state ID '"
                            + transition.getTargetStateId() + "' does not exist"));
                }
            }

            // Validar que pelo menos uma referência está presente
            if ((transition.getSourceState() == null
                    || transition.getSourceState().getName() == null)
                    && transition.getSourceStateId() == null) {
                errors.add(ValidationError.ofInvalidTransitionReference(i,
                        "source state reference missing (provide either source or sourceStateId)"));
            }

            if ((transition.getTargetState() == null
                    || transition.getTargetState().getName() == null)
                    && transition.getTargetStateId() == null) {
                errors.add(ValidationError.ofInvalidTransitionReference(i,
                        "target state reference missing (provide either target or targetStateId)"));
            }

            // Validar evento
            if (transition.getEvent() == null) {
                errors.add(ValidationError.ofInvalidEvent(i, "event cannot be null"));
            } else if (transition.getEvent().getName() == null
                    || transition.getEvent().getName().trim().isEmpty()) {
                errors.add(ValidationError.ofInvalidEvent(i, "event name cannot be null or empty"));
            }

            // Agrupar para validação de ambiguidade
            if (transition.getSourceState() != null && transition.getSourceState().getName() != null
                    && transition.getEvent() != null && transition.getEvent().getName() != null) {

                String key = transition.getSourceState().getName() + ":"
                        + transition.getEvent().getName();
                transitionsByKey.computeIfAbsent(key, k -> new ArrayList<>()).add(transition);
            }
        }

        // Segunda passada: validar regras de ambiguidade
        for (Map.Entry<String, List<Transition>> entry : transitionsByKey.entrySet()) {
            String key = entry.getKey();
            List<Transition> sameKeyTransitions = entry.getValue();

            if (sameKeyTransitions.size() > 1) {
                // Múltiplas transições com mesmo (state + event)
                List<Transition> withoutCondition = sameKeyTransitions.stream()
                        .filter(t -> t.getCondition() == null || t.getCondition().trim().isEmpty())
                        .toList();

                List<Transition> withCondition = sameKeyTransitions.stream()
                        .filter(t -> t.getCondition() != null && !t.getCondition().trim().isEmpty())
                        .toList();

                // Regra 1: Se existe mais de uma sem condition -> ERRO
                if (withoutCondition.size() > 1) {
                    String[] parts = key.split(":");
                    errors.add(ValidationError.ofAmbiguousTransition(parts[0], parts[1]));
                }
                // Regra 2: Se existe 1 sem condition E N com condition -> ERRO
                else if (withoutCondition.size() == 1 && withCondition.size() > 0) {
                    String[] parts = key.split(":");
                    errors.add(ValidationError.ofAmbiguousTransition(parts[0], parts[1]));
                }
                // Regra 3: Se existem múltiplas com condition -> OK (permitido)
                // (não adiciona erro, é válido)
            }
        }
    }

    /**
     * Valida o estado inicial.
     */
    private void validateInitialState(JourneyDefinition definition, List<ValidationError> errors) {
        State initialState = definition.getInitialState();
        List<State> states = definition.getStates();

        if (initialState == null) {
            errors.add(ValidationError.ofInvalidInitialState("Initial state must be specified"));
            return;
        }

        if (initialState.getName() == null || initialState.getName().trim().isEmpty()) {
            errors.add(ValidationError
                    .ofInvalidInitialState("Initial state name cannot be null or empty"));
            return;
        }

        // Verificar se o estado inicial está na lista de estados
        boolean initialStateExists =
                states.stream().anyMatch(s -> initialState.getName().equals(s.getName()));

        if (!initialStateExists) {
            errors.add(ValidationError.ofInvalidInitialState("Initial state '"
                    + initialState.getName() + "' must be included in states list"));
        }

        // Verificar se o estado inicial é do tipo INITIAL
        State actualInitialState = states.stream()
                .filter(s -> initialState.getName().equals(s.getName())).findFirst().orElse(null);

        if (actualInitialState != null && !StateType.INITIAL.equals(actualInitialState.getType())) {
            errors.add(ValidationError.ofInvalidInitialState(
                    "Initial state '" + initialState.getName() + "' must be of type INITIAL"));
        }
    }

    /**
     * Valida estados finais.
     */
    private void validateFinalStates(JourneyDefinition definition, List<ValidationError> errors) {
        List<State> states = definition.getStates();
        List<Transition> transitions = definition.getTransitions();

        // Verificar se há pelo menos um estado final
        List<State> finalStates = states.stream().filter(s -> StateType.FINAL.equals(s.getType()))
                .collect(Collectors.toList());

        if (finalStates.isEmpty()) {
            errors.add(ValidationError.ofMissingFinalState());
        }

        // Verificar se estados finais têm transições de saída (recomendado)
        if (transitions != null) {
            Set<String> finalStateNames =
                    finalStates.stream().map(State::getName).collect(Collectors.toSet());

            for (Transition transition : transitions) {
                if (transition.getSourceState() != null
                        && finalStateNames.contains(transition.getSourceState().getName())) {
                    errors.add(ValidationError
                            .ofFinalStateWithOutgoing(transition.getSourceState().getName()));
                }
            }
        }
    }

    /**
     * Valida se todos os estados (exceto o inicial) são alcançáveis.
     */
    private void validateReachableStates(JourneyDefinition definition,
            List<ValidationError> errors) {
        List<State> states = definition.getStates();
        List<Transition> transitions = definition.getTransitions();

        if (transitions == null || transitions.isEmpty()) {
            // Se não há transições, apenas o estado inicial é alcançável
            State initialState = definition.getInitialState();
            if (states.size() > 1) {
                Set<String> unreachableStates = states.stream()
                        .filter(s -> !s.getName()
                                .equals(initialState != null ? initialState.getName() : ""))
                        .map(State::getName).collect(Collectors.toSet());

                for (String unreachable : unreachableStates) {
                    errors.add(ValidationError.ofUnreachableState(unreachable));
                }
            }
            return;
        }

        // Construir grafo de alcance
        Set<String> reachableStates = new HashSet<>();
        State initialState = definition.getInitialState();

        if (initialState != null && initialState.getName() != null) {
            reachableStates.add(initialState.getName());
            findReachableStates(initialState.getName(), transitions, reachableStates);
        }

        // Verificar estados não alcançáveis
        for (State state : states) {
            if (!reachableStates.contains(state.getName())) {
                errors.add(ValidationError.ofUnreachableState(state.getName()));
            }
        }
    }

    /**
     * Método recursivo para encontrar estados alcançáveis.
     */
    private void findReachableStates(String currentStateName, List<Transition> transitions,
            Set<String> reachableStates) {

        for (Transition transition : transitions) {
            if (transition.getSourceState() != null
                    && currentStateName.equals(transition.getSourceState().getName())
                    && transition.getTargetState() != null
                    && transition.getTargetState().getName() != null) {

                String targetStateName = transition.getTargetState().getName();
                if (reachableStates.add(targetStateName)) {
                    // Estado não visitado ainda, explorar recursivamente
                    findReachableStates(targetStateName, transitions, reachableStates);
                }
            }
        }
    }

    /**
     * Valida condições das transições (validação básica de estrutura).
     */
    private void validateConditions(JourneyDefinition definition, List<ValidationError> errors) {
        List<Transition> transitions = definition.getTransitions();

        if (transitions == null) {
            return;
        }

        for (int i = 0; i < transitions.size(); i++) {
            Transition transition = transitions.get(i);
            String condition = transition.getCondition();

            if (condition != null && !condition.trim().isEmpty()) {
                // Validação básica: verificar se a condição não está apenas com espaços
                if (condition.trim().isEmpty()) {
                    errors.add(ValidationError.ofInvalidCondition(i, "condition cannot be empty"));
                }

                // Validação básica: verificar caracteres básicos
                if (condition.contains("/*") || condition.contains("*/")) {
                    errors.add(ValidationError.ofInvalidCondition(i,
                            "condition contains invalid characters"));
                }
            }
        }
    }
}
