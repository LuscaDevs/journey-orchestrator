package com.luscadevs.journeyorchestrator.adapters.in.web;

import com.luscadevs.journeyorchestrator.domain.exception.DomainException;
import com.luscadevs.journeyorchestrator.domain.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler to ensure proper RFC 9457 compliance
 * and correct HTTP status mapping for domain exceptions.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void shouldHandleDomainExceptionAndReturnProblemDetail() {
        // Given
        DomainException testException = new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, "Test message");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(testException, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals("JOURNEY_001", response.getErrorCode());
        assertEquals("Test message", response.getProblemDetail().getDetail());
        assertEquals(404, response.getProblemDetail().getStatus());
        assertEquals("/api/test", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void shouldMapNotFoundErrorsTo404() {
        // Given
        DomainException notFoundException = new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND,
                "Not found");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(notFoundException, mockRequest);

        // Then
        assertEquals(404, response.getProblemDetail().getStatus());
        assertEquals("JOURNEY_001", response.getErrorCode());
    }

    @Test
    void shouldMapValidationErrorTo400() {
        // Given
        DomainException validationException = new TestDomainException(ErrorCode.VALIDATION_FAILED, "Validation failed");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(validationException, mockRequest);

        // Then
        assertEquals(400, response.getProblemDetail().getStatus());
        assertEquals("VALIDATION_001", response.getErrorCode());
    }

    @Test
    void shouldMapConflictErrorsTo409() {
        // Given
        DomainException conflictException = new TestDomainException(ErrorCode.CONCURRENT_MODIFICATION, "Conflict");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(conflictException, mockRequest);

        // Then
        assertEquals(409, response.getProblemDetail().getStatus());
        assertEquals("CONFLICT_001", response.getErrorCode());
    }

    @Test
    void shouldIncludeErrorContext() {
        // Given
        DomainException contextException = new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, "Not found")
                .withContext("journeyDefinitionId", "test-123")
                .withContext("userId", "user-456");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(contextException, mockRequest);

        // Then
        Map<String, Object> context = response.getAdditionalContext();
        assertEquals(2, context.size());
        assertEquals("test-123", context.get("journeyDefinitionId"));
        assertEquals("user-456", context.get("userId"));
    }

    @Test
    void shouldCreateValidProblemDetailStructure() {
        // Given
        DomainException testException = new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, "Test message");

        // When
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(testException, mockRequest);

        // Then - Verify RFC 9457 compliance
        assertNotNull(response.getProblemDetail().getType());
        assertNotNull(response.getProblemDetail().getTitle());
        assertNotNull(response.getProblemDetail().getStatus());
        assertNotNull(response.getProblemDetail().getDetail());
        assertNotNull(response.getProblemDetail().getInstance());
        assertEquals("JOURNEY_001", response.getProblemDetail().getProperties().get("errorCode"));
    }

    // Test helper class for DomainException
    private static class TestDomainException extends DomainException {
        public TestDomainException(ErrorCode errorCode, String details) {
            super(errorCode, details);
        }
    }
}
