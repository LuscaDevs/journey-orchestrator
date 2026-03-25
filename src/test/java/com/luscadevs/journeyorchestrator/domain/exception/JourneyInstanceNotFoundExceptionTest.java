package com.luscadevs.journeyorchestrator.domain.exception;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JourneyInstanceNotFoundException to ensure proper
 * exception creation, context handling, and error code mapping.
 */
class JourneyInstanceNotFoundExceptionTest {
    
    @Test
    void shouldCreateExceptionWithCorrectErrorCode() {
        // Given
        String journeyInstanceId = "instance-123";
        
        // When
        JourneyInstanceNotFoundException exception = new JourneyInstanceNotFoundException(journeyInstanceId);
        
        // Then
        assertEquals(ErrorCode.JOURNEY_INSTANCE_NOT_FOUND, exception.getErrorCode());
        assertEquals("JOURNEY_101", exception.getErrorCode().getCode());
    }
    
    @Test
    void shouldCreateExceptionWithCorrectMessage() {
        // Given
        String journeyInstanceId = "instance-456";
        
        // When
        JourneyInstanceNotFoundException exception = new JourneyInstanceNotFoundException(journeyInstanceId);
        
        // Then
        assertEquals("Journey instance with ID 'instance-456' not found", exception.getDetails());
    }
    
    @Test
    void shouldStoreJourneyInstanceId() {
        // Given
        String journeyInstanceId = "instance-789";
        
        // When
        JourneyInstanceNotFoundException exception = new JourneyInstanceNotFoundException(journeyInstanceId);
        
        // Then
        assertEquals(journeyInstanceId, exception.getJourneyInstanceId());
    }
    
    @Test
    void shouldIncludeContextInformation() {
        // Given
        String journeyInstanceId = "instance-context";
        
        // When
        JourneyInstanceNotFoundException exception = new JourneyInstanceNotFoundException(journeyInstanceId);
        
        // Then
        Map<String, Object> context = exception.getContext();
        assertEquals(1, context.size());
        assertEquals(journeyInstanceId, context.get("journeyInstanceId"));
    }
    
    @Test
    void shouldHaveCorrectHashCodeAndEquals() {
        // Given
        String journeyInstanceId = "instance-same";
        JourneyInstanceNotFoundException exception1 = new JourneyInstanceNotFoundException(journeyInstanceId);
        JourneyInstanceNotFoundException exception2 = new JourneyInstanceNotFoundException(journeyInstanceId);
        JourneyInstanceNotFoundException exception3 = new JourneyInstanceNotFoundException("different");
        
        // Then & When
        assertEquals(exception1, exception2);
        assertEquals(exception1.hashCode(), exception2.hashCode());
        assertNotEquals(exception1, exception3);
        assertNotEquals(exception1.hashCode(), exception3.hashCode());
    }
    
    @Test
    void shouldExtendDomainException() {
        // Given
        String journeyInstanceId = "instance-inheritance";
        
        // When
        JourneyInstanceNotFoundException exception = new JourneyInstanceNotFoundException(journeyInstanceId);
        
        // Then
        assertTrue(exception instanceof DomainException);
        assertTrue(exception instanceof RuntimeException);
    }
    
    @Test
    void shouldHandleNullJourneyInstanceId() {
        // Given
        String journeyInstanceId = null;
        
        // When
        JourneyInstanceNotFoundException exception = new JourneyInstanceNotFoundException(journeyInstanceId);
        
        // Then
        assertEquals("Journey instance with ID 'null' not found", exception.getDetails());
        assertNull(exception.getJourneyInstanceId());
        assertEquals(null, exception.getContext().get("journeyInstanceId"));
    }
    
    @Test
    void shouldHandleEmptyJourneyInstanceId() {
        // Given
        String journeyInstanceId = "";
        
        // When
        JourneyInstanceNotFoundException exception = new JourneyInstanceNotFoundException(journeyInstanceId);
        
        // Then
        assertEquals("Journey instance with ID '' not found", exception.getDetails());
        assertEquals("", exception.getJourneyInstanceId());
        assertEquals("", exception.getContext().get("journeyInstanceId"));
    }
}
