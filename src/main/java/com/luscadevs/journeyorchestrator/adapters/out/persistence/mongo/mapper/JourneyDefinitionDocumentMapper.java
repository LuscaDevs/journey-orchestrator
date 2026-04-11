package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyDefinitionDocument;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between JourneyDefinition domain entity and MongoDB document.
 */
@Component
public class JourneyDefinitionDocumentMapper {

        /**
         * Convert domain entity to MongoDB document.
         */
        public JourneyDefinitionDocument toDocument(JourneyDefinition journeyDefinition) {
                if (journeyDefinition == null) {
                        return null;
                }

                JourneyDefinitionDocument document = new JourneyDefinitionDocument();
                document.setId(journeyDefinition.getId());
                document.setJourneyCode(journeyDefinition.getJourneyCode());
                document.setName(journeyDefinition.getName());
                document.setVersion(journeyDefinition.getVersion());
                document.setActive(journeyDefinition.isActive());

                // Map states using available fields
                if (journeyDefinition.getStates() != null) {
                        List<JourneyDefinitionDocument.StateDocument> stateDocuments =
                                        journeyDefinition.getStates().stream().map(state -> {
                                                JourneyDefinitionDocument.Position position = null;
                                                if (state.getPosition() != null) {
                                                        position = new JourneyDefinitionDocument.Position(
                                                                        state.getPosition().getX(),
                                                                        state.getPosition().getY());
                                                }
                                                return new JourneyDefinitionDocument.StateDocument(
                                                                state.getId() != null ? state
                                                                                .getId().toString()
                                                                                : null,
                                                                state.getName(),
                                                                state.getType().name(), position);
                                        }).collect(Collectors.toList());
                        document.setStates(stateDocuments);
                }

                // Map transitions using available fields and sort in sequential order
                if (journeyDefinition.getTransitions() != null) {
                        List<JourneyDefinitionDocument.TransitionDocument> transitionDocuments =
                                        journeyDefinition.getTransitions().stream().map(
                                                        transition -> new JourneyDefinitionDocument.TransitionDocument(
                                                                        null, // id
                                                                        transition.getSourceState() != null
                                                                                        ? transition.getSourceState()
                                                                                                        .getName()
                                                                                        : null,
                                                                        transition.getTargetState() != null
                                                                                        ? transition.getTargetState()
                                                                                                        .getName()
                                                                                        : null,
                                                                        transition.getSourceStateId() != null
                                                                                        ? transition.getSourceStateId()
                                                                                                        .toString()
                                                                                        : null,
                                                                        transition.getTargetStateId() != null
                                                                                        ? transition.getTargetStateId()
                                                                                                        .toString()
                                                                                        : null,
                                                                        transition.getEvent()
                                                                                        .getName(),
                                                                        transition.getCondition(),
                                                                        null // metadata - not
                                                                             // available in current
                                                                             // domain
                                                                             // domain
                                                        )).collect(Collectors.toList());

                        // Sort transitions in sequential order (from initial to final)
                        List<JourneyDefinitionDocument.TransitionDocument> sortedTransitions =
                                        sortTransitionsSequentially(transitionDocuments,
                                                        journeyDefinition.getStates());
                        document.setTransitions(sortedTransitions);
                }

                return document;
        }

