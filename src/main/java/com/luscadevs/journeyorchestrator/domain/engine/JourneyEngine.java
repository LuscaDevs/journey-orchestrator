package com.luscadevs.journeyorchestrator.domain.engine;

import org.springframework.stereotype.Component;

import com.luscadevs.journeyorchestrator.application.engine.exception.InvalidTransitionException;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

@Component
public class JourneyEngine {

    public void applyEvent(JourneyInstance journeyInstance, JourneyDefinition journeyDefinition, Event event) {
        // 1. pegar estado atual
        State currentState = journeyInstance.getCurrentState();

        // 2. procurar transição válida
        Transition validTransition = journeyDefinition.findTransition(currentState, event);

        // 3. verificar se a transição é válida
        if (validTransition == null) {
            throw new InvalidTransitionException(
                    "Event " + event.getName() +
                            " not allowed in state " + currentState.getName());
        }

        // 4. atualizar estado atual
        journeyInstance.transitionTo(validTransition.getTargetState(), event);

        // 5. verificar se o novo estado é terminal
        State newState = journeyInstance.getCurrentState();

        if (newState != null && newState.isFinalState()) {
            journeyInstance.complete();
        }
    }

}
