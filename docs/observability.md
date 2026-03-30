# Execution Observability Documentation

## Overview

The Execution Observability feature provides automatic logging of method execution lifecycle for controllers and services in the Journey Orchestrator application. This feature enables comprehensive monitoring and debugging capabilities without requiring manual logging code in business logic.

## Features

### Automatic Method Logging
- **Controller Logging**: All REST controller methods are automatically logged
- **Service Logging**: All service methods are automatically logged  
- **Lifecycle Tracking**: Method start and completion events are captured
- **Duration Measurement**: Execution time is measured and logged

### Structured Logging
- **JSON Format**: All logs are output in structured JSON format
- **MDC Context**: Correlation IDs and context are propagated across components
- **Performance Monitoring**: Slow operations are automatically detected and warned

### Error Enhancement
- **MDC Error Context**: Error information is automatically added to logging context
- **Global Exception Handling**: Enhanced error logging with proper context

## Configuration

The observability feature can be configured through `application.yml`:

```yaml
observability:
  logging:
    enabled: true
    excluded-methods:
      - toString
      - hashCode  
      - equals
    excluded-packages:
      - java.lang.Object
      - java.lang.String
    slow-operation-threshold: 1000  # milliseconds
    log-parameters: false  # Security: never log parameters
```

## Log Format

### Execution Log Structure
```json
{
  "timestamp": "2026-03-30T01:59:38.201694800Z",
  "level": "INFO",
  "thread": "http-nio-8080-exec-2",
  "logger": "com.luscadevs.journeyorchestrator.adapters.observability.aspect.ExecutionLoggingAspect",
  "message": "Execution started: {executionPhase=START, componentType=CONTROLLER, executionStatus=SUCCESS, mdcContext={correlationId=e7f5c47b-2a50-4c7b-9202-6b9d50953961}, correlationId=e7f5c47b-2a50-4c7b-9202-6b9d50953961, httpMethod=null, componentIdentifier=com.luscadevs.journeyorchestrator.api.controller.JourneyDefinitionController.getJourneyDefinitionsByCode, threadName=http-nio-8080-exec-2, requestPath=null, timestamp=2026-03-30T01:59:38.201694800Z}",
  "context": "correlationId=e7f5c47b-2a50-4c7b-9202-6b9d50953961"
}
```

### Performance Warnings
```json
{
  "timestamp": "2026-03-30T01:59:38.279149700Z",
  "level": "WARN", 
  "thread": "http-nio-8080-exec-2",
  "logger": "com.luscadevs.journeyorchestrator.adapters.observability.aspect.ExecutionLoggingAspect",
  "message": "Slow operation detected: 1500ms | Component: com.luscadevs.journeyorchestrator.application.service.JourneyDefinitionService.getJourneyDefinitionsByCode | CorrelationId: e7f5c47b-2a50-4c7b-9202-6b9d50953961",
  "context": "correlationId=e7f5c47b-2a50-4c7b-9202-6b9d50953961"
}
```

## Security Considerations

### Parameter Logging
- **Disabled by Default**: Method parameters are never logged for security
- **Configurable**: Can be enabled via `observability.logging.log-parameters` (not recommended)
- **Sensitive Data**: Automatic filtering prevents sensitive information exposure

### Exclusion Rules
Methods and packages can be excluded from logging:
- Methods: `toString`, `hashCode`, `equals`
- Packages: `java.lang.Object`, `java.lang.String`

## Implementation Details

### Core Components

1. **ExecutionLoggingAspect**: Main AOP aspect that intercepts method calls
2. **MDCErrorEnhancer**: Enhances MDC context with error information
3. **ObservabilityProperties**: Configuration management
4. **Enum Models**: Type-safe execution phase, component, and status definitions

### Integration Points

1. **GlobalExceptionHandler**: Enhanced with MDC error context
2. **Spring Boot Autoconfiguration**: Automatic aspect registration
3. **Logback Configuration**: Structured JSON logging setup

## Monitoring and Debugging

### Correlation IDs
Each request gets a unique correlation ID that is:
- Generated automatically when not present
- Propagated through all components in the request chain
- Included in all log entries for traceability

### Performance Metrics
- **Execution Duration**: Measured in milliseconds
- **Slow Operation Warning**: Configurable threshold (default 1000ms)
- **Component Identification**: Clear identification of controller vs service calls

## Usage Examples

### Controller Method
```java
@RestController
public class MyController {
    
    public ResponseEntity<String> myMethod() {
        // Automatically logged: START, COMPLETION
        // Duration measured: ~50ms
        // Status tracked: SUCCESS
        return ResponseEntity.ok("result");
    }
}
```

### Service Method
```java
@Service  
public class MyService {
    
    public String processData(String input) {
        // Automatically logged: START, COMPLETION
        // Duration measured: ~100ms
        // Status tracked: SUCCESS
        return "processed: " + input;
    }
}
```

## Testing

### Integration Tests
The feature includes comprehensive integration tests:
- Controller execution logging verification
- Service execution logging verification
- Error handling and logging verification
- Configuration loading verification

### Unit Tests
Aspect-oriented unit tests cover:
- Logging enable/disable functionality
- Method/package exclusion rules
- Performance threshold monitoring
- Error context enhancement

## Troubleshooting

### Common Issues
1. **Logs Not Appearing**: Check `observability.logging.enabled=true`
2. **Missing Correlation IDs**: Verify MDC context setup
3. **Performance Warnings**: Adjust `slow-operation-threshold` as needed
4. **JSON Parsing Issues**: Verify logback configuration

### Debug Mode
Enable debug logging for the observability package:
```yaml
logging:
  level:
    com.luscadevs.journeyorchestrator.adapters.observability: DEBUG
```

## Future Enhancements

Potential improvements for future iterations:
1. **Custom Metrics**: Integration with Micrometer for metrics collection
2. **Log Aggregation**: Integration with log aggregation systems
3. **Dynamic Configuration**: Runtime configuration changes
4. **Tracing Integration**: Distributed tracing with OpenTelemetry

---

*This documentation covers the Execution Observability feature implementation as of version 1.0.0.*
