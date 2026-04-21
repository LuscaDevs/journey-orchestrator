package com.luscadevs.journeyorchestrator.api.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.luscadevs.journey.api.generated.model.CreateJourneyDefinitionRequest;
import com.luscadevs.journey.api.generated.model.JourneyDefinitionResponse;
import com.luscadevs.journey.api.generated.model.TransitionRequest;
import com.luscadevs.journey.api.generated.model.TransitionResponse;

import com.luscadevs.journeyorchestrator.application.port.ConditionEvaluatorPort;
import com.luscadevs.journeyorchestrator.domain.exception.InvalidConditionSyntaxException;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionValidationException;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyErrorCodes;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinitionStatus;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.StateType;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;

public final class JourneyDefinitionMapper {

        private static ConditionEvaluatorPort conditionEvaluator;

        private JourneyDefinitionMapper() {}

        public static void setConditionEvaluator(ConditionEvaluatorPort evaluator) {
                conditionEvaluator = evaluator;
        }

        public static JourneyDefinition toDomain(CreateJourneyDefinitionRequest request) {

                // 1️⃣ Mapear states
                List<State> states = request.getStates().stream().map(s -> {

                        // Mapear corretamente entre enums gerados e de domínio
                        StateType domainType;
                        if (s.getType() == null) {
                                domainType = StateType.INTERMEDIATE;
                        } else {
                                switch (s.getType().getValue()) {
                                        case "INITIAL" -> domainType = StateType.INITIAL;
                                        case "INTERMEDIATE" -> domainType = StateType.INTERMEDIATE;
                                        case "FINAL" -> domainType = StateType.FINAL;
                                        default -> throw new IllegalArgumentException(
                                                        "Unknown StateType: "
                                                                        + s.getType().getValue());
                                }
                        }

                        // Map position if provided
                        com.luscadevs.journeyorchestrator.domain.journey.Position position = null;
                        if (s.getPosition() != null) {
                                position = com.luscadevs.journeyorchestrator.domain.journey.Position
                                                .builder().x(s.getPosition().getX())
                                                .y(s.getPosition().getY()).build();
                        }

                        // Auto-generate UUID if not provided
                        java.util.UUID stateId = s.getId();
                        if (stateId == null) {
                                stateId = java.util.UUID.randomUUID();
                        }

                        return State.builder().id(stateId).name(s.getName()).type(domainType)
                                        .position(position).build();
                }).toList();

                // 2️⃣ Criar mapa para lookup rápido
                Map<String, State> stateMap = states.stream()
                                .collect(Collectors.toMap(State::getName, Function.identity()));

                // 3️⃣ Resolver estado inicial pelo tipo
                State initialState = states.stream().filter(s -> s.getType() == StateType.INITIAL)
                                .findFirst()
                                .orElseThrow(() -> new JourneyDefinitionValidationException(
                                                JourneyErrorCodes.NO_INITIAL_STATE,
                                                "No INITIAL state defined. Every journey must have exactly one INITIAL state."));

                // 4️⃣ Mapear transitions
                List<Transition> transitions = request.getTransitions().stream()
                                .map(t -> mapTransition(t, stateMap)).toList();

                // 5️⃣ Construir domínio
                String journeyCode =
                                request.getJourneyCode() != null ? request.getJourneyCode().trim()
                                                : null;
                return JourneyDefinition.builder().journeyCode(journeyCode).name(request.getName())
                                .version(request.getVersion())
                                .status(request.getStatus() != null
                                                ? JourneyDefinitionStatus.valueOf(
                                                                request.getStatus().getValue())
                                                : JourneyDefinitionStatus.RASCUNHO)
                                .states(states).initialState(initialState).transitions(transitions)
                                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
        }

