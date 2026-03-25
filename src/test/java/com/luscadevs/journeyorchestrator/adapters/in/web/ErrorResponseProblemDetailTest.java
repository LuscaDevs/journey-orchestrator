package com.luscadevs.journeyorchestrator.adapters.in.web;

import com.luscadevs.journeyorchestrator.domain.exception.DomainException;
import com.luscadevs.journeyorchestrator.domain.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ErrorResponseProblemDetail to ensure RFC 9457 compliance
 * and proper HTTP status mapping for all error codes.
 */
class ErrorResponseProblemDetailTest {

    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void shouldCreateErrorResponseFromDomainException() {
        // Given
        TestDomainException testException = new TestDomainException(
                ErrorCode.JOURNEY_DEFINITION_NOT_FOUND,
                "Journey definition not found");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(testException, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals("JOURNEY_001", response.getErrorCode());
        assertEquals("Journey definition not found", response.getProblemDetail().getDetail());
        assertEquals(404, response.getProblemDetail().getStatus());
        assertEquals("/api/test", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void shouldMapJourneyDefinitionNotFoundTo404() {
        // Given
        TestDomainException exception = new TestDomainException(
                ErrorCode.JOURNEY_DEFINITION_NOT_FOUND,
                "Not found");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then
        assertEquals(404, response.getProblemDetail().getStatus());
        assertEquals("JOURNEY_001", response.getErrorCode());
        assertEquals("Journey definition not found", response.getProblemDetail().getTitle());
    }

    @Test
    void shouldMapJourneyInstanceNotFoundTo404() {
        // Given
        TestDomainException exception = new TestDomainException(
                ErrorCode.JOURNEY_INSTANCE_NOT_FOUND,
                "Instance not found");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then
        assertEquals(404, response.getProblemDetail().getStatus());
        assertEquals("JOURNEY_101", response.getErrorCode());
    }

    @Test
    void shouldMapInvalidStateTransitionTo422() {
        // Given
        TestDomainException exception = new TestDomainException(
                ErrorCode.INVALID_STATE_TRANSITION,
                "Invalid transition");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then
        assertEquals(422, response.getProblemDetail().getStatus());
        assertEquals("STATE_001", response.getErrorCode());
    }

    @Test
    void shouldMapConcurrentModificationTo409() {
        // Given
        TestDomainException exception = new TestDomainException(
                ErrorCode.CONCURRENT_MODIFICATION,
                "Concurrent modification");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then
        assertEquals(409, response.getProblemDetail().getStatus());
        assertEquals("CONFLICT_001", response.getErrorCode());
    }

    @Test
    void shouldMapValidationErrorsTo400() {
        // Given
        TestDomainException exception = new TestDomainException(
                ErrorCode.VALIDATION_FAILED,
                "Validation failed");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then
        assertEquals(400, response.getProblemDetail().getStatus());
        assertEquals("VALIDATION_001", response.getErrorCode());
    }

    @Test
    void shouldMapSystemErrorsTo500() {
        // Given
        TestDomainException exception = new TestDomainException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Internal error");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then
        assertEquals(500, response.getProblemDetail().getStatus());
        assertEquals("SYSTEM_001", response.getErrorCode());
    }

    @Test
    void shouldIncludeExceptionContext() {
        // Given
        TestDomainException exception = new TestDomainException(
                ErrorCode.JOURNEY_DEFINITION_NOT_FOUND,
                "Not found");
        exception.withContext("journeyDefinitionId", "test-123");
        exception.withContext("userId", "user-456");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then
        Map<String, Object> context = response.getAdditionalContext();
        assertEquals(2, context.size());
        assertEquals("test-123", context.get("journeyDefinitionId"));
        assertEquals("user-456", context.get("userId"));
    }

    @Test
    void shouldCreateValidProblemDetailStructure() {
        // Given
        TestDomainException exception = new TestDomainException(
                ErrorCode.JOURNEY_DEFINITION_NOT_FOUND,
                "Test message");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then - Verify RFC 9457 compliance
        assertNotNull(response.getProblemDetail().getType());
        assertNotNull(response.getProblemDetail().getTitle());
        assertNotNull(response.getProblemDetail().getStatus());
        assertNotNull(response.getProblemDetail().getDetail());
        assertNotNull(response.getProblemDetail().getInstance());
        assertEquals("JOURNEY_001", response.getProblemDetail().getProperties().get("errorCode"));

        // Verify type URI format
        assertTrue(response.getProblemDetail().getType().toString().contains("journey_001"));
    }

    @Test
    void shouldSetTimestampToCurrentTime() {
        // Given
        TestDomainException exception = new TestDomainException(
                ErrorCode.JOURNEY_DEFINITION_NOT_FOUND,
                "Test message");
        Instant beforeTime = Instant.now().minusSeconds(1);

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then
        String timestampStr = (String) response.getProblemDetail().getProperties().get("timestamp");
        assertNotNull(timestampStr);

        Instant timestamp = Instant.parse(timestampStr);
        assertTrue(timestamp.isAfter(beforeTime));
        assertTrue(timestamp.isBefore(Instant.now().plusSeconds(1)));
    }

    // Test helper class for DomainException
    private static class TestDomainException extends DomainException {
        public TestDomainException(ErrorCode errorCode, String details) {
            super(errorCode, details);
        }
    }
}