        /**
         * Sort transitions in sequential order based on journey flow from initial to final states.
         */
        private List<JourneyDefinitionDocument.TransitionDocument> sortTransitionsSequentially(
                        List<JourneyDefinitionDocument.TransitionDocument> transitions,
                        List<State> states) {
                if (transitions == null || transitions.isEmpty() || states == null
                                || states.isEmpty()) {
                        return transitions;
                }

                // Find the initial state
                String initialStateName =
                                states.stream().filter(s -> "INITIAL".equals(s.getType().name()))
                                                .map(State::getName).findFirst().orElse(null);

                if (initialStateName == null) {
                        // No initial state found, return original list
                        return transitions;
                }

                // Build a map of transitions by source state for quick lookup
                java.util.Map<String, List<JourneyDefinitionDocument.TransitionDocument>> transitionsBySource =
                                transitions.stream().collect(java.util.stream.Collectors.groupingBy(
                                                JourneyDefinitionDocument.TransitionDocument::getFromState));

                // Sort transitions sequentially
                List<JourneyDefinitionDocument.TransitionDocument> sortedTransitions =
                                new java.util.ArrayList<>();
                java.util.Set<String> visitedStates = new java.util.HashSet<>();
                String currentState = initialStateName;

                while (currentState != null && !visitedStates.contains(currentState)) {
                        visitedStates.add(currentState);
                        List<JourneyDefinitionDocument.TransitionDocument> outgoingTransitions =
                                        transitionsBySource.get(currentState);

                        if (outgoingTransitions != null && !outgoingTransitions.isEmpty()) {
                                // Add transitions from current state
                                for (JourneyDefinitionDocument.TransitionDocument transition : outgoingTransitions) {
                                        if (!sortedTransitions.contains(transition)) {
                                                sortedTransitions.add(transition);
                                        }
                                }

                                // Move to the first target state (assuming linear flow)
                                String nextState = outgoingTransitions.get(0).getToState();
                                if (!visitedStates.contains(nextState)) {
                                        currentState = nextState;
                                } else {
                                        currentState = null;
                                }
                        } else {
                                currentState = null;
                        }
                }

                // Add any remaining transitions that weren't in the sequential path (e.g.,
                // branching)
                for (JourneyDefinitionDocument.TransitionDocument transition : transitions) {
                        if (!sortedTransitions.contains(transition)) {
                                sortedTransitions.add(transition);
                        }
                }

                return sortedTransitions;
        }

        /**
         * Convert MongoDB document to domain entity.
         */
        public JourneyDefinition toDomain(JourneyDefinitionDocument document) {
                if (document == null) {
                        return null;
                }

                JourneyDefinition.JourneyDefinitionBuilder builder = JourneyDefinition.builder()
                                .id(document.getId()).journeyCode(document.getJourneyCode())
                                .name(document.getName()).version(document.getVersion())
                                .active(document.isActive());

                // Map states using available fields
                if (document.getStates() != null) {
                        List<State> states = document.getStates().stream().map(stateDoc -> {
                                State.StateBuilder stateBuilder = State.builder()
                                                .name(stateDoc.getName())
                                                .type(com.luscadevs.journeyorchestrator.domain.journey.StateType
                                                                .valueOf(stateDoc.getType()));

                                // Map id if present
                                if (stateDoc.getId() != null) {
                                        try {
                                                stateBuilder.id(java.util.UUID
                                                                .fromString(stateDoc.getId()));
                                        } catch (IllegalArgumentException e) {
                                                // Invalid UUID format, let builder auto-generate
                                        }
                                }

                                // Map position if present
                                if (stateDoc.getPosition() != null) {
                                        stateBuilder.position(
                                                        com.luscadevs.journeyorchestrator.domain.journey.Position
                                                                        .builder()
                                                                        .x(stateDoc.getPosition()
                                                                                        .getX())
                                                                        .y(stateDoc.getPosition()
                                                                                        .getY())
                                                                        .build());
                                }

                                return stateBuilder.build();
                        }).collect(Collectors.toList());
                        builder.states(states);
                }

                // Map transitions using available fields
                if (document.getTransitions() != null) {
                        List<Transition> transitions =
                                        document.getTransitions().stream().map(transDoc -> {
                                                Transition.TransitionBuilder transitionBuilder =
                                                                Transition.builder().sourceState(
                                                                                State.builder().name(
                                                                                                transDoc.getFromState())
                                                                                                .build())
                                                                                .targetState(State
                                                                                                .builder()
                                                                                                .name(transDoc.getToState())
                                                                                                .build())
                                                                                .event(new Event(
                                                                                                transDoc.getEvent()))
                                                                                .condition(transDoc
                                                                                                .getCondition());

                                                // Map sourceStateId if present
                                                if (transDoc.getSourceStateId() != null) {
                                                        try {
                                                                transitionBuilder.sourceStateId(
                                                                                java.util.UUID.fromString(
                                                                                                transDoc.getSourceStateId()));
                                                        } catch (IllegalArgumentException e) {
                                                                // Invalid UUID format, ignore
                                                        }
                                                }

                                                // Map targetStateId if present
                                                if (transDoc.getTargetStateId() != null) {
                                                        try {
                                                                transitionBuilder.targetStateId(
                                                                                java.util.UUID.fromString(
                                                                                                transDoc.getTargetStateId()));
                                                        } catch (IllegalArgumentException e) {
                                                                // Invalid UUID format, ignore
                                                        }
                                                }

                                                return transitionBuilder.build();
                                        }).collect(Collectors.toList());
                        builder.transitions(transitions);
                }

                return builder.build();
        }
}
