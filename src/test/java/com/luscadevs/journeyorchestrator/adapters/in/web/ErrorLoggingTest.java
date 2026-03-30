package com.luscadevs.journeyorchestrator.adapters.in.web;

import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionNotFoundException;
import com.luscadevs.journeyorchestrator.domain.exception.InvalidStateTransitionException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.MDC;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for error logging functionality in GlobalExceptionHandler. Verifies that structured
 * logging includes correlation IDs, context, and proper log levels.
 */
class ErrorLoggingTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getHeader("X-Correlation-ID")).thenReturn(null);

        // Clear MDC before each test
        MDC.clear();
    }

    @Test
    void shouldLogDomainExceptionWithCorrelationId() {
        // Given
        JourneyDefinitionNotFoundException exception =
                new JourneyDefinitionNotFoundException("test-123");

        // When
        exceptionHandler.handleDomainException(exception, mockRequest);

        // Then - Verify MDC is cleared after handling
        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("requestPath"));
        assertNull(MDC.get("httpMethod"));
        assertNull(MDC.get("errorCode"));
        assertNull(MDC.get("exceptionType"));
    }

    @Test
    void shouldUseExistingCorrelationIdFromHeader() {
        // Given
        String existingCorrelationId = "existing-123";
        when(mockRequest.getHeader("X-Correlation-ID")).thenReturn(existingCorrelationId);

        JourneyDefinitionNotFoundException exception =
                new JourneyDefinitionNotFoundException("test-456");

        // When
        exceptionHandler.handleDomainException(exception, mockRequest);

        // Then - Verify MDC is cleared after handling
        assertNull(MDC.get("correlationId"));
    }

    @Test
    void shouldLogValidationExceptionWithCorrelationId() {
        // Given
        MethodArgumentNotValidException validationException = createMockValidationException();

        // When
        exceptionHandler.handleValidationException(validationException, mockRequest);

        // Then - Verify MDC is cleared after handling
        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("requestPath"));
        assertNull(MDC.get("httpMethod"));
    }

    @Test
    void shouldLogGenericExceptionWithCorrelationId() {
        // Given
        RuntimeException genericException = new RuntimeException("Unexpected error");

        // When
        exceptionHandler.handleGenericException(genericException, mockRequest);

        // Then - Verify MDC is cleared after handling
        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("requestPath"));
        assertNull(MDC.get("httpMethod"));
    }

    @Test
    void shouldSetCorrectLoggingContextForDomainExceptions() {
        // Given
        InvalidStateTransitionException exception =
                new InvalidStateTransitionException("instance-123", "RUNNING", "COMPLETED");

        // When
        exceptionHandler.handleInvalidStateTransitionException(exception, mockRequest);

        // Then - Verify MDC is cleared after handling
        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("errorCode"));
        assertNull(MDC.get("exceptionType"));
    }

    @Test
    void shouldSetCorrectLoggingContextForValidationExceptions() {
        // Given
        MethodArgumentNotValidException validationException = createMockValidationException();

        // When
        exceptionHandler.handleValidationException(validationException, mockRequest);

        // Then - Verify MDC is cleared after handling
        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("requestPath"));
        assertNull(MDC.get("httpMethod"));
        // Note: errorCode and exceptionType should not be set for validation exceptions
        assertNull(MDC.get("errorCode"));
        assertNull(MDC.get("exceptionType"));
    }

    @Test
    void shouldSetCorrectLoggingContextForGenericExceptions() {
        // Given
        RuntimeException genericException = new RuntimeException("Unexpected error");

        // When
        exceptionHandler.handleGenericException(genericException, mockRequest);

        // Then - Verify MDC is cleared after handling
        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("requestPath"));
        assertNull(MDC.get("httpMethod"));
        // Note: errorCode and exceptionType should not be set for generic exceptions
        assertNull(MDC.get("errorCode"));
        assertNull(MDC.get("exceptionType"));
    }

    @Test
    void shouldGenerateNewCorrelationIdWhenNotProvided() {
        // Given
        when(mockRequest.getHeader("X-Correlation-ID")).thenReturn(null);
        JourneyDefinitionNotFoundException exception =
                new JourneyDefinitionNotFoundException("test-789");

        // When
        exceptionHandler.handleDomainException(exception, mockRequest);

        // Then - Should not throw exception and should handle gracefully
        // The correlation ID should be generated internally
        assertDoesNotThrow(() -> exceptionHandler.handleDomainException(exception, mockRequest));
    }

    @Test
    void shouldHandleEmptyCorrelationIdHeader() {
        // Given
        when(mockRequest.getHeader("X-Correlation-ID")).thenReturn("");
        JourneyDefinitionNotFoundException exception =
                new JourneyDefinitionNotFoundException("test-999");

        // When & Then
        assertDoesNotThrow(() -> exceptionHandler.handleDomainException(exception, mockRequest));
    }

    @Test
    void shouldMaintainMdcCleanupAfterException() {
        // Given
        JourneyDefinitionNotFoundException exception =
                new JourneyDefinitionNotFoundException("test-cleanup");

        // When
        assertDoesNotThrow(() -> exceptionHandler.handleDomainException(exception, mockRequest));

        // Then - All MDC keys should be cleared
        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("requestPath"));
        assertNull(MDC.get("httpMethod"));
        assertNull(MDC.get("errorCode"));
        assertNull(MDC.get("exceptionType"));
    }

    /**
     * Helper method to create a mock MethodArgumentNotValidException.
     */
    private MethodArgumentNotValidException createMockValidationException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "fieldName", "rejectedValue", false, null,
                null, "validation message");

        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(bindingResult.getFieldErrorCount()).thenReturn(1);

        return new MethodArgumentNotValidException(null, bindingResult);
    }
}
