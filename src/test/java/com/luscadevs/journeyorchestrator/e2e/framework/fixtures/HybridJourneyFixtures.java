package com.luscadevs.journeyorchestrator.e2e.framework.fixtures;

import java.util.Map;

/**
 * Hybrid fixture approach combining JSON files with Java type safety and flexibility. This provides
 * the best of both worlds: - JSON readability and maintainability - Java type safety and IDE
 * support - Dynamic fixture generation capabilities
 */
public class HybridJourneyFixtures {

    private final JsonFixtureLoader jsonLoader;

    public HybridJourneyFixtures() {
        this.jsonLoader = new JsonFixtureLoader();
    }

    /**
     * Loads simple journey definition from JSON
     */
    public Map<String, Object> simpleJourney() {
        return jsonLoader.loadValidJourneyDefinition("simple-journey");
    }

    /**
     * Loads conditional journey definition from JSON
     */
    public Map<String, Object> conditionalJourney() {
        return jsonLoader.loadValidJourneyDefinition("conditional-journey");
    }

    /**
     * Creates a variation of simple journey with custom journey code
     */
    public Map<String, Object> simpleJourney(String journeyCode) {
        return jsonLoader.createJourneyVariation("simple-journey",
                Map.of("journeyCode", journeyCode));
    }

    /**
     * Creates a variation of conditional journey with custom journey code
     */
    public Map<String, Object> conditionalJourney(String journeyCode) {
        return jsonLoader.createJourneyVariation("conditional-journey",
                Map.of("journeyCode", journeyCode));
    }

    /**
     * Creates a high-value conditional journey with specific amount threshold
     */
    public Map<String, Object> highValueConditionalJourney(double threshold) {
        Map<String, Object> base = conditionalJourney();

        // Update the first transition condition to use custom threshold
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> transitions =
                (java.util.List<Map<String, Object>>) base.get("transitions");

        if (!transitions.isEmpty()) {
            transitions.get(0).put("condition", "#eventData.amount > " + threshold);
        }

        return base;
    }

    /**
     * Creates a multi-state journey with sequential processing
     */
    public Map<String, Object> multiStateJourney() {
        return jsonLoader.createJourneyVariation("simple-journey", Map.of("journeyCode",
                "MULTI_STATE_JOURNEY", "name", "Multi State Test Journey", "states",
                java.util.List.of(
                        Map.of("name", "START", "type", "INITIAL", "description", "Initial state"),
                        Map.of("name", "PROCESSING", "type", "INTERMEDIATE", "description",
                                "Processing state"),
                        Map.of("name", "VALIDATED", "type", "INTERMEDIATE", "description",
                                "Validated state"),
                        Map.of("name", "FINAL", "type", "FINAL", "description", "Final state")),
                "transitions",
                java.util.List.of(
                        Map.of("source", "START", "event", "PROCESS", "target", "PROCESSING",
                                "description", "Start processing"),
                        Map.of("source", "PROCESSING", "event", "VALIDATE", "target", "VALIDATED",
                                "description", "Validation step"),
                        Map.of("source", "VALIDATED", "event", "FINALIZE", "target", "FINAL",
                                "description", "Finalization step"))));
    }
}
