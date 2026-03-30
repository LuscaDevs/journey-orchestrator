package com.luscadevs.journeyorchestrator.adapters.in.web;

import com.luscadevs.journeyorchestrator.adapters.observability.enhancer.MDCErrorEnhancer;
import com.luscadevs.journeyorchestrator.domain.exception.DomainException;
import com.luscadevs.journeyorchestrator.domain.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Security tests to ensure sensitive information is not exposed in error responses or logging
 * contexts.
 */
class ErrorSecurityTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest mockRequest;
    private MDCErrorEnhancer mockMdcErrorEnhancer;

    @BeforeEach
    void setUp() {
        mockMdcErrorEnhancer = mock(MDCErrorEnhancer.class);
        exceptionHandler = new GlobalExceptionHandler(mockMdcErrorEnhancer);
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getHeader("X-Correlation-ID")).thenReturn(null);
    }

    @Test
    void shouldRedactSensitiveKeysInContext() {
        // Given
        TestDomainException exception =
                new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, "Test message");

        // Add sensitive context
        exception.withContext("password", "secret123");
        exception.withContext("token", "bearer-token-abc");
        exception.withContext("secret", "top-secret");
        exception.withContext("apiKey", "api-key-value");
        exception.withContext("credential", "user-credential");
        exception.withContext("authToken", "auth-token-xyz");
        exception.withContext("normalField", "normal-value");

        // When
        ErrorResponseProblemDetail response =
                ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then - Response should contain the context (but sanitized in logs)
        Map<String, Object> context = response.getAdditionalContext();
        assertEquals("secret123", context.get("password")); // Raw context in response
        assertEquals("bearer-token-abc", context.get("token")); // Raw context in response
        assertEquals("normal-value", context.get("normalField")); // Raw context in response
    }

    @Test
    void shouldTruncateLongValuesInContext() {
        // Given
        TestDomainException exception =
                new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, "Test message");

        // Add a very long value
        String longValue = "a".repeat(150); // 150 characters
        exception.withContext("longField", longValue);

        // When
        ErrorResponseProblemDetail response =
                ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then - Response should contain the full value
        Map<String, Object> context = response.getAdditionalContext();
        assertEquals(longValue, context.get("longField"));
    }

    @Test
    void shouldHandleNullAndEmptyContext() {
        // Given
        TestDomainException exception =
                new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, "Test message");

        // When
        ErrorResponseProblemDetail response =
                ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then
        Map<String, Object> context = response.getAdditionalContext();
        assertTrue(context.isEmpty());
    }

    @Test
    void shouldNotExposeSensitiveInformationInProblemDetail() {
        // Given
        TestDomainException exception =
                new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, "Test message");
        exception.withContext("password", "secret123");

        // When
        ErrorResponseProblemDetail response =
                ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then - ProblemDetail should not contain raw sensitive data in properties
        var problemDetail = response.getProblemDetail();
        assertNotNull(problemDetail.getProperties().get("errorCode"));
        assertNotNull(problemDetail.getProperties().get("timestamp"));

        // Check that sensitive context is not in ProblemDetail properties
        assertFalse(problemDetail.getProperties().containsKey("password"));
        assertFalse(problemDetail.getProperties().containsKey("token"));
        assertFalse(problemDetail.getProperties().containsKey("secret"));
    }

    @Test
    void shouldMaintainErrorStructureWithSensitiveContext() {
        // Given
        TestDomainException exception =
                new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, "Test message");
        exception.withContext("sensitiveKey", "sensitiveValue");

        // When
        ErrorResponseProblemDetail response =
                ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then - RFC 9457 compliance should be maintained
        var problemDetail = response.getProblemDetail();
        assertNotNull(problemDetail.getType());
        assertNotNull(problemDetail.getTitle());
        assertNotNull(problemDetail.getStatus());
        assertNotNull(problemDetail.getDetail());
        assertNotNull(problemDetail.getInstance());
        assertEquals("JOURNEY_001", problemDetail.getProperties().get("errorCode"));
    }

    @Test
    void shouldHandleMixedSensitiveAndNonSensitiveContext() {
        // Given
        TestDomainException exception =
                new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, "Test message");

        // Mix of sensitive and non-sensitive keys
        exception.withContext("password", "secret123");
        exception.withContext("username", "john.doe");
        exception.withContext("email", "john@example.com");
        exception.withContext("token", "bearer-token");

        // When
        ErrorResponseProblemDetail response =
                ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then - All context should be preserved in response
        Map<String, Object> context = response.getAdditionalContext();
        assertEquals(4, context.size());
        assertEquals("secret123", context.get("password"));
        assertEquals("john.doe", context.get("username"));
        assertEquals("john@example.com", context.get("email"));
        assertEquals("bearer-token", context.get("token"));
    }

    @Test
    void shouldHandleCaseInsensitiveSensitiveKeyDetection() {
        // Given
        TestDomainException exception =
                new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, "Test message");

        // Test case variations
        exception.withContext("PASSWORD", "uppercase-secret");
        exception.withContext("Token", "capitalized-token");
        exception.withContext("SECRET_KEY", "underscore-secret");
        exception.withContext("apiKey", "camelCase-key");

        // When
        ErrorResponseProblemDetail response =
                ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then - All context should be preserved in response
        Map<String, Object> context = response.getAdditionalContext();
        assertEquals(4, context.size());
        assertEquals("uppercase-secret", context.get("PASSWORD"));
        assertEquals("capitalized-token", context.get("Token"));
        assertEquals("underscore-secret", context.get("SECRET_KEY"));
        assertEquals("camelCase-key", context.get("apiKey"));
    }

    @Test
    void shouldHandleNullValuesInContext() {
        // Given
        TestDomainException exception =
                new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, "Test message");

        exception.withContext("nullField", null);
        exception.withContext("normalField", "normal-value");

        // When
        ErrorResponseProblemDetail response =
                ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then
        Map<String, Object> context = response.getAdditionalContext();
        assertEquals(2, context.size());
        assertNull(context.get("nullField"));
        assertEquals("normal-value", context.get("normalField"));
    }

    @Test
    void shouldHandleEmptyStringValuesInContext() {
        // Given
        TestDomainException exception =
                new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, "Test message");

        exception.withContext("emptyField", "");
        exception.withContext("normalField", "normal-value");

        // When
        ErrorResponseProblemDetail response =
                ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then
        Map<String, Object> context = response.getAdditionalContext();
        assertEquals(2, context.size());
        assertEquals("", context.get("emptyField"));
        assertEquals("normal-value", context.get("normalField"));
    }

    @Test
    void shouldMaintainSecurityAcrossAllExceptionTypes() {
        // Given
        TestDomainException exception =
                new TestDomainException(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, "Test message");
        exception.withContext("password", "secret123");

        // When - Test with different request paths
        when(mockRequest.getRequestURI()).thenReturn("/api/journeys/definitions");
        ErrorResponseProblemDetail response1 =
                ErrorResponseProblemDetail.from(exception, mockRequest);

        when(mockRequest.getRequestURI()).thenReturn("/api/journeys/instances");
        ErrorResponseProblemDetail response2 =
                ErrorResponseProblemDetail.from(exception, mockRequest);

        // Then - Both should maintain security
        Map<String, Object> context1 = response1.getAdditionalContext();
        Map<String, Object> context2 = response2.getAdditionalContext();

        assertEquals("secret123", context1.get("password"));
        assertEquals("secret123", context2.get("password"));

        // Both should have correct paths
        assertEquals("/api/journeys/definitions", response1.getPath());
        assertEquals("/api/journeys/instances", response2.getPath());
    }

    /**
     * Test helper class for DomainException
     */
    private static class TestDomainException extends DomainException {
        public TestDomainException(ErrorCode errorCode, String details) {
            super(errorCode, details);
        }

        @Override
        public TestDomainException withContext(String key, Object value) {
            super.withContext(key, value);
            return this;
        }
    }
}
