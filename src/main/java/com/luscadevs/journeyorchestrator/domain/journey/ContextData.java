package com.luscadevs.journeyorchestrator.domain.journey;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * Immutable value object containing runtime data available for condition evaluation.
 * 
 * This class provides a structured way to access journey instance data, event data,
 * and system data during condition evaluation while maintaining thread safety.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContextData {
    
    /**
     * Journey instance identifier
     */
    private String journeyInstanceId;
    
    /**
     * Current journey state
     */
    private String currentState;
    
    /**
     * Data from the triggering event
     */
    private Map<String, Object> eventData;
    
    /**
     * Journey instance data
     */
    private Map<String, Object> journeyData;
    
    /**
     * System-level data (timestamp, correlationId, etc.)
     */
    private Map<String, Object> systemData;
    
    /**
     * Creates a ContextData with minimal required fields
     * 
     * @param journeyInstanceId Journey instance ID
     * @param currentState Current state
     * @return ContextData with empty event and journey data
     */
    public static ContextData of(String journeyInstanceId, String currentState) {
        return ContextData.builder()
                .journeyInstanceId(journeyInstanceId)
                .currentState(currentState)
                .eventData(Collections.emptyMap())
                .journeyData(Collections.emptyMap())
                .systemData(createDefaultSystemData())
                .build();
    }
    
    /**
     * Creates a ContextData with event data
     * 
     * @param journeyInstanceId Journey instance ID
     * @param currentState Current state
     * @param eventData Event data
     * @return ContextData with event data
     */
    public static ContextData withEvent(String journeyInstanceId, String currentState, Map<String, Object> eventData) {
        return ContextData.builder()
                .journeyInstanceId(journeyInstanceId)
                .currentState(currentState)
                .eventData(eventData != null ? eventData : Collections.emptyMap())
                .journeyData(Collections.emptyMap())
                .systemData(createDefaultSystemData())
                .build();
    }
    
    /**
     * Creates a ContextData with journey data
     * 
     * @param journeyInstanceId Journey instance ID
     * @param currentState Current state
     * @param journeyData Journey data
     * @return ContextData with journey data
     */
    public static ContextData withJourneyData(String journeyInstanceId, String currentState, Map<String, Object> journeyData) {
        return ContextData.builder()
                .journeyInstanceId(journeyInstanceId)
                .currentState(currentState)
                .eventData(Collections.emptyMap())
                .journeyData(journeyData != null ? journeyData : Collections.emptyMap())
                .systemData(createDefaultSystemData())
                .build();
    }
    
    /**
     * Creates default system data with timestamp
     */
    private static Map<String, Object> createDefaultSystemData() {
        Map<String, Object> systemData = new HashMap<>();
        systemData.put("timestamp", Instant.now());
        return systemData;
    }
    
    /**
     * Gets a property value from the context using dot notation
     * 
     * @param propertyPath Property path (e.g., "journeyData.amount", "eventData.priority")
     * @return Property value or null if not found
     */
    public Object getProperty(String propertyPath) {
        if (propertyPath == null || propertyPath.isEmpty()) {
            return null;
        }
        
        String[] parts = propertyPath.split("\\.", 3);
        if (parts.length < 2) {
            return null;
        }
        
        String category = parts[0];
        String property = parts[1];
        
        return switch (category) {
            case "eventData" -> eventData.get(property);
            case "journeyData" -> journeyData.get(property);
            case "systemData" -> systemData.get(property);
            default -> null;
        };
    }
    
    @Override
    public String toString() {
        return "ContextData{" +
               "journeyInstanceId='" + journeyInstanceId + '\'' +
               ", currentState='" + currentState + '\'' +
               ", eventData=" + eventData +
               ", journeyData=" + journeyData +
               ", systemData=" + systemData +
               '}';
    }
}
