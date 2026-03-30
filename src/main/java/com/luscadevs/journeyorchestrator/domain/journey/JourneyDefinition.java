package com.luscadevs.journeyorchestrator.domain.journey;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class JourneyDefinition {

    private String id;

    private String journeyCode;

    private String name;

    private Integer version;

    private boolean active;

    private State initialState;

    private List<State> states;

    private List<Transition> transitions;

    private Instant createdAt;

    public Transition findTransition(State fromState, Event event, Object eventData) {

        for (Transition transition : transitions) {

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
