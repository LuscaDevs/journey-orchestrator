package com.luscadevs.journeyorchestrator.domain.engine;

import java.time.Instant;

import com.luscadevs.journeyorchestrator.application.engine.exception.InvalidTransitionException;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstanceStatus;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistory;

public class JourneyEngine {

    public void applyEvent(JourneyInstance journeyInstance, JourneyDefinition journeyDefinition, Event event) {
        // 1. pegar estado atual
        String currentState = journeyInstance.getCurrentState();

        // 2. procurar transição válida
        Transition validTransition = null;

        for (Transition transition : journeyDefinition.getTransitions()) {
            boolean sameSource = transition.getSourceState().equals(currentState);
            boolean sameEvent = transition.getEvent().equals(event.getName());

            if (sameSource && sameEvent) {
                validTransition = transition;
                break;
            }
        }

        // 3. validar se encontrou transição
        if (validTransition == null) {
            throw new InvalidTransitionException(
                    "Event " + event.getName() +
                            " not allowed in state " + currentState);
        }

        // 4. guardar estado antigo
        String oldState = journeyInstance.getCurrentState();

        // 5. atualizar estado atual
        journeyInstance.setCurrentState(validTransition.getTargetState());

        // 6. atualizar timestamp
        Instant now = Instant.now();
        journeyInstance.setUpdatedAt(now);

        // 7. guardar histórico
        TransitionHistory historyEntry = new TransitionHistory();
        historyEntry.setFromState(oldState);
        historyEntry.setToState(validTransition.getTargetState());
        historyEntry.setEvent(event.getName());
        historyEntry.setTimestamp(now);

        journeyInstance.getHistory().add(historyEntry);

        // 8. verificar se o novo estado é terminal
        State newState = null;

        for (State state : journeyDefinition.getStates()) {
            if (state.getName().equals(validTransition.getTargetState())) {
                newState = state;
                break;
            }
        }

        if (newState != null && newState.isFinal()) {
            journeyInstance.setStatus(JourneyInstanceStatus.COMPLETED);
        }
    }
}
