package com.luscadevs.journeyorchestrator.domain.exception;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JourneyAlreadyCompletedException to ensure proper
 * exception creation, context handling, and error code mapping.
 */
class JourneyAlreadyCompletedExceptionTest {
    
    @Test
    void shouldCreateExceptionWithCorrectErrorCode() {
        // Given
        String journeyInstanceId = "completed-123";
        
        // When
        JourneyAlreadyCompletedException exception = new JourneyAlreadyCompletedException(journeyInstanceId);
        
        // Then
        assertEquals(ErrorCode.JOURNEY_ALREADY_COMPLETED, exception.getErrorCode());
        assertEquals("JOURNEY_102", exception.getErrorCode().getCode());
    }
    
    @Test
    void shouldCreateExceptionWithCorrectMessage() {
        // Given
        String journeyInstanceId = "completed-456";
        
        // When
        JourneyAlreadyCompletedException exception = new JourneyAlreadyCompletedException(journeyInstanceId);
        
        // Then
        assertEquals("Journey instance 'completed-456' has already completed and cannot be modified", 
                     exception.getDetails());
    }
    
    @Test
    void shouldStoreJourneyInstanceId() {
        // Given
        String journeyInstanceId = "completed-789";
        
        // When
        JourneyAlreadyCompletedException exception = new JourneyAlreadyCompletedException(journeyInstanceId);
        
        // Then
        assertEquals(journeyInstanceId, exception.getJourneyInstanceId());
    }
    
    @Test
    void shouldIncludeContextInformation() {
        // Given
        String journeyInstanceId = "completed-context";
        
        // When
        JourneyAlreadyCompletedException exception = new JourneyAlreadyCompletedException(journeyInstanceId);
        
        // Then
        Map<String, Object> context = exception.getContext();
        assertEquals(2, context.size());
        assertEquals(journeyInstanceId, context.get("journeyInstanceId"));
        assertEquals("COMPLETED", context.get("completedState"));
    }
    
    @Test
    void shouldHaveCorrectHashCodeAndEquals() {
        // Given
        String journeyInstanceId = "completed-same";
        JourneyAlreadyCompletedException exception1 = new JourneyAlreadyCompletedException(journeyInstanceId);
        JourneyAlreadyCompletedException exception2 = new JourneyAlreadyCompletedException(journeyInstanceId);
        JourneyAlreadyCompletedException exception3 = new JourneyAlreadyCompletedException("different");
        
        // Then & When
        assertEquals(exception1, exception2);
        assertEquals(exception1.hashCode(), exception2.hashCode());
        assertNotEquals(exception1, exception3);
        assertNotEquals(exception1.hashCode(), exception3.hashCode());
    }
    
    @Test
    void shouldExtendDomainException() {
        // Given
        String journeyInstanceId = "completed-inheritance";
        
        // When
        JourneyAlreadyCompletedException exception = new JourneyAlreadyCompletedException(journeyInstanceId);
        
        // Then
        assertTrue(exception instanceof DomainException);
        assertTrue(exception instanceof RuntimeException);
    }
    
    @Test
    void shouldHandleNullJourneyInstanceId() {
        // Given
        String journeyInstanceId = null;
        
        // When
        JourneyAlreadyCompletedException exception = new JourneyAlreadyCompletedException(journeyInstanceId);
        
        // Then
        assertEquals("Journey instance 'null' has already completed and cannot be modified", exception.getDetails());
        assertNull(exception.getJourneyInstanceId());
        assertEquals(null, exception.getContext().get("journeyInstanceId"));
        assertEquals("COMPLETED", exception.getContext().get("completedState"));
    }
    
    @Test
    void shouldHandleEmptyJourneyInstanceId() {
        // Given
        String journeyInstanceId = "";
        
        // When
        JourneyAlreadyCompletedException exception = new JourneyAlreadyCompletedException(journeyInstanceId);
        
        // Then
        assertEquals("Journey instance '' has already completed and cannot be modified", exception.getDetails());
        assertEquals("", exception.getJourneyInstanceId());
        assertEquals("", exception.getContext().get("journeyInstanceId"));
        assertEquals("COMPLETED", exception.getContext().get("completedState"));
    }
}