        private static Transition mapTransition(TransitionRequest t, Map<String, State> stateMap) {

                State source = null;
                State target = null;

                // Resolve source state - prefer ID if provided, otherwise use name
                if (t.getSourceStateId() != null && t.getSource() != null) {
                        // Both ID and name provided - validate they point to the same state
                        State idBasedSource = stateMap.values().stream()
                                        .filter(s -> t.getSourceStateId().equals(s.getId()))
                                        .findFirst()
                                        .orElseThrow(() -> new JourneyDefinitionValidationException(
                                                        JourneyErrorCodes.SOURCE_STATE_NOT_FOUND,
                                                        "Source state with ID '"
                                                                        + t.getSourceStateId()
                                                                        + "' not found"));

                        State nameBasedSource = stateMap.get(t.getSource());
                        if (nameBasedSource == null) {
                                throw new JourneyDefinitionValidationException(
                                                JourneyErrorCodes.SOURCE_STATE_NOT_FOUND,
                                                "Source state '" + t.getSource() + "' not found");
                        }

                        if (!idBasedSource.equals(nameBasedSource)) {
                                throw new JourneyDefinitionValidationException(
                                                JourneyErrorCodes.TRANSITION_CONFLICT,
                                                "Conflict: source state ID '" + t.getSourceStateId()
                                                                + "' and name '" + t.getSource()
                                                                + "' refer to different states");
                        }

                        source = idBasedSource;
                } else if (t.getSourceStateId() != null) {
                        // ID-based reference only
                        source = stateMap.values().stream()
                                        .filter(s -> t.getSourceStateId().equals(s.getId()))
                                        .findFirst()
                                        .orElseThrow(() -> new JourneyDefinitionValidationException(
                                                        JourneyErrorCodes.SOURCE_STATE_NOT_FOUND,
                                                        "Source state with ID '"
                                                                        + t.getSourceStateId()
                                                                        + "' not found"));
                } else if (t.getSource() != null) {
                        // Name-based reference only (legacy)
                        source = stateMap.get(t.getSource());
                        if (source == null) {
                                throw new JourneyDefinitionValidationException(
                                                JourneyErrorCodes.SOURCE_STATE_NOT_FOUND,
                                                "Source state '" + t.getSource() + "' not found");
                        }
                } else {
                        throw new JourneyDefinitionValidationException(
                                        JourneyErrorCodes.TRANSITION_SOURCE_REQUIRED,
                                        "Source state reference missing (provide either source or sourceStateId)");
                }

                // Resolve target state - prefer ID if provided, otherwise use name
                if (t.getTargetStateId() != null && t.getTarget() != null) {
                        // Both ID and name provided - validate they point to the same state
                        State idBasedTarget = stateMap.values().stream()
                                        .filter(s -> t.getTargetStateId().equals(s.getId()))
                                        .findFirst()
                                        .orElseThrow(() -> new JourneyDefinitionValidationException(
                                                        JourneyErrorCodes.TARGET_STATE_NOT_FOUND,
                                                        "Target state with ID '"
                                                                        + t.getTargetStateId()
                                                                        + "' not found"));

                        State nameBasedTarget = stateMap.get(t.getTarget());
                        if (nameBasedTarget == null) {
                                throw new JourneyDefinitionValidationException(
                                                JourneyErrorCodes.TARGET_STATE_NOT_FOUND,
                                                "Target state '" + t.getTarget() + "' not found");
                        }

                        if (!idBasedTarget.equals(nameBasedTarget)) {
                                throw new JourneyDefinitionValidationException(
                                                JourneyErrorCodes.TRANSITION_CONFLICT,
                                                "Conflict: target state ID '" + t.getTargetStateId()
                                                                + "' and name '" + t.getTarget()
                                                                + "' refer to different states");
                        }

                        target = idBasedTarget;
                } else if (t.getTargetStateId() != null) {
                        // ID-based reference only
                        target = stateMap.values().stream()
                                        .filter(s -> t.getTargetStateId().equals(s.getId()))
                                        .findFirst()
                                        .orElseThrow(() -> new JourneyDefinitionValidationException(
                                                        JourneyErrorCodes.TARGET_STATE_NOT_FOUND,
                                                        "Target state with ID '"
                                                                        + t.getTargetStateId()
                                                                        + "' not found"));
                } else if (t.getTarget() != null) {
                        // Name-based reference only (legacy)
                        target = stateMap.get(t.getTarget());
                        if (target == null) {
                                throw new JourneyDefinitionValidationException(
                                                JourneyErrorCodes.TARGET_STATE_NOT_FOUND,
                                                "Target state '" + t.getTarget() + "' not found");
                        }
                } else {
                        throw new JourneyDefinitionValidationException(
                                        JourneyErrorCodes.TRANSITION_TARGET_REQUIRED,
                                        "Target state reference missing (provide either target or targetStateId)");
                }

                // Build transition with resolved states
                Transition.TransitionBuilder builder = Transition.builder().sourceState(source)
                                .targetState(target).event(new Event(t.getEvent()))
                                .condition(validateCondition(t.getCondition()));

                // Store the ID references for persistence
                if (t.getSourceStateId() != null) {
                        builder.sourceStateId(t.getSourceStateId());
                }
                if (t.getTargetStateId() != null) {
                        builder.targetStateId(t.getTargetStateId());
                }

                return builder.build();
        }

