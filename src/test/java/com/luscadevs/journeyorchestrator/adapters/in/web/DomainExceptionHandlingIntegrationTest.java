package com.luscadevs.journeyorchestrator.adapters.in.web;

import com.luscadevs.journeyorchestrator.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests to verify that domain exceptions are properly handled
 * by the GlobalExceptionHandler and mapped to correct HTTP responses.
 */
class DomainExceptionHandlingIntegrationTest {
    
    @Test
    void shouldHandleJourneyDefinitionNotFoundException() {
        // Given
        String journeyDefinitionId = "test-123";
        JourneyDefinitionNotFoundException exception = new JourneyDefinitionNotFoundException(journeyDefinitionId);
        HttpServletRequest mockRequest = createMockRequest("/api/journeys/definitions/" + journeyDefinitionId);
        
        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);
        
        // Then
        assertEquals("JOURNEY_001", response.getErrorCode());
        assertEquals(404, response.getProblemDetail().getStatus());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getProblemDetail().getStatus());
        assertEquals("Journey definition not found", response.getProblemDetail().getTitle());
        assertEquals("Journey definition with ID 'test-123' not found", response.getProblemDetail().getDetail());
        assertEquals(journeyDefinitionId, response.getAdditionalContext().get("journeyDefinitionId"));
    }
    
    @Test
    void shouldHandleJourneyInstanceNotFoundException() {
        // Given
        String journeyInstanceId = "instance-456";
        JourneyInstanceNotFoundException exception = new JourneyInstanceNotFoundException(journeyInstanceId);
        HttpServletRequest mockRequest = createMockRequest("/api/journeys/instances/" + journeyInstanceId);
        
        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);
        
        // Then
        assertEquals("JOURNEY_101", response.getErrorCode());
        assertEquals(404, response.getProblemDetail().getStatus());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getProblemDetail().getStatus());
        assertEquals("Journey instance not found", response.getProblemDetail().getTitle());
        assertEquals("Journey instance with ID 'instance-456' not found", response.getProblemDetail().getDetail());
        assertEquals(journeyInstanceId, response.getAdditionalContext().get("journeyInstanceId"));
    }
    
    @Test
    void shouldHandleInvalidStateTransitionException() {
        // Given
        String journeyInstanceId = "instance-789";
        String fromState = "RUNNING";
        String toState = "CANCELLED";
        InvalidStateTransitionException exception = new InvalidStateTransitionException(journeyInstanceId, fromState, toState);
        HttpServletRequest mockRequest = createMockRequest("/api/journeys/instances/" + journeyInstanceId + "/transition");
        
        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);
        
        // Then
        assertEquals("STATE_001", response.getErrorCode());
        assertEquals(422, response.getProblemDetail().getStatus());
        assertEquals(HttpStatus.valueOf(422).value(), response.getProblemDetail().getStatus());
        assertEquals("Invalid state transition", response.getProblemDetail().getTitle());
        assertEquals("Invalid state transition from 'RUNNING' to 'CANCELLED' for journey instance 'instance-789'", 
                     response.getProblemDetail().getDetail());
        assertEquals(journeyInstanceId, response.getAdditionalContext().get("journeyInstanceId"));
        assertEquals(fromState, response.getAdditionalContext().get("fromState"));
        assertEquals(toState, response.getAdditionalContext().get("toState"));
    }
    
    @Test
    void shouldHandleJourneyAlreadyCompletedException() {
        // Given
        String journeyInstanceId = "completed-123";
        JourneyAlreadyCompletedException exception = new JourneyAlreadyCompletedException(journeyInstanceId);
        HttpServletRequest mockRequest = createMockRequest("/api/journeys/instances/" + journeyInstanceId + "/complete");
        
        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);
        
        // Then
        assertEquals("JOURNEY_102", response.getErrorCode());
        assertEquals(422, response.getProblemDetail().getStatus());
        assertEquals(HttpStatus.valueOf(422).value(), response.getProblemDetail().getStatus());
        assertEquals("Journey has already completed", response.getProblemDetail().getTitle());
        assertEquals("Journey instance 'completed-123' has already completed and cannot be modified", 
                     response.getProblemDetail().getDetail());
        assertEquals(journeyInstanceId, response.getAdditionalContext().get("journeyInstanceId"));
        assertEquals("COMPLETED", response.getAdditionalContext().get("completedState"));
    }
    
    @Test
    void shouldMaintainRfc9457ComplianceForAllDomainExceptions() {
        // Given - Test all domain exceptions for RFC 9457 compliance
        DomainException[] exceptions = {
            new JourneyDefinitionNotFoundException("test"),
            new JourneyInstanceNotFoundException("test"),
            new InvalidStateTransitionException("test", "FROM", "TO"),
            new JourneyAlreadyCompletedException("test")
        };
        
        for (DomainException exception : exceptions) {
            HttpServletRequest mockRequest = createMockRequest("/api/test");
            
            // When
            ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);
            
            // Then - Verify RFC 9457 compliance
            assertNotNull(response.getProblemDetail().getType(), "Type should not be null for " + exception.getClass().getSimpleName());
            assertNotNull(response.getProblemDetail().getTitle(), "Title should not be null for " + exception.getClass().getSimpleName());
            assertNotNull(response.getProblemDetail().getStatus(), "Status should not be null for " + exception.getClass().getSimpleName());
            assertNotNull(response.getProblemDetail().getDetail(), "Detail should not be null for " + exception.getClass().getSimpleName());
            assertNotNull(response.getProblemDetail().getInstance(), "Instance should not be null for " + exception.getClass().getSimpleName());
            assertNotNull(response.getProblemDetail().getProperties().get("errorCode"), "ErrorCode should not be null for " + exception.getClass().getSimpleName());
            assertNotNull(response.getProblemDetail().getProperties().get("timestamp"), "Timestamp should not be null for " + exception.getClass().getSimpleName());
        }
    }
    
    @Test
    void shouldMapDomainExceptionsToCorrectHttpStatusCodes() {
        // Given & When & Then - Verify HTTP status mapping
        assertEquals(404, getHttpStatusForException(new JourneyDefinitionNotFoundException("test")));
        assertEquals(404, getHttpStatusForException(new JourneyInstanceNotFoundException("test")));
        assertEquals(422, getHttpStatusForException(new InvalidStateTransitionException("test", "FROM", "TO")));
        assertEquals(422, getHttpStatusForException(new JourneyAlreadyCompletedException("test")));
    }
    
    @Test
    void shouldIncludeRequestPathInAllResponses() {
        // Given
        String requestPath = "/api/journeys/test-path";
        HttpServletRequest mockRequest = createMockRequest(requestPath);
        JourneyDefinitionNotFoundException exception = new JourneyDefinitionNotFoundException("test");
        
        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);
        
        // Then
        assertEquals(requestPath, response.getPath());
    }
    
    @Test
    void shouldIncludeTimestampInAllResponses() {
        // Given
        HttpServletRequest mockRequest = createMockRequest("/api/test");
        JourneyInstanceNotFoundException exception = new JourneyInstanceNotFoundException("test");
        
        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);
        
        // Then
        assertNotNull(response.getTimestamp());
        assertNotNull(response.getProblemDetail().getProperties().get("timestamp"));
    }
    
    /**
     * Helper method to create a mock HttpServletRequest.
     */
    private HttpServletRequest createMockRequest(String requestURI) {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn(requestURI);
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getContextPath()).thenReturn("");
        when(mockRequest.getServletPath()).thenReturn("");
        return mockRequest;
    }
    
    /**
     * Helper method to extract HTTP status from exception.
     */
    private int getHttpStatusForException(DomainException exception) {
        HttpServletRequest mockRequest = createMockRequest("/api/test");
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);
        return response.getProblemDetail().getStatus();
    }
}
