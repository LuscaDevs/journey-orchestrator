package com.luscadevs.journeyorchestrator.domain.journey;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import java.util.HashMap;
import java.time.Instant;

/**
 * Unit tests for ContextData value object.
 * 
 * Tests cover immutability, property access, and factory methods for journey context data during
 * condition evaluation.
 */
class ContextDataTest {

    @Test
    @DisplayName("Should create ContextData with minimal required fields")
    void shouldCreateContextDataWithMinimalRequiredFields() {
        // Given
        String journeyInstanceId = "journey-123";
        String currentState = "START";

        // When
        ContextData contextData = ContextData.of(journeyInstanceId, currentState);

        // Then
        assertNotNull(contextData);
        assertEquals(journeyInstanceId, contextData.getJourneyInstanceId());
        assertEquals(currentState, contextData.getCurrentState());
        assertNotNull(contextData.getEventData());
        assertNotNull(contextData.getJourneyData());
        assertNotNull(contextData.getSystemData());
        assertTrue(contextData.getSystemData().containsKey("timestamp"));
    }

    @Test
    @DisplayName("Should create ContextData with event data")
    void shouldCreateContextDataWithEventData() {
        // Given
        String journeyInstanceId = "journey-123";
        String currentState = "START";
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("priority", "HIGH");
        eventData.put("source", "WEB");

        // When
        ContextData contextData = ContextData.withEvent(journeyInstanceId, currentState, eventData);

        // Then
        assertNotNull(contextData);
        assertEquals(journeyInstanceId, contextData.getJourneyInstanceId());
        assertEquals(currentState, contextData.getCurrentState());
        assertEquals(eventData, contextData.getEventData());
        assertNotNull(contextData.getJourneyData());
        assertNotNull(contextData.getSystemData());
    }

    @Test
    @DisplayName("Should create ContextData with journey data")
    void shouldCreateContextDataWithJourneyData() {
        // Given
        String journeyInstanceId = "journey-123";
        String currentState = "START";
        Map<String, Object> journeyData = new HashMap<>();
        journeyData.put("amount", 1500);
        journeyData.put("customerType", "PREMIUM");

        // When
        ContextData contextData =
                ContextData.withJourneyData(journeyInstanceId, currentState, journeyData);

        // Then
        assertNotNull(contextData);
        assertEquals(journeyInstanceId, contextData.getJourneyInstanceId());
        assertEquals(currentState, contextData.getCurrentState());
        assertEquals(journeyData, contextData.getJourneyData());
        assertNotNull(contextData.getEventData());
        assertNotNull(contextData.getSystemData());
    }

    @Test
    @DisplayName("Should get property value correctly for journey data")
    void shouldGetPropertyValueCorrectlyForJourneyData() {
        // Given
        Map<String, Object> journeyData = new HashMap<>();
        journeyData.put("amount", 1500);
        journeyData.put("customerType", "PREMIUM");

        ContextData contextData = ContextData.builder().journeyInstanceId("journey-123")
                .currentState("START").journeyData(journeyData).build();

        // When & Then
        assertEquals(1500, contextData.getProperty("journeyData.amount"));
        assertEquals("PREMIUM", contextData.getProperty("journeyData.customerType"));
        assertNull(contextData.getProperty("journeyData.nonexistent"));
    }

    @Test
    @DisplayName("Should get property value correctly for event data")
    void shouldGetPropertyValueCorrectlyForEventData() {
        // Given
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("priority", "HIGH");
        eventData.put("source", "WEB");

        ContextData contextData = ContextData.builder().journeyInstanceId("journey-123")
                .currentState("START").eventData(eventData).build();

        // When & Then
        assertEquals("HIGH", contextData.getProperty("eventData.priority"));
        assertEquals("WEB", contextData.getProperty("eventData.source"));
        assertNull(contextData.getProperty("eventData.nonexistent"));
    }

    @Test
    @DisplayName("Should get property value correctly for system data")
    void shouldGetPropertyValueCorrectlyForSystemData() {
        // Given
        ContextData contextData = ContextData.of("journey-123", "START");

        // When & Then
        assertNotNull(contextData.getProperty("systemData.timestamp"));
        assertTrue(contextData.getProperty("systemData.timestamp") instanceof Instant);
        assertNull(contextData.getProperty("systemData.nonexistent"));
    }

    @Test
    @DisplayName("Should return null for invalid property path")
    void shouldReturnNullForInvalidPropertyPath() {
        // Given
        ContextData contextData = ContextData.of("journey-123", "START");

        // When & Then
        assertNull(contextData.getProperty(null));
        assertNull(contextData.getProperty(""));
        assertNull(contextData.getProperty("invalid"));
        assertNull(contextData.getProperty("invalid.path"));
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        Map<String, Object> journeyData = new HashMap<>();
        journeyData.put("amount", 1500);

        ContextData contextData = ContextData.builder().journeyInstanceId("journey-123")
                .currentState("START").journeyData(journeyData).build();

        // When
        String toString = contextData.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("ContextData"));
        assertTrue(toString.contains("journeyInstanceId"));
        assertTrue(toString.contains("currentState"));
        assertTrue(toString.contains("journeyData"));
        assertTrue(toString.contains("1500"));
    }
}
