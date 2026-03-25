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

import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.StateType;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;

public final class JourneyDefinitionMapper {

        private JourneyDefinitionMapper() {
        }

        public static JourneyDefinition toDomain(CreateJourneyDefinitionRequest request) {

                // 1️⃣ Mapear states
                List<State> states = request.getStates()
                                .stream()
                                .map(s -> {

                                        StateType type = s.getType() == null
                                                        ? StateType.NORMAL
                                                        : StateType.valueOf(s.getType().name());

                                        return State.builder()
                                                        .name(s.getName())
                                                        .type(type)
                                                        .build();
                                })
                                .toList();

                // 2️⃣ Criar mapa para lookup rápido
                Map<String, State> stateMap = states.stream()
                                .collect(Collectors.toMap(State::getName, Function.identity()));

                // 3️⃣ Resolver estado inicial pelo tipo
                State initialState = states.stream()
                                .filter(s -> s.getType() == StateType.INITIAL)
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("No INITIAL state defined"));

                // 4️⃣ Mapear transitions
                List<Transition> transitions = request.getTransitions()
                                .stream()
                                .map(t -> mapTransition(t, stateMap))
                                .toList();

                // 5️⃣ Construir domínio
                return JourneyDefinition.builder()
                                .journeyCode(request.getJourneyCode())
                                .name(request.getName())
                                .version(request.getVersion())
                                .states(states)
                                .initialState(initialState)
                                .transitions(transitions)
                                .createdAt(Instant.now())
                                .build();
        }

        private static Transition mapTransition(TransitionRequest t, Map<String, State> stateMap) {

                State source = stateMap.get(t.getSource());
                State target = stateMap.get(t.getTarget());

                if (source == null) {
                        throw new IllegalArgumentException("Source state '" + t.getSource() + "' not found");
                }

                if (target == null) {
                        throw new IllegalArgumentException("Target state '" + t.getTarget() + "' not found");
                }

                return Transition.builder()
                                .sourceState(source)
                                .targetState(target)
                                .event(new Event(t.getEvent()))
                                .build();
        }

        public static JourneyDefinitionResponse toResponse(JourneyDefinition definition) {

                JourneyDefinitionResponse response = new JourneyDefinitionResponse();

                response.setId(definition.getId());
                response.setJourneyCode(definition.getJourneyCode());
                response.setName(definition.getName());
                response.setVersion(definition.getVersion());
                response.setActive(definition.isActive());
                response.setCreatedAt(definition.getCreatedAt() != null
                                ? OffsetDateTime.ofInstant(definition.getCreatedAt(), ZoneOffset.UTC)
                                : null);

                // states - handle null case
                if (definition.getStates() != null) {
                        List<com.luscadevs.journey.api.generated.model.State> states = definition.getStates()
                                        .stream()
                                        .map(s -> new com.luscadevs.journey.api.generated.model.State()
                                                        .name(s.getName())
                                                        .type(com.luscadevs.journey.api.generated.model.StateType
                                                                        .valueOf(s.getType().name())))
                                        .toList();
                        response.setStates(states);
                } else {
                        response.setStates(List.of());
                }

                // transitions - handle null case
                if (definition.getTransitions() != null) {
                        List<TransitionResponse> transitions = definition.getTransitions()
                                        .stream()
                                        .map(t -> new TransitionResponse()
                                                        .source(t.getSourceState().getName())
                                                        .event(t.getEvent().getName())
                                                        .target(t.getTargetState().getName()))
                                        .toList();
                        response.setTransitions(transitions);
                } else {
                        response.setTransitions(List.of());
                }

                return response;
        }
}