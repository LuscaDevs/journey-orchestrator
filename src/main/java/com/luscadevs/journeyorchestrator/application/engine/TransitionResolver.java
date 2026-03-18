package com.luscadevs.journeyorchestrator.application.engine;

import org.springframework.stereotype.Component;

import com.luscadevs.journeyorchestrator.application.engine.exception.InvalidTransitionException;
import com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document.JourneyDefinitionDocument;
import com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document.TransitionDocument;

@Component
public class TransitionResolver {

    public TransitionDocument resolve(
            JourneyDefinitionDocument definition,
            String currentState,
            String event) {

        return definition.getTransitions()
                .stream()
                .filter(t -> t.getSource().equals(currentState) &&
                        t.getEvent().equals(event))
                .findFirst()
                .orElseThrow(() -> new InvalidTransitionException(
                        String.format(
                                "Invalid transition: state=%s event=%s",
                                currentState,
                                event)));
    }

}