# Quickstart Guide: Execution Observability

**Feature**: 005-execution-observability  
**Date**: 2026-03-29  
**Version**: 1.0

## Overview

This quickstart guide provides step-by-step instructions for implementing and using the execution observability feature in the Journey Orchestrator system.

## Prerequisites

- Java 21
- Spring Boot 4.0.3
- Maven 3.8+
- Existing Journey Orchestrator project structure
- SLF4J and Logback configured

## Implementation Steps

### Step 1: Add Dependencies

Add the following dependencies to your `pom.xml`:

```xml
<dependencies>
    <!-- Spring AOP for method interception -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    
    <!-- JSON logging support -->
    <dependency>
        <groupId>ch.qos.logback.contrib</groupId>
        <artifactId>logback-json-classic</artifactId>
        <version>0.1.5</version>
    </dependency>
    
    <!-- Jackson for JSON processing -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
</dependencies>
```

### Step 2: Create Configuration Properties

Create `ObservabilityProperties.java`:

```java
package com.luscadevs.journeyorchestrator.observability.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@ConfigurationProperties("observability.logging")
public class ObservabilityProperties {
    
    private boolean enabled = true;
    private Set<String> excludedMethods = new HashSet<>();
    private Set<String> excludedPackages = new HashSet<>();
    private long slowOperationThreshold = 1000; // ms
    private boolean logParameters = false; // Always false for security
    
    // Getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public Set<String> getExcludedMethods() { return excludedMethods; }
    public void setExcludedMethods(Set<String> excludedMethods) { this.excludedMethods = excludedMethods; }
    
    public Set<String> getExcludedPackages() { return excludedPackages; }
    public void setExcludedPackages(Set<String> excludedPackages) { this.excludedPackages = excludedPackages; }
    
    public long getSlowOperationThreshold() { return slowOperationThreshold; }
    public void setSlowOperationThreshold(long slowOperationThreshold) { this.slowOperationThreshold = slowOperationThreshold; }
    
    public boolean isLogParameters() { return logParameters; }
    public void setLogParameters(boolean logParameters) { this.logParameters = logParameters; }
}
```

### Step 3: Create Logging Aspect

Create `ExecutionLoggingAspect.java`:

```java
package com.luscadevs.journeyorchestrator.observability.aspect;

import com.luscadevs.journeyorchestrator.observability.config.ObservabilityProperties;
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
    
    private Object logExecution(ProceedingJoinPoint joinPoint, String componentType) throws Throwable {
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
            logExecutionCompletion(componentIdentifier, componentType, correlationId, duration, EXECUTION_STATUS_SUCCESS, null);
            
            return result;
        } catch (Exception e) {
            long duration = (System.nanoTime() - startTime) / 1_000_000; // Convert to milliseconds
            
            // Log failure
            logExecutionCompletion(componentIdentifier, componentType, correlationId, duration, EXECUTION_STATUS_FAILURE, e);
            
            throw e;
        }
    }
    
    private void logExecutionStart(String componentIdentifier, String componentType, String correlationId) {
        Map<String, Object> logData = createLogData(
            Instant.now(),
            EXECUTION_PHASE_START,
            componentType,
            componentIdentifier,
            correlationId,
            null,
            null,
            null
        );
        
        logger.info("Execution started: {}", logData);
    }
    
    private void logExecutionCompletion(String componentIdentifier, String componentType, 
                                      String correlationId, long duration, String status, Exception exception) {
        Map<String, Object> logData = createLogData(
            Instant.now(),
            EXECUTION_PHASE_COMPLETION,
            componentType,
            componentIdentifier,
            correlationId,
            duration,
            status,
            exception
        );
        
        if (EXECUTION_STATUS_FAILURE.equals(status)) {
            logger.error("Execution failed: {}", logData);
        } else {
            logger.info("Execution completed: {}", logData);
        }
    }
    
    private Map<String, Object> createLogData(Instant timestamp, String executionPhase, 
                                            String componentType, String componentIdentifier,
                                            String correlationId, Long duration, String status, Exception exception) {
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
        MDC.getCopyOfContextMap().forEach(mdcContext::put);
        logData.put("mdcContext", mdcContext);
        
        // Add conditional fields
        if (duration != null) {
            logData.put("executionDuration", duration);
        }
        
        if (exception != null) {
            logData.put("errorCode", exception.getClass().getSimpleName());
        }
        
        // Add controller-specific fields
        if (COMPONENT_TYPE_CONTROLLER.equals(componentType)) {
            logData.put("httpMethod", MDC.get("httpMethod"));
            logData.put("requestPath", MDC.get("requestPath"));
        }
        
        return logData;
    }
    
    private String getComponentIdentifier(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName;
    }
}
```

