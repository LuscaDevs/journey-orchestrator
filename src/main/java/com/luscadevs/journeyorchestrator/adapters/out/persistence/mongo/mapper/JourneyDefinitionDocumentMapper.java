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
                document.setVersion(String.valueOf(journeyDefinition.getVersion()));
                document.setActive(journeyDefinition.isActive());

                // Map states using available fields
                if (journeyDefinition.getStates() != null) {
                        List<JourneyDefinitionDocument.StateDocument> stateDocuments =
                                        journeyDefinition.getStates().stream().map(
                                                        state -> new JourneyDefinitionDocument.StateDocument(
                                                                        state.getName(),
                                                                        state.getType().name()))
                                                        .collect(Collectors.toList());
                        document.setStates(stateDocuments);
                }

                // Map transitions using available fields
                if (journeyDefinition.getTransitions() != null) {
                        List<JourneyDefinitionDocument.TransitionDocument> transitionDocuments =
                                        journeyDefinition.getTransitions().stream().map(
                                                        transition -> new JourneyDefinitionDocument.TransitionDocument(
                                                                        transition.getEvent()
                                                                                        .getName(),
                                                                        transition.getSourceState()
                                                                                        .getName(),
                                                                        transition.getTargetState()
                                                                                        .getName(),
                                                                        transition.getEvent()
                                                                                        .getName(),
                                                                        transition.getCondition(), // condition
                                                                                                   // -
                                                                                                   // now
                                                                                                   // available
                                                                                                   // in
                                                                                                   // domain
                                                                        null // metadata - not
                                                                             // available in current
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
                                .name(document.getName())
                                .version(Integer.parseInt(document.getVersion()))
                                .active(document.isActive());

                // Map states using available fields
                if (document.getStates() != null) {
                        List<State> states = document.getStates().stream().map(stateDoc -> State
                                        .builder().name(stateDoc.getName())
                                        .type(com.luscadevs.journeyorchestrator.domain.journey.StateType
                                                        .valueOf(stateDoc.getType()))
                                        .build()).collect(Collectors.toList());
                        builder.states(states);
                }

                // Map transitions using available fields
                if (document.getTransitions() != null) {
                        List<Transition> transitions = document.getTransitions().stream().map(
                                        transDoc -> Transition.builder().sourceState(State.builder()
                                                        .name(transDoc.getFromState()).build())
                                                        .targetState(State.builder()
                                                                        .name(transDoc.getToState())
                                                                        .build())
                                                        .event(new Event(transDoc.getEvent()))
                                                        .condition(transDoc.getCondition()).build())
                                        .collect(Collectors.toList());
                        builder.transitions(transitions);
                }

                return builder.build();
        }
}
