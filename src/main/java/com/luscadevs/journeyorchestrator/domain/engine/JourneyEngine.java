package com.luscadevs.journeyorchestrator.domain.engine;

import org.springframework.stereotype.Component;

import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.StateType;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

@Component
public class JourneyEngine {

    public void applyEvent(JourneyInstance journeyInstance, JourneyDefinition journeyDefinition,
            Event event, Object eventData) {
        // 1. pegar estado atual
        State currentState = journeyInstance.getCurrentState();

        // 2. procurar transição válida (agora com avaliação de condições)
        Transition validTransition =
                journeyDefinition.findTransition(currentState, event, eventData);

        // 3. verificar se a transição é válida
        if (validTransition == null) {
            String errorMessage =
                    "Event " + event.getName() + " not allowed in state " + currentState.getName();

            // Verificar se existem transições com este evento para debugging
            java.util.List<Transition> possibleTransitions = journeyDefinition.getTransitions()
                    .stream().filter(t -> t.getSourceState().equals(currentState)
                            && t.getEvent().equals(event))
                    .toList();

            if (!possibleTransitions.isEmpty()) {
                errorMessage += ". Conditions evaluated: ";
                java.util.List<String> conditionDetails = new java.util.ArrayList<>();

                for (Transition t : possibleTransitions) {
                    if (t.getCondition() != null && !t.getCondition().trim().isEmpty()) {
                        conditionDetails.add(t.getCondition() + " (FAILED)");
                    } else {
                        conditionDetails.add("No condition (AVAILABLE)");
                    }
                }

                errorMessage += String.join(", ", conditionDetails);
                errorMessage += " - No conditions were met.";
            }

            // Usar exceção com mensagem detalhada
            throw new com.luscadevs.journeyorchestrator.domain.exception.InvalidStateTransitionException(
                    journeyInstance.getId(), currentState.getName(), event.getName(), errorMessage);
        }

        // 4. atualizar estado atual
        journeyInstance.transitionTo(validTransition.getTargetState(), event);

        // 5. verificar se o novo estado é terminal
        State newState = journeyInstance.getCurrentState();

        if (newState != null && newState.getType() == StateType.FINAL) {
            journeyInstance.complete();
        }
    }

}
