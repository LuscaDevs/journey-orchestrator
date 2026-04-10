package com.luscadevs.journeyorchestrator.domain.journeyinstance;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.luscadevs.journey.api.generated.model.JourneyStatus;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.StateType;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JourneyInstance {
    private String id;
    private String journeyDefinitionId;
    private Integer journeyVersion;
    private State currentState;
    private JourneyStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<TransitionHistory> history;
    private Map<String, Object> context;
    private Long version;

    public void transitionTo(State newState, Event event) {

        TransitionHistory historyEntry =
                TransitionHistory.builder().id(TransitionHistoryEventId.generate())
                        .instanceId(this.id).fromState(this.currentState).toState(newState)
                        .event(event).timestamp(Instant.now()).metadata(Map.of()).build();

        Instant now = Instant.now();
        this.currentState = newState;
        this.updatedAt = now;
        this.history.add(historyEntry);
    }

    public static JourneyInstance start(String definitionId, Integer version, State initialState,
            Map<String, Object> context) {

        return JourneyInstance.builder().id(UUID.randomUUID().toString())
                .journeyDefinitionId(definitionId).journeyVersion(version)
                .currentState(initialState).status(JourneyStatus.RUNNING).createdAt(Instant.now())
                .updatedAt(Instant.now()).context(context).history(new ArrayList<>()).build();

    }

    public void complete() {
        this.status = JourneyStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = JourneyStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /**
     * Verifica se a jornada pode receber eventos (não está completada).
     * 
     * @throws JourneyAlreadyCompletedException se a jornada já estiver completada
     */
    public void ensureCanReceiveEvents() {
        if (this.status == JourneyStatus.COMPLETED) {
            throw new com.luscadevs.journeyorchestrator.domain.exception.JourneyAlreadyCompletedException(
                    this.id);
        }
    }

    /**
     * Aplica um evento à instância de jornada, executando a transição de estado correspondente de
     * acordo com a definição da jornada.
     * 
     * @param definition Definição da jornada contendo estados e transições
     * @param event Evento a ser aplicado
     * @param eventData Dados do evento para avaliação de condições
     * @throws InvalidStateTransitionException se a transição não for válida
     */
    public void applyEvent(JourneyDefinition definition, Event event, Object eventData) {
        // 0. verificar se a jornada pode receber eventos
        ensureCanReceiveEvents();

        // 1. pegar estado atual
        State currentState = this.currentState;

        // 2. procurar transição válida (agora com avaliação de condições)
        Transition validTransition = definition.findTransition(currentState, event, eventData);

        // 3. verificar se a transição é válida
        if (validTransition == null) {
            String errorMessage =
                    "Event " + event.getName() + " not allowed in state " + currentState.getName();

            // Verificar se existem transições com este evento para debugging
            java.util.List<Transition> possibleTransitions = definition.getTransitions().stream()
                    .filter(t -> t.getSourceState().equals(currentState)
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
                    this.id, currentState.getName(), event.getName(), errorMessage);
        }

        // 4. atualizar estado atual
        transitionTo(validTransition.getTargetState(), event);

        // 5. verificar se o novo estado é terminal
        State newState = this.currentState;

        if (newState != null && newState.getType() == StateType.FINAL) {
            complete();
        }
    }
}
