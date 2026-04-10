package com.luscadevs.journeyorchestrator.domain.engine;

import org.springframework.stereotype.Component;

import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

/**
 * Engine central de execução de jornadas. Responsável por toda a lógica de avaliação de transições,
 * condições e execução do fluxo.
 */
@Component
public class JourneyEngine {

    /**
     * Aplica um evento à instância de jornada executando toda a lógica de fluxo.
     * 
     * @param journeyInstance Instância da jornada
     * @param journeyDefinition Definição da jornada
     * @param event Evento a ser aplicado
     * @param eventData Dados do evento
     */
    public void applyEvent(JourneyInstance journeyInstance, JourneyDefinition journeyDefinition,
            Event event, Object eventData) {

        // 0. verificar se a jornada pode receber eventos
        journeyInstance.ensureCanReceiveEvents();

        // 1. pegar estado atual
        State currentState = journeyInstance.getCurrentState();

        // 2. procurar transição válida (com avaliação de condições)
        Transition validTransition =
                findTransition(journeyDefinition, currentState, event, eventData);

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

        if (newState != null && newState
                .getType() == com.luscadevs.journeyorchestrator.domain.journey.StateType.FINAL) {
            journeyInstance.complete();
        }
    }

    /**
     * Encontra a transição válida para o estado atual e evento, avaliando condições se necessário.
     * 
     * @param definition Definição da jornada
     * @param fromState Estado de origem
     * @param event Evento disparado
     * @param eventData Dados do evento para avaliação de condições
     * @return Transição válida ou null se não encontrar
     */
    public Transition findTransition(JourneyDefinition definition, State fromState, Event event,
            Object eventData) {
        for (Transition transition : definition.getTransitions()) {
            if (transition.getSourceState().equals(fromState)
                    && transition.getEvent().equals(event)) {

                // Se não tem condição, retorna a transição
                if (transition.getCondition() == null
                        || transition.getCondition().trim().isEmpty()) {
                    return transition;
                }

                // Se tem condição, avalia usando SpEL
                boolean conditionResult = evaluateCondition(transition.getCondition(), eventData);

                if (conditionResult) {
                    return transition;
                }
            }
        }

        return null;
    }

    /**
     * Avalia condição usando SpEL (Spring Expression Language)
     */
    private boolean evaluateCondition(String condition, Object eventData) {
        try {
            // Se eventData for null, condição não pode ser avaliada
            if (eventData == null) {
                System.out.println("EventData is null - cannot evaluate conditions");
                return false;
            }

            // Implementação simplificada - em produção usar SpEL proper
            if (eventData instanceof java.util.Map) {
                java.util.Map<?, ?> dataMap = (java.util.Map<?, ?>) eventData;

                // Substituir variáveis na condição
                String evaluatedCondition = condition;

                // Substituir #eventData.* pelos valores reais
                for (java.util.Map.Entry<?, ?> entry : dataMap.entrySet()) {
                    String key = entry.getKey().toString();
                    Object value = entry.getValue();

                    System.out.println("Substituting: #" + key + " -> " + value);

                    if (value instanceof Number) {
                        evaluatedCondition = evaluatedCondition
                                .replaceAll("#eventData\\." + key + "\\b", value.toString());
                    } else {
                        evaluatedCondition = evaluatedCondition.replaceAll(
                                "#eventData\\." + key + "\\b", "'" + value.toString() + "'");
                    }
                }

                System.out.println("Final expression: " + evaluatedCondition);

                // Avaliar expressão simplificada (apenas operadores básicos)
                return evaluateSimpleExpression(evaluatedCondition);
            }

            return false;
        } catch (Exception e) {
            // Log error e retorna false por segurança
            return false;
        }
    }

    /**
     * Avalia expressões simples com operadores básicos
     */
    private boolean evaluateSimpleExpression(String expression) {
        try {
            System.out.println("Evaluating expression: " + expression);
            // Implementação básica para AND, OR, >, <, >=, <=, ==
            // Em produção, usar SpEL proper

            // Remover espaços extras
            expression = expression.replaceAll("\\s+", " ").trim();

            // Avaliar AND
            if (expression.contains(" AND ")) {
                String[] parts = expression.split(" AND ");
                System.out.println("AND expression with " + parts.length + " parts");
                for (int i = 0; i < parts.length; i++) {
                    boolean result = evaluateSimpleCondition(parts[i].trim());
                    System.out.println("  Part " + i + ": '" + parts[i].trim() + "' = " + result);
                    if (!result) {
                        return false;
                    }
                }
                return true;
            }

            // Avaliar OR
            if (expression.contains(" OR ")) {
                String[] parts = expression.split(" OR ");
                for (String part : parts) {
                    if (evaluateSimpleCondition(part.trim())) {
                        return true;
                    }
                }
                return false;
            }

            // Condição simples
            return evaluateSimpleCondition(expression);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Avalia condição simples (sem AND/OR)
     */
    private boolean evaluateSimpleCondition(String condition) {
        condition = condition.trim();

        // Operadores
        if (condition.contains(">=")) {
            String[] parts = condition.split(">=");
            return compareNumbers(parts[0].trim(), parts[1].trim(), ">=");
        }
        if (condition.contains("<=")) {
            String[] parts = condition.split("<=");
            return compareNumbers(parts[0].trim(), parts[1].trim(), "<=");
        }
        if (condition.contains(">")) {
            String[] parts = condition.split(">");
            return compareNumbers(parts[0].trim(), parts[1].trim(), ">");
        }
        if (condition.contains("<")) {
            String[] parts = condition.split("<");
            return compareNumbers(parts[0].trim(), parts[1].trim(), "<");
        }
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            return compareNumbers(parts[0].trim(), parts[1].trim(), "==");
        }

        return false;
    }

    /**
     * Compara valores numéricos
     */
    private boolean compareNumbers(String left, String right, String operator) {
        try {
            double leftVal = Double.parseDouble(left.replaceAll("'", ""));
            double rightVal = Double.parseDouble(right.replaceAll("'", ""));

            return switch (operator) {
                case ">=" -> leftVal >= rightVal;
                case "<=" -> leftVal <= rightVal;
                case ">" -> leftVal > rightVal;
                case "<" -> leftVal < rightVal;
                case "==" -> leftVal == rightVal;
                default -> false;
            };
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