### Step 4: Enhance MDC Error Handling

Create `MDCErrorEnhancer.java`:

```java
package com.luscadevs.journeyorchestrator.observability.enhancer;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MDCErrorEnhancer {
    
    public void enhanceMDCWithError(Exception exception) {
        if (exception != null) {
            MDC.put("errorCode", exception.getClass().getSimpleName());
            MDC.put("errorMessage", exception.getMessage());
        }
    }
    
    public void clearErrorMDC() {
        MDC.remove("errorCode");
        MDC.remove("errorMessage");
    }
}
```

### Step 5: Update Global Exception Handler

Enhance your existing `GlobalExceptionHandler.java`:

```java
package com.luscadevs.journeyorchestrator.adapters.in.web;

import com.luscadevs.journeyorchestrator.observability.enhancer.MDCErrorEnhancer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private final MDCErrorEnhancer mdcErrorEnhancer;
    
    public GlobalExceptionHandler(MDCErrorEnhancer mdcErrorEnhancer) {
        this.mdcErrorEnhancer = mdcErrorEnhancer;
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        // Enhance MDC with error information
        mdcErrorEnhancer.enhanceMDCWithError(e);
        
        try {
            // Your existing exception handling logic
            ErrorResponse errorResponse = new ErrorResponse("INTERNAL_ERROR", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        } finally {
            // Clean up MDC error context
            mdcErrorEnhancer.clearErrorMDC();
        }
    }
    
    // Your other exception handlers...
}
```

### Step 6: Configure Logback for JSON Logging

Update your `logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <property name="LOGS" value="./logs"/>
    
    <!-- Console Appender with JSON -->
    <appender name="ConsoleJson" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.contrib.json.classic.JsonEncoder">
            <jsonGeneratorDecorator class="ch.qos.logback.contrib.json.classic.JsonJsonGeneratorDecorator"/>
        </encoder>
    </appender>
    
    <!-- File Appender with JSON -->
    <appender name="FileJson" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOGS}/execution-observability.log</file>
        <encoder class="ch.qos.logback.contrib.json.classic.JsonEncoder">
            <jsonGeneratorDecorator class="ch.qos.logback.contrib.json.classic.JsonJsonGeneratorDecorator"/>
        </encoder>
        
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOGS}/execution-observability.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
    </appender>
    
    <!-- Root Logger -->
    <root level="INFO">
        <appender-ref ref="ConsoleJson"/>
        <appender-ref ref="FileJson"/>
    </root>
    
    <!-- Specific logger for execution observability -->
    <logger name="com.luscadevs.journeyorchestrator.observability" level="INFO"/>
</configuration>
```

### Step 7: Configure Application Properties

Add to your `application.yml`:

```yaml
observability:
  logging:
    enabled: true
    slow-operation-threshold: 1000  # ms
    log-parameters: false  # Security: never log parameters
    excluded-methods:
      - "toString"
      - "hashCode"
      - "equals"
    excluded-packages:
      - "java.lang"
      - "java.util"

logging:
  level:
    com.luscadevs.journeyorchestrator.observability: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

## Usage Examples

### Basic Controller

```java
@RestController
@RequestMapping("/api/journeys")
public class JourneyController {
    
    private final JourneyInstanceService journeyInstanceService;
    
    public JourneyController(JourneyInstanceService journeyInstanceService) {
        this.journeyInstanceService = journeyInstanceService;
    }
    
    @PostMapping
    public ResponseEntity<JourneyInstanceResponse> createJourney(@RequestBody StartJourneyRequest request) {
        // This method will be automatically logged
        JourneyInstanceResponse response = journeyInstanceService.createJourneyInstance(request);
        return ResponseEntity.ok(response);
    }
}
```

### Basic Service

```java
@Service
public class JourneyInstanceService {
    
    private final JourneyInstanceRepositoryPort repository;
    
    public JourneyInstanceService(JourneyInstanceRepositoryPort repository) {
        this.repository = repository;
    }
    
