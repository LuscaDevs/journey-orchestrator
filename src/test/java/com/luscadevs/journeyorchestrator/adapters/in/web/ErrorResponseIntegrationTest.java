package com.luscadevs.journeyorchestrator.adapters.in.web;

import com.luscadevs.journeyorchestrator.domain.exception.DomainException;
import com.luscadevs.journeyorchestrator.domain.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.HttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests to verify error response format consistency across
 * different exception types and ensure RFC 9457 compliance.
 */
class ErrorResponseIntegrationTest {

    @Test
    void shouldReturnConsistentErrorResponseFormatForDomainException() {
        // Given
        TestDomainException exception = new TestDomainException(
                ErrorCode.JOURNEY_DEFINITION_NOT_FOUND,
                "Not found");
        HttpServletRequest mockRequest = createMockRequest("/api/test");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then - Verify response structure
        assertNotNull(response);
        assertEquals("JOURNEY_001", response.getErrorCode());
        assertEquals("Not found", response.getProblemDetail().getDetail());
        assertEquals(404, response.getProblemDetail().getStatus());
        assertEquals("/api/test", response.getPath());
        assertNotNull(response.getTimestamp());

        // Verify RFC 9457 compliance
        assertNotNull(response.getProblemDetail().getType());
        assertNotNull(response.getProblemDetail().getTitle());
        assertNotNull(response.getProblemDetail().getStatus());
        assertNotNull(response.getProblemDetail().getDetail());
        assertNotNull(response.getProblemDetail().getInstance());
        assertEquals("JOURNEY_001", response.getProblemDetail().getProperties().get("errorCode"));
    }

    @Test
    void shouldReturnCorrectHttpStatusForDifferentErrorCodes() {
        // Test different error codes map to correct HTTP status

        // Journey Definition Not Found -> 404
        verifyErrorResponseForErrorCode(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, 404);

        // Journey Instance Not Found -> 404
        verifyErrorResponseForErrorCode(ErrorCode.JOURNEY_INSTANCE_NOT_FOUND, 404);

        // Invalid State Transition -> 422
        verifyErrorResponseForErrorCode(ErrorCode.INVALID_STATE_TRANSITION, 422);

        // Concurrent Modification -> 409
        verifyErrorResponseForErrorCode(ErrorCode.CONCURRENT_MODIFICATION, 409);

        // Validation Failed -> 400
        verifyErrorResponseForErrorCode(ErrorCode.VALIDATION_FAILED, 400);

        // Internal Server Error -> 500
        verifyErrorResponseForErrorCode(ErrorCode.INTERNAL_SERVER_ERROR, 500);
    }

    @Test
    void shouldIncludeErrorContextInResponse() {
        // Given - Create an exception with context
        TestDomainException exception = new TestDomainException(
                ErrorCode.JOURNEY_DEFINITION_NOT_FOUND,
                "Not found");
        exception.withContext("journeyDefinitionId", "test-123");
        exception.withContext("userId", "user-456");

        HttpServletRequest mockRequest = createMockRequest("/test");

        // When - Convert to error response
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then - Verify context is included
        var context = response.getAdditionalContext();
        assertEquals(2, context.size());
        assertEquals("test-123", context.get("journeyDefinitionId"));
        assertEquals("user-456", context.get("userId"));
    }

    @Test
    void shouldValidateProblemDetailCompliance() {
        // Given - Create a domain exception
        TestDomainException exception = new TestDomainException(
                ErrorCode.JOURNEY_DEFINITION_NOT_FOUND,
                "Test message");

        HttpServletRequest mockRequest = createMockRequest("/test");

        // When - Convert to error response
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then - Verify RFC 9457 compliance
        var problemDetail = response.getProblemDetail();

        // Required fields
        assertNotNull(problemDetail.getType(), "Type should not be null");
        assertNotNull(problemDetail.getTitle(), "Title should not be null");
        assertNotNull(problemDetail.getStatus(), "Status should not be null");
        assertNotNull(problemDetail.getDetail(), "Detail should not be null");
        assertNotNull(problemDetail.getInstance(), "Instance should not be null");

        // Verify type is a valid URI
        assertTrue(problemDetail.getType().toString().startsWith("https://api.journey-orchestrator.com/errors/"));

        // Verify errorCode is included as property
        assertEquals("JOURNEY_001", problemDetail.getProperties().get("errorCode"));
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
     * Helper method to verify HTTP status mapping for specific error codes.
     */
    private void verifyErrorResponseForErrorCode(ErrorCode errorCode, int expectedStatus) {
        // Given
        TestDomainException exception = new TestDomainException(errorCode, "Test message");
        HttpServletRequest mockRequest = createMockRequest("/test");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then
        assertEquals(expectedStatus, response.getProblemDetail().getStatus());
    }

    // Test helper class for DomainException
    private static class TestDomainException extends DomainException {
        public TestDomainException(ErrorCode errorCode, String details) {
            super(errorCode, details);
        }
    }
}
