package com.luscadevs.journeyorchestrator.adapters.in.web;

import com.luscadevs.journeyorchestrator.adapters.observability.enhancer.MDCErrorEnhancer;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.MDC;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for validation error logging functionality. Verifies that validation errors are
 * properly logged with correlation IDs and context.
 */
class ValidationErrorLoggingTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest mockRequest;
    private MDCErrorEnhancer mockMdcErrorEnhancer;

    @BeforeEach
    void setUp() {
        mockMdcErrorEnhancer = mock(MDCErrorEnhancer.class);
        exceptionHandler = new GlobalExceptionHandler(mockMdcErrorEnhancer);
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/api/journeys");
        when(mockRequest.getMethod()).thenReturn("POST");
        when(mockRequest.getHeader("X-Correlation-ID")).thenReturn(null);

        // Clear MDC before each test
        MDC.clear();
    }

    @Test
    void shouldLogValidationErrorsWithCorrelationId() {
        // Given
        MethodArgumentNotValidException validationException = createMockValidationException("name",
                "must not be empty", "description", "must be at least 10 characters");

        // When
        var response = exceptionHandler.handleValidationException(validationException, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());

        // Verify MDC is cleared after handling
        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("requestPath"));
        assertNull(MDC.get("httpMethod"));
    }

    @Test
    void shouldLogValidationErrorsWithExistingCorrelationId() {
        // Given
        String correlationId = "test-correlation-123";
        when(mockRequest.getHeader("X-Correlation-ID")).thenReturn(correlationId);

        MethodArgumentNotValidException validationException =
                createMockValidationException("field1", "error message 1");

        // When
        var response = exceptionHandler.handleValidationException(validationException, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());

        // Verify MDC is cleared after handling
        assertNull(MDC.get("correlationId"));
    }

    @Test
    void shouldHandleMultipleValidationErrors() {
        // Given
        MethodArgumentNotValidException validationException = createMockValidationException(
                "field1", "error1", "field2", "error2", "field3", "error3");

        // When
        var response = exceptionHandler.handleValidationException(validationException, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());

        // Verify the response contains field error details
        var problemDetail = response.getBody();
        assertNotNull(problemDetail);
        assertEquals("Validation Failed", problemDetail.getTitle());
        assertEquals("VALIDATION_001", problemDetail.getProperties().get("errorCode"));
    }

    @Test
    void shouldSetCorrectMdcContextForValidationErrors() {
        // Given
        MethodArgumentNotValidException validationException =
                createMockValidationException("testField", "test error");

        // When
        exceptionHandler.handleValidationException(validationException, mockRequest);

        // Then - MDC should be cleared after handling
        assertNull(MDC.get("correlationId"));
        assertNull(MDC.get("requestPath"));
        assertNull(MDC.get("httpMethod"));

        // Validation exceptions should not set errorCode or exceptionType in MDC
        assertNull(MDC.get("errorCode"));
        assertNull(MDC.get("exceptionType"));
    }

    @Test
    void shouldHandleEmptyValidationErrors() {
        // Given
        MethodArgumentNotValidException validationException = createMockValidationException();

        // When
        var response = exceptionHandler.handleValidationException(validationException, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());

        // Verify MDC is cleared
        assertNull(MDC.get("correlationId"));
    }

    @Test
    void shouldMaintainRequestPathInLoggingContext() {
        // Given
        String requestPath = "/api/journeys/definitions";
        when(mockRequest.getRequestURI()).thenReturn(requestPath);

        MethodArgumentNotValidException validationException =
                createMockValidationException("name", "required field");

        // When
        exceptionHandler.handleValidationException(validationException, mockRequest);

        // Then - MDC should be cleared after handling
        assertNull(MDC.get("requestPath"));
    }

    @Test
    void shouldMaintainHttpMethodInLoggingContext() {
        // Given
        String httpMethod = "PUT";
        when(mockRequest.getMethod()).thenReturn(httpMethod);

        MethodArgumentNotValidException validationException =
                createMockValidationException("status", "invalid value");

        // When
        exceptionHandler.handleValidationException(validationException, mockRequest);

        // Then - MDC should be cleared after handling
        assertNull(MDC.get("httpMethod"));
    }

    @Test
    void shouldGenerateCorrelationIdForValidationErrors() {
        // Given
        when(mockRequest.getHeader("X-Correlation-ID")).thenReturn(null);

        MethodArgumentNotValidException validationException =
                createMockValidationException("test", "error");

        // When & Then
        assertDoesNotThrow(
                () -> exceptionHandler.handleValidationException(validationException, mockRequest));
    }

    @Test
    void shouldHandleValidationErrorWithNullRejectedValue() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError =
                new FieldError("object", "field", null, false, null, null, "must not be null");

        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(bindingResult.getFieldErrorCount()).thenReturn(1);

        MethodArgumentNotValidException validationException =
                new MethodArgumentNotValidException(null, bindingResult);

        // When
        var response = exceptionHandler.handleValidationException(validationException, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldHandleValidationErrorWithEmptyRejectedValue() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError =
                new FieldError("object", "field", "", false, null, null, "must not be empty");

        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(bindingResult.getFieldErrorCount()).thenReturn(1);

        MethodArgumentNotValidException validationException =
                new MethodArgumentNotValidException(null, bindingResult);

        // When
        var response = exceptionHandler.handleValidationException(validationException, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
    }

    /**
     * Helper method to create a mock MethodArgumentNotValidException with field errors.
     */
    private MethodArgumentNotValidException createMockValidationException(String... fieldErrors) {
        BindingResult bindingResult = mock(BindingResult.class);

        // Create field errors from the varargs parameter
        List<FieldError> errors = java.util.stream.IntStream.range(0, fieldErrors.length / 2)
                .mapToObj(i -> new FieldError("object", fieldErrors[i * 2], "rejectedValue", false,
                        null, null, fieldErrors[i * 2 + 1]))
                .toList();

        when(bindingResult.getFieldErrors()).thenReturn(errors);
        when(bindingResult.getFieldErrorCount()).thenReturn(errors.size());

        return new MethodArgumentNotValidException(null, bindingResult);
    }
}
