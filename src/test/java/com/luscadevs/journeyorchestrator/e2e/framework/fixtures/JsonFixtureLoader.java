package com.luscadevs.journeyorchestrator.e2e.framework.fixtures;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Map;

/**
 * Hybrid fixture loader that combines JSON files with Java type safety. Loads JSON fixtures and
 * provides type-safe access with validation.
 */
public class JsonFixtureLoader {

    private final ObjectMapper objectMapper;

    public JsonFixtureLoader() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Loads a journey definition from JSON file with type safety
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadJourneyDefinition(String fixtureName) {
        try {
            String path = "fixtures/journey-definitions/" + fixtureName + ".json";
            ClassPathResource resource = new ClassPathResource(path);

            if (!resource.exists()) {
                throw new IllegalArgumentException("Fixture not found: " + path);
            }

            return objectMapper.readValue(resource.getInputStream(), Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load fixture: " + fixtureName, e);
        }
    }

    /**
     * Loads and validates a journey definition fixture
     */
    public Map<String, Object> loadValidJourneyDefinition(String fixtureName) {
        Map<String, Object> journey = loadJourneyDefinition(fixtureName);
        validateJourneyDefinition(journey);
        return journey;
    }

    /**
     * Basic validation for journey definition structure
     */
    private void validateJourneyDefinition(Map<String, Object> journey) {
        if (!journey.containsKey("journeyCode")) {
            throw new IllegalArgumentException("Journey definition must contain 'journeyCode'");
        }
        if (!journey.containsKey("states")) {
            throw new IllegalArgumentException("Journey definition must contain 'states'");
        }
        if (!journey.containsKey("transitions")) {
            throw new IllegalArgumentException("Journey definition must contain 'transitions'");
        }
    }

    /**
     * Creates a variation of a journey definition with custom parameters
     */
    public Map<String, Object> createJourneyVariation(String baseFixture,
            Map<String, Object> overrides) {
        Map<String, Object> journey = loadValidJourneyDefinition(baseFixture);

        // Apply overrides
        overrides.forEach((key, value) -> {
            if (key.contains(".")) {
                // Handle nested keys like "states[0].name"
                setNestedValue(journey, key, value);
            } else {
                journey.put(key, value);
            }
        });

        return journey;
    }

    @SuppressWarnings("unchecked")
    private void setNestedValue(Map<String, Object> map, String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = map;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (part.contains("[") && part.contains("]")) {
                // Handle array access like "states[0]"
                String arrayKey = part.substring(0, part.indexOf("["));
                int index =
                        Integer.parseInt(part.substring(part.indexOf("[") + 1, part.indexOf("]")));

                Object arrayObj = current.get(arrayKey);
                if (arrayObj instanceof java.util.List) {
                    java.util.List<Map<String, Object>> list =
                            (java.util.List<Map<String, Object>>) arrayObj;
                    current = list.get(index);
                }
            } else {
                current = (Map<String, Object>) current.get(part);
            }
        }

        String lastKey = parts[parts.length - 1];
        if (lastKey.contains("[") && lastKey.contains("]")) {
            String arrayKey = lastKey.substring(0, lastKey.indexOf("["));
            int index = Integer
                    .parseInt(lastKey.substring(lastKey.indexOf("[") + 1, lastKey.indexOf("]")));

            Object arrayObj = current.get(arrayKey);
            if (arrayObj instanceof java.util.List) {
                java.util.List<Map<String, Object>> list =
                        (java.util.List<Map<String, Object>>) arrayObj;
                list.set(index, (Map<String, Object>) value);
            }
        } else {
            current.put(lastKey, value);
        }
    }
}