    public JourneyInstanceResponse createJourneyInstance(StartJourneyRequest request) {
        // This method will be automatically logged
        // Business logic here...
        return response;
    }
}
```

## Expected Log Output

### Controller Start Log
```json
{
  "timestamp": "2026-03-29T10:45:00.123Z",
  "executionPhase": "START",
  "componentType": "CONTROLLER",
  "componentIdentifier": "com.luscadevs.journeyorchestrator.adapters.in.web.JourneyController.createJourney",
  "correlationId": "req-12345-abcde",
  "httpMethod": "POST",
  "requestPath": "/api/journeys",
  "executionStatus": "SUCCESS",
  "threadName": "http-nio-8080-exec-1",
  "mdcContext": {
    "correlationId": "req-12345-abcde",
    "httpMethod": "POST",
    "requestPath": "/api/journeys",
    "threadName": "http-nio-8080-exec-1"
  }
}
```

### Service Completion Log
```json
{
  "timestamp": "2026-03-29T10:45:00.168Z",
  "executionPhase": "COMPLETION",
  "componentType": "SERVICE",
  "componentIdentifier": "com.luscadevs.journeyorchestrator.application.service.JourneyInstanceService.createJourneyInstance",
  "correlationId": "req-12345-abcde",
  "executionDuration": 45,
  "executionStatus": "SUCCESS",
  "threadName": "http-nio-8080-exec-1",
  "mdcContext": {
    "correlationId": "req-12345-abcde",
    "httpMethod": "POST",
    "requestPath": "/api/journeys",
    "threadName": "http-nio-8080-exec-1"
  }
}
```

## Testing

### Unit Test Example

```java
@ExtendWith(MockitoExtension.class)
class ExecutionLoggingAspectTest {
    
    @Mock
    private ObservabilityProperties properties;
    
    @InjectMocks
    private ExecutionLoggingAspect aspect;
    
    @Test
    void shouldLogControllerExecution() throws Throwable {
        // Given
        when(properties.isEnabled()).thenReturn(true);
        
        // When
        // Test your aspect logic
    }
}
```

### Integration Test Example

```java
@SpringBootTest
@AutoConfigureTestDatabase
class ExecutionObservabilityIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldGenerateExecutionLogs() {
        // Given
        // When
        ResponseEntity<String> response = restTemplate.postForEntity("/api/journeys", request, String.class);
        
        // Then
        // Verify logs are generated
        // Verify log format and content
    }
}
```

## Monitoring and Troubleshooting

### Log Analysis Commands

```bash
# View all execution logs for a specific correlation ID
grep "req-12345-abcde" logs/execution-observability.log

# Find slow operations (>1000ms)
jq 'select(.executionDuration > 1000)' logs/execution-observability.log

# Count errors by component
jq -r '.componentType + " " + .executionStatus' logs/execution-observability.log | sort | uniq -c
```

### Common Issues

1. **Missing correlationId**: Check that CorrelationIdFilter is properly configured
2. **No logs generated**: Verify that observability.logging.enabled=true
3. **Performance impact**: Monitor logging overhead and adjust excluded methods/packages
4. **Log format issues**: Verify JSON encoder configuration in logback-spring.xml

## Configuration Options

### Performance Tuning

```yaml
observability:
  logging:
    enabled: true
    slow-operation-threshold: 500  # Lower threshold for more sensitive monitoring
    excluded-methods:
      - "toString"
      - "hashCode"
      - "equals"
      - "get*"  # Exclude getters
    excluded-packages:
      - "java.lang"
      - "java.util"
      - "org.springframework"
```

### Environment-Specific Settings

```yaml
# Development
---
spring:
  profiles: dev
observability:
  logging:
    enabled: true
    slow-operation-threshold: 100

# Production
---
spring:
  profiles: prod
observability:
  logging:
    enabled: true
    slow-operation-threshold: 1000
```

## Next Steps

1. **Performance Testing**: Measure logging overhead under load
2. **Log Aggregation**: Set up log aggregation tools (ELK Stack, Splunk, etc.)
3. **Alerting**: Configure alerts for error rates and slow operations
4. **Dashboard**: Create monitoring dashboards for execution metrics
5. **Retention**: Implement log retention policies based on compliance requirements

## Support

For issues or questions about the execution observability feature:
- Check the logs for error messages
- Verify configuration settings
- Review the troubleshooting section above
- Consult the full specification document for detailed requirements
