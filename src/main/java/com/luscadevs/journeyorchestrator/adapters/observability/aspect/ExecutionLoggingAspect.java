package com.luscadevs.journeyorchestrator.adapters.observability.aspect;

import com.luscadevs.journeyorchestrator.adapters.observability.config.ObservabilityProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect for automatic execution logging of controllers and application services. Provides
 * comprehensive observability without contaminating business logic.
 */
@Aspect
@Component
public class ExecutionLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionLoggingAspect.class);
    private static final String COMPONENT_TYPE_CONTROLLER = "CONTROLLER";
    private static final String COMPONENT_TYPE_SERVICE = "SERVICE";
    private static final String EXECUTION_PHASE_START = "START";
    private static final String EXECUTION_PHASE_COMPLETION = "COMPLETION";
    private static final String EXECUTION_STATUS_SUCCESS = "SUCCESS";
    private static final String EXECUTION_STATUS_FAILURE = "FAILURE";

    private final ObservabilityProperties properties;

    public ExecutionLoggingAspect(ObservabilityProperties properties) {
        this.properties = properties;
    }

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, COMPONENT_TYPE_CONTROLLER);
    }

    @Around("@within(org.springframework.stereotype.Service)")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, COMPONENT_TYPE_SERVICE);
    }

    /**
     * Core logging logic for both controllers and services.
     */
    private Object logExecution(ProceedingJoinPoint joinPoint, String componentType)
            throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        String componentIdentifier = getComponentIdentifier(joinPoint);
        String correlationId = MDC.get("correlationId");

        // Log start
        logExecutionStart(componentIdentifier, componentType, correlationId);

        long startTime = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long duration = (System.nanoTime() - startTime) / 1_000_000; // Convert to milliseconds

            // Log successful completion
            logExecutionCompletion(componentIdentifier, componentType, correlationId, duration,
                    EXECUTION_STATUS_SUCCESS, null);

            return result;
        } catch (Exception e) {
            long duration = (System.nanoTime() - startTime) / 1_000_000; // Convert to milliseconds

            // Log failure
            logExecutionCompletion(componentIdentifier, componentType, correlationId, duration,
                    EXECUTION_STATUS_FAILURE, e);

            throw e;
        }
    }

    /**
     * Logs the start of method execution.
     */
    private void logExecutionStart(String componentIdentifier, String componentType,
            String correlationId) {
        Map<String, Object> logData = createLogData(Instant.now(), EXECUTION_PHASE_START,
                componentType, componentIdentifier, correlationId, null, null);

        logger.info("Execution started: {}", logData);
    }

    /**
     * Logs the completion of method execution.
     */
    private void logExecutionCompletion(String componentIdentifier, String componentType,
            String correlationId, Long duration, String status, Exception exception) {
        Map<String, Object> logData = createLogData(Instant.now(), EXECUTION_PHASE_COMPLETION,
                componentType, componentIdentifier, correlationId, duration, status);

        // Add slow operation warning if threshold exceeded
        if (duration != null && duration > properties.getSlowOperationThreshold()) {
            logger.warn("Slow operation detected: {}ms | Component: {} | CorrelationId: {}",
                    duration, componentIdentifier, correlationId);
        }

        if (EXECUTION_STATUS_FAILURE.equals(status)) {
            logger.error("Execution failed: {}", logData);
        } else {
            logger.info("Execution completed: {}", logData);
        }
    }

    /**
     * Creates log data map with all required fields.
     */
    private Map<String, Object> createLogData(Instant timestamp, String executionPhase,
            String componentType, String componentIdentifier, String correlationId, Long duration,
            String status) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("timestamp", timestamp.toString());
        logData.put("executionPhase", executionPhase);
        logData.put("componentType", componentType);
        logData.put("componentIdentifier", componentIdentifier);
        logData.put("correlationId", correlationId);
        logData.put("executionStatus", status != null ? status : "SUCCESS");
        logData.put("threadName", Thread.currentThread().getName());

        // Add MDC context
        Map<String, String> mdcContext = new HashMap<>();
        if (MDC.getCopyOfContextMap() != null) {
            MDC.getCopyOfContextMap().forEach(mdcContext::put);
        }
        logData.put("mdcContext", mdcContext);

        // Add conditional fields
        if (duration != null) {
            logData.put("executionDuration", duration);
        }

        // Add controller-specific fields
        if (COMPONENT_TYPE_CONTROLLER.equals(componentType)) {
            logData.put("httpMethod", MDC.get("httpMethod"));
            logData.put("requestPath", MDC.get("requestPath"));
        }

        return logData;
    }

    /**
     * Extracts component identifier from join point.
     */
    private String getComponentIdentifier(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName;
    }
}
