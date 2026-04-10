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

                // Map transitions using available fields
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
                        document.setTransitions(transitionDocuments);
                }

                return document;
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
