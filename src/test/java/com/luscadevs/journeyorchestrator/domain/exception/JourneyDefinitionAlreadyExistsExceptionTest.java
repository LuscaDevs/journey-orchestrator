package com.luscadevs.journeyorchestrator.domain.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JourneyDefinitionAlreadyExistsException domain exception.
 */
@DisplayName("JourneyDefinitionAlreadyExistsException Tests")
class JourneyDefinitionAlreadyExistsExceptionTest {

    @Test
    @DisplayName("Should create exception with journey definition ID")
    void shouldCreateExceptionWithJourneyDefinitionId() {
        // Given
        String journeyDefinitionId = "TEST_JOURNEY:1";

        // When
        JourneyDefinitionAlreadyExistsException exception = 
            new JourneyDefinitionAlreadyExistsException(journeyDefinitionId);

        // Then
        assertEquals(journeyDefinitionId, exception.getJourneyDefinitionId());
        assertEquals(ErrorCode.JOURNEY_DEFINITION_ALREADY_EXISTS, exception.getErrorCode());
        assertEquals("Journey definition already exists: " + journeyDefinitionId, exception.getDetails());
        assertNotNull(exception.getContext());
    }

    @Test
    @DisplayName("Should have correct error code")
    void shouldHaveCorrectErrorCode() {
        // Given
        String journeyDefinitionId = "TEST_JOURNEY:1";

        // When
        JourneyDefinitionAlreadyExistsException exception = 
            new JourneyDefinitionAlreadyExistsException(journeyDefinitionId);

        // Then
        assertEquals(ErrorCode.JOURNEY_DEFINITION_ALREADY_EXISTS, exception.getErrorCode());
        assertEquals("JOURNEY_002", exception.getErrorCode().getCode());
        assertEquals("Journey definition already exists", exception.getErrorCode().getDefaultMessage());
    }

    @Test
    @DisplayName("Should have correct details message")
    void shouldHaveCorrectDetailsMessage() {
        // Given
        String journeyDefinitionId = "ORDER_PROCESSING:2";

        // When
        JourneyDefinitionAlreadyExistsException exception = 
            new JourneyDefinitionAlreadyExistsException(journeyDefinitionId);

        // Then
        assertEquals("Journey definition already exists: " + journeyDefinitionId, exception.getDetails());
    }

    @Test
    @DisplayName("Should be a domain exception")
    void shouldBeDomainException() {
        // Given
        String journeyDefinitionId = "TEST_JOURNEY:1";

        // When
        JourneyDefinitionAlreadyExistsException exception = 
            new JourneyDefinitionAlreadyExistsException(journeyDefinitionId);

        // Then
        assertTrue(exception instanceof DomainException);
        assertInstanceOf(DomainException.class, exception);
    }

    @Test
    @DisplayName("Should have correct hashCode and equals")
    void shouldHaveCorrectHashCodeAndEquals() {
        // Given
        String journeyDefinitionId = "TEST_JOURNEY:1";

        // When
        JourneyDefinitionAlreadyExistsException exception1 = 
            new JourneyDefinitionAlreadyExistsException(journeyDefinitionId);
        JourneyDefinitionAlreadyExistsException exception2 = 
            new JourneyDefinitionAlreadyExistsException(journeyDefinitionId);
        JourneyDefinitionAlreadyExistsException exception3 = 
            new JourneyDefinitionAlreadyExistsException("OTHER_JOURNEY:1");

        // Then
        assertEquals(exception1, exception2);
        assertEquals(exception1.hashCode(), exception2.hashCode());
        assertNotEquals(exception1, exception3);
        assertNotEquals(exception1.hashCode(), exception3.hashCode());
        assertNotEquals(null, exception1);
        assertNotEquals(exception1, new Object());
    }

    @Test
    @DisplayName("Should handle null journey definition ID gracefully")
    void shouldHandleNullJourneyDefinitionId() {
        // Given
        String journeyDefinitionId = null;

        // When
        JourneyDefinitionAlreadyExistsException exception = 
            new JourneyDefinitionAlreadyExistsException(journeyDefinitionId);

        // Then
        assertNull(exception.getJourneyDefinitionId());
        assertEquals(ErrorCode.JOURNEY_DEFINITION_ALREADY_EXISTS, exception.getErrorCode());
        assertEquals("Journey definition already exists: null", exception.getDetails());
    }

    @Test
    @DisplayName("Should handle empty journey definition ID")
    void shouldHandleEmptyJourneyDefinitionId() {
        // Given
        String journeyDefinitionId = "";

        // When
        JourneyDefinitionAlreadyExistsException exception = 
            new JourneyDefinitionAlreadyExistsException(journeyDefinitionId);

        // Then
        assertEquals(journeyDefinitionId, exception.getJourneyDefinitionId());
        assertEquals("Journey definition already exists: ", exception.getDetails());
    }
}