        private static String validateCondition(String condition) {
                if (condition == null || condition.trim().isEmpty()) {
                        return condition;
                }

                if (conditionEvaluator == null) {
                        throw new IllegalStateException(
                                        "ConditionEvaluator not initialized. Call setConditionEvaluator() first.");
                }

                if (!conditionEvaluator.validateExpression(condition)) {
                        throw new InvalidConditionSyntaxException(condition,
                                        "Expression cannot be parsed.");
                }

                return condition;
        }

        public static JourneyDefinitionResponse toResponse(JourneyDefinition definition) {

                JourneyDefinitionResponse response = new JourneyDefinitionResponse();

                response.setId(definition.getId());
                response.setJourneyCode(definition.getJourneyCode());
                response.setName(definition.getName());
                response.setVersion(definition.getVersion());
                response.setStatus(definition.getStatus() != null
                                ? com.luscadevs.journey.api.generated.model.JourneyDefinitionStatus
                                                .valueOf(definition.getStatus().name())
                                : com.luscadevs.journey.api.generated.model.JourneyDefinitionStatus.RASCUNHO);
                response.setCreatedAt(definition.getCreatedAt() != null
                                ? OffsetDateTime.ofInstant(definition.getCreatedAt(),
                                                ZoneOffset.UTC)
                                : null);
                response.setUpdatedAt(definition.getUpdatedAt() != null
                                ? OffsetDateTime.ofInstant(definition.getUpdatedAt(),
                                                ZoneOffset.UTC)
                                : null);

                // states - handle null case
                if (definition.getStates() != null) {
                        List<com.luscadevs.journey.api.generated.model.State> states =
                                        definition.getStates().stream().map(s -> {
                                                com.luscadevs.journey.api.generated.model.StateType apiType;
                                                if (s.getType() == null) {
                                                        apiType = com.luscadevs.journey.api.generated.model.StateType.INTERMEDIATE;
                                                } else {
                                                        switch (s.getType()) {
                                                                case INITIAL -> apiType =
                                                                                com.luscadevs.journey.api.generated.model.StateType.INITIAL;
                                                                case INTERMEDIATE -> apiType =
                                                                                com.luscadevs.journey.api.generated.model.StateType.INTERMEDIATE;
                                                                case FINAL -> apiType =
                                                                                com.luscadevs.journey.api.generated.model.StateType.FINAL;
                                                                default -> throw new IllegalArgumentException(
                                                                                "Unknown StateType: "
                                                                                                + s.getType());
                                                        }
                                                }

                                                com.luscadevs.journey.api.generated.model.State state =
                                                                new com.luscadevs.journey.api.generated.model.State()
                                                                                .name(s.getName())
                                                                                .type(apiType)
                                                                                .id(s.getId());

                                                // Map position if present
                                                if (s.getPosition() != null) {
                                                        com.luscadevs.journey.api.generated.model.StatePosition position =
                                                                        new com.luscadevs.journey.api.generated.model.StatePosition()
                                                                                        .x(s.getPosition()
                                                                                                        .getX())
                                                                                        .y(s.getPosition()
                                                                                                        .getY());
                                                        state.setPosition(position);
                                                }

                                                return state;
                                        }).toList();
                        response.setStates(states);
                } else {
                        response.setStates(List.of());
                }

                // transitions - handle null case
                if (definition.getTransitions() != null) {
                        List<TransitionResponse> transitions = definition.getTransitions().stream()
                                        .map(t -> new TransitionResponse()
                                                        .source(t.getSourceState() != null
                                                                        ? t.getSourceState()
                                                                                        .getName()
                                                                        : null)
                                                        .target(t.getTargetState() != null
                                                                        ? t.getTargetState()
                                                                                        .getName()
                                                                        : null)
                                                        .sourceStateId(t.getSourceStateId())
                                                        .targetStateId(t.getTargetStateId())
                                                        .event(t.getEvent() != null
                                                                        ? t.getEvent().getName()
                                                                        : null)
                                                        .condition(t.getCondition()))
                                        .toList();
                        response.setTransitions(transitions);
                } else {
                        response.setTransitions(List.of());
                }

                return response;
        }
}
