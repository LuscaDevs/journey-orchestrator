package com.luscadevs.journeyorchestrator.domain.exception;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JourneyDefinitionNotFoundException to ensure proper
 * exception creation, context handling, and error code mapping.
 */
class JourneyDefinitionNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithCorrectErrorCode() {
        // Given
        String journeyDefinitionId = "test-123";

        // When
        JourneyDefinitionNotFoundException exception = new JourneyDefinitionNotFoundException(journeyDefinitionId);

        // Then
        assertEquals(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, exception.getErrorCode());
        assertEquals("JOURNEY_001", exception.getErrorCode().getCode());
    }

    @Test
    void shouldCreateExceptionWithCorrectMessage() {
        // Given
        String journeyDefinitionId = "test-123";

        // When
        JourneyDefinitionNotFoundException exception = new JourneyDefinitionNotFoundException(journeyDefinitionId);

        // Then
        assertEquals("Journey definition with ID 'test-123' not found", exception.getDetails());
    }

    @Test
    void shouldStoreJourneyDefinitionId() {
        // Given
        String journeyDefinitionId = "test-456";

        // When
        JourneyDefinitionNotFoundException exception = new JourneyDefinitionNotFoundException(journeyDefinitionId);

        // Then
        assertEquals(journeyDefinitionId, exception.getJourneyDefinitionId());
    }

    @Test
    void shouldIncludeContextInformation() {
        // Given
        String journeyDefinitionId = "test-789";

        // When
        JourneyDefinitionNotFoundException exception = new JourneyDefinitionNotFoundException(journeyDefinitionId);

        // Then
        Map<String, Object> context = exception.getContext();
        assertEquals(1, context.size());
        assertEquals(journeyDefinitionId, context.get("journeyDefinitionId"));
    }

    @Test
    void shouldHaveCorrectHashCodeAndEquals() {
        // Given
        String journeyDefinitionId = "test-same";
        JourneyDefinitionNotFoundException exception1 = new JourneyDefinitionNotFoundException(journeyDefinitionId);
        JourneyDefinitionNotFoundException exception2 = new JourneyDefinitionNotFoundException(journeyDefinitionId);
        JourneyDefinitionNotFoundException exception3 = new JourneyDefinitionNotFoundException("different");

        // Then & When
        assertEquals(exception1, exception2);
        assertEquals(exception1.hashCode(), exception2.hashCode());
        assertNotEquals(exception1, exception3);
        assertNotEquals(exception1.hashCode(), exception3.hashCode());

        // Test equals with different types
        assertNotEquals(exception1, new Object());
        assertNotEquals(exception1, null);
    }

    @Test
    void shouldExtendDomainException() {
        // Given
        String journeyDefinitionId = "test-inheritance";

        // When
        JourneyDefinitionNotFoundException exception = new JourneyDefinitionNotFoundException(journeyDefinitionId);

        // Then
        assertTrue(exception instanceof DomainException);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldHandleNullJourneyDefinitionId() {
        // Given
        String journeyDefinitionId = null;

        // When
        JourneyDefinitionNotFoundException exception = new JourneyDefinitionNotFoundException(journeyDefinitionId);

        // Then
        assertEquals("Journey definition with ID 'null' not found", exception.getDetails());
        assertNull(exception.getJourneyDefinitionId());
        assertEquals(null, exception.getContext().get("journeyDefinitionId"));
    }

    @Test
    void shouldHandleEmptyJourneyDefinitionId() {
        // Given
        String journeyDefinitionId = "";

        // When
        JourneyDefinitionNotFoundException exception = new JourneyDefinitionNotFoundException(journeyDefinitionId);

        // Then
        assertEquals("Journey definition with ID '' not found", exception.getDetails());
        assertEquals("", exception.getJourneyDefinitionId());
        assertEquals("", exception.getContext().get("journeyDefinitionId"));
    }
}
