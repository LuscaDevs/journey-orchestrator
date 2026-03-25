package com.luscadevs.journeyorchestrator.domain.exception;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InvalidStateTransitionException to ensure proper
 * exception creation, context handling, and error code mapping.
 */
class InvalidStateTransitionExceptionTest {
    
    @Test
    void shouldCreateExceptionWithCorrectErrorCode() {
        // Given
        String journeyInstanceId = "instance-123";
        String fromState = "PENDING";
        String toState = "COMPLETED";
        
        // When
        InvalidStateTransitionException exception = new InvalidStateTransitionException(journeyInstanceId, fromState, toState);
        
        // Then
        assertEquals(ErrorCode.INVALID_STATE_TRANSITION, exception.getErrorCode());
        assertEquals("STATE_001", exception.getErrorCode().getCode());
    }
    
    @Test
    void shouldCreateExceptionWithCorrectMessage() {
        // Given
        String journeyInstanceId = "instance-456";
        String fromState = "RUNNING";
        String toState = "CANCELLED";
        
        // When
        InvalidStateTransitionException exception = new InvalidStateTransitionException(journeyInstanceId, fromState, toState);
        
        // Then
        assertEquals("Invalid state transition from 'RUNNING' to 'CANCELLED' for journey instance 'instance-456'", 
                     exception.getDetails());
    }
    
    @Test
    void shouldStoreTransitionDetails() {
        // Given
        String journeyInstanceId = "instance-789";
        String fromState = "STARTED";
        String toState = "FAILED";
        
        // When
        InvalidStateTransitionException exception = new InvalidStateTransitionException(journeyInstanceId, fromState, toState);
        
        // Then
        assertEquals(journeyInstanceId, exception.getJourneyInstanceId());
        assertEquals(fromState, exception.getFromState());
        assertEquals(toState, exception.getToState());
    }
    
    @Test
    void shouldIncludeContextInformation() {
        // Given
        String journeyInstanceId = "instance-context";
        String fromState = "PENDING";
        String toState = "RUNNING";
        
        // When
        InvalidStateTransitionException exception = new InvalidStateTransitionException(journeyInstanceId, fromState, toState);
        
        // Then
        Map<String, Object> context = exception.getContext();
        assertEquals(3, context.size());
        assertEquals(journeyInstanceId, context.get("journeyInstanceId"));
        assertEquals(fromState, context.get("fromState"));
        assertEquals(toState, context.get("toState"));
    }
    
    @Test
    void shouldHaveCorrectHashCodeAndEquals() {
        // Given
        String journeyInstanceId = "instance-same";
        String fromState = "PENDING";
        String toState = "RUNNING";
        
        InvalidStateTransitionException exception1 = new InvalidStateTransitionException(journeyInstanceId, fromState, toState);
        InvalidStateTransitionException exception2 = new InvalidStateTransitionException(journeyInstanceId, fromState, toState);
        InvalidStateTransitionException exception3 = new InvalidStateTransitionException("different", fromState, toState);
        
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
        String fromState = "PENDING";
        String toState = "RUNNING";
        
        // When
        InvalidStateTransitionException exception = new InvalidStateTransitionException(journeyInstanceId, fromState, toState);
        
        // Then
        assertTrue(exception instanceof DomainException);
        assertTrue(exception instanceof RuntimeException);
    }
    
    @Test
    void shouldHandleNullValues() {
        // Given
        String journeyInstanceId = null;
        String fromState = null;
        String toState = null;
        
        // When
        InvalidStateTransitionException exception = new InvalidStateTransitionException(journeyInstanceId, fromState, toState);
        
        // Then
        assertEquals("Invalid state transition from 'null' to 'null' for journey instance 'null'", exception.getDetails());
        assertNull(exception.getJourneyInstanceId());
        assertNull(exception.getFromState());
        assertNull(exception.getToState());
        assertEquals(null, exception.getContext().get("journeyInstanceId"));
        assertEquals(null, exception.getContext().get("fromState"));
        assertEquals(null, exception.getContext().get("toState"));
    }
    
    @Test
    void shouldHandleEmptyValues() {
        // Given
        String journeyInstanceId = "";
        String fromState = "";
        String toState = "";
        
        // When
        InvalidStateTransitionException exception = new InvalidStateTransitionException(journeyInstanceId, fromState, toState);
        
        // Then
        assertEquals("Invalid state transition from '' to '' for journey instance ''", exception.getDetails());
        assertEquals("", exception.getJourneyInstanceId());
        assertEquals("", exception.getFromState());
        assertEquals("", exception.getToState());
        assertEquals("", exception.getContext().get("journeyInstanceId"));
        assertEquals("", exception.getContext().get("fromState"));
        assertEquals("", exception.getContext().get("toState"));
    }
}
