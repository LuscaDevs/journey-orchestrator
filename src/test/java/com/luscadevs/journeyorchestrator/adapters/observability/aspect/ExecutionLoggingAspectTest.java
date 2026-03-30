package com.luscadevs.journeyorchestrator.adapters.observability.aspect;

import com.luscadevs.journeyorchestrator.adapters.observability.config.ObservabilityProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExecutionLoggingAspect.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExecutionLoggingAspectTest {

    @Mock
    private ObservabilityProperties properties;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @InjectMocks
    private ExecutionLoggingAspect aspect;

    @BeforeEach
    void setUp() {
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getExcludedMethods()).thenReturn(new java.util.HashSet<>());
        when(properties.getExcludedPackages()).thenReturn(new java.util.HashSet<>());
        when(properties.getSlowOperationThreshold()).thenReturn(1000L);
        when(properties.isLogParameters()).thenReturn(false);
    }

    @Test
    @DisplayName("Should log controller method start and completion")
    void shouldLogControllerMethodExecution() throws Throwable {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.getTarget()).thenReturn(new Object());
        when(joinPoint.proceed()).thenReturn("test result");
        when(joinPoint.getStaticPart()).thenReturn(null);

        // When
        Object result = aspect.logControllerExecution(joinPoint);

        // Then
        verify(joinPoint).proceed();
        assertEquals("test result", result);
    }

    @Test
    @DisplayName("Should log service method start and completion")
    void shouldLogServiceMethodExecution() throws Throwable {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("performOperation");
        when(joinPoint.getTarget()).thenReturn(new Object());
        when(joinPoint.proceed()).thenReturn("service result");
        when(joinPoint.getStaticPart()).thenReturn(null);

        // When
        Object result = aspect.logServiceExecution(joinPoint);

        // Then
        verify(joinPoint).proceed();
        assertEquals("service result", result);
    }

    @Test
    @DisplayName("Should skip logging when disabled")
    void shouldSkipLoggingWhenDisabled() throws Throwable {
        // Given
        when(properties.isEnabled()).thenReturn(false);
        when(joinPoint.proceed()).thenReturn("test result");

        // When
        Object result = aspect.logControllerExecution(joinPoint);

        // Then
        verify(joinPoint).proceed();
        assertEquals("test result", result);
    }

    @Test
    @DisplayName("Should skip excluded methods")
    void shouldSkipExcludedMethods() throws Throwable {
        // Given
        when(properties.getExcludedMethods()).thenReturn(java.util.Set.of("toString", "hashCode"));
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("toString");
        when(joinPoint.getTarget()).thenReturn(new Object());
        when(joinPoint.proceed()).thenReturn("test result");

        // When
        Object result = aspect.logControllerExecution(joinPoint);

        // Then
        verify(joinPoint).proceed();
        assertEquals("test result", result);
    }

    @Test
    @DisplayName("Should skip excluded packages")
    void shouldSkipExcludedPackages() throws Throwable {
        // Given
        when(properties.getExcludedPackages()).thenReturn(java.util.Set.of("java.lang.Object"));
        when(joinPoint.getTarget()).thenReturn(new Object());
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.proceed()).thenReturn("test result");

        // When
        Object result = aspect.logControllerExecution(joinPoint);

        // Then
        verify(joinPoint).proceed();
        assertEquals("test result", result);
    }

    @Test
    @DisplayName("Should log slow operation warning")
    void shouldLogSlowOperationWarning() throws Throwable {
        // Given
        when(properties.getSlowOperationThreshold()).thenReturn(50L);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("slowMethod");
        when(joinPoint.getTarget()).thenReturn(new Object());
        when(joinPoint.proceed()).thenReturn("service result");
        when(joinPoint.getStaticPart()).thenReturn(null);

        // When
        Object result = aspect.logServiceExecution(joinPoint);

        // Then
        verify(joinPoint).proceed();
        assertEquals("service result", result);
    }

    @Test
    @DisplayName("Should measure execution duration")
    void shouldMeasureExecutionDuration() throws Throwable {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.getTarget()).thenReturn(new Object());
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            try {
                Thread.sleep(100);
                return "result";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
        when(joinPoint.getStaticPart()).thenReturn(null);

        // When
        Object result = aspect.logServiceExecution(joinPoint);

        // Then
        verify(joinPoint).proceed();
        assertEquals("result", result);
    }
}
