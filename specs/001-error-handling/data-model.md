# Data Model: Standardized Error Handling Mechanism

**Date**: 2025-03-25  
**Feature**: Standardized Error Handling Mechanism

## Domain Exception Hierarchy

### Base Exception: DomainException

```java
@Getter
@RequiredArgsConstructor
public abstract class DomainException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String details;
    
    // Business context can be added by subclasses
    protected Map<String, Object> context = new HashMap<>();
    
    public DomainException withContext(String key, Object value) {
        context.put(key, value);
        return this;
    }
    
    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(context);
    }
}
```

### Error Code Enumeration

```java
public enum ErrorCode {
    // Journey Definition Errors
    JOURNEY_DEFINITION_NOT_FOUND("JOURNEY_001", "Journey definition not found"),
    JOURNEY_DEFINITION_ALREADY_EXISTS("JOURNEY_002", "Journey definition already exists"),
    JOURNEY_DEFINITION_INVALID("JOURNEY_003", "Journey definition is invalid"),
    
    // Journey Instance Errors
    JOURNEY_INSTANCE_NOT_FOUND("JOURNEY_101", "Journey instance not found"),
    JOURNEY_ALREADY_COMPLETED("JOURNEY_102", "Journey has already completed"),
    JOURNEY_ALREADY_STARTED("JOURNEY_103", "Journey has already started"),
    
    // State Transition Errors
    INVALID_STATE_TRANSITION("STATE_001", "Invalid state transition"),
    STATE_TRANSITION_NOT_ALLOWED("STATE_002", "State transition not allowed in current state"),
    
    // Validation Errors
    VALIDATION_FAILED("VALIDATION_001", "Validation failed"),
    INVALID_REQUEST_FORMAT("VALIDATION_002", "Invalid request format"),
    MISSING_REQUIRED_FIELD("VALIDATION_003", "Missing required field"),
    
    // Conflict Errors
    CONCURRENT_MODIFICATION("CONFLICT_001", "Concurrent modification detected"),
    RESOURCE_LOCKED("CONFLICT_002", "Resource is currently locked"),
    
    // System Errors
    INTERNAL_SERVER_ERROR("SYSTEM_001", "Internal server error"),
    DATABASE_ERROR("SYSTEM_002", "Database operation failed"),
    EXTERNAL_SERVICE_ERROR("SYSTEM_003", "External service error");
    
    private final String code;
    private final String defaultMessage;
    
    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
    
    public String getCode() { return code; }
    public String getDefaultMessage() { return defaultMessage; }
}
```

### Specific Domain Exceptions

#### Journey Definition Not Found

```java
@Getter
@EqualsAndHashCode(callSuper = true)
public class JourneyDefinitionNotFoundException extends DomainException {
    private final String journeyDefinitionId;
    
    public JourneyDefinitionNotFoundException(String journeyDefinitionId) {
        super(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, 
              String.format("Journey definition with ID '%s' not found", journeyDefinitionId));
        this.journeyDefinitionId = journeyDefinitionId;
        withContext("journeyDefinitionId", journeyDefinitionId);
    }
}
```

#### Journey Instance Not Found

```java
@Getter
@EqualsAndHashCode(callSuper = true)
public class JourneyInstanceNotFoundException extends DomainException {
    private final String journeyInstanceId;
    
    public JourneyInstanceNotFoundException(String journeyInstanceId) {
        super(ErrorCode.JOURNEY_INSTANCE_NOT_FOUND, 
              String.format("Journey instance with ID '%s' not found", journeyInstanceId));
        this.journeyInstanceId = journeyInstanceId;
        withContext("journeyInstanceId", journeyInstanceId);
    }
}
```

#### Invalid State Transition

```java
@Getter
@EqualsAndHashCode(callSuper = true)
public class InvalidStateTransitionException extends DomainException {
    private final String currentState;
    private final String targetState;
    private final String journeyInstanceId;
    
    public InvalidStateTransitionException(String journeyInstanceId, 
                                           String currentState, 
                                           String targetState) {
        super(ErrorCode.INVALID_STATE_TRANSITION, 
              String.format("Cannot transition from '%s' to '%s' for journey instance '%s'", 
                           currentState, targetState, journeyInstanceId));
        this.journeyInstanceId = journeyInstanceId;
        this.currentState = currentState;
        this.targetState = targetState;
        withContext("journeyInstanceId", journeyInstanceId);
        withContext("currentState", currentState);
        withContext("targetState", targetState);
    }
}
```

#### Journey Already Completed

```java
@Getter
@EqualsAndHashCode(callSuper = true)
public class JourneyAlreadyCompletedException extends DomainException {
    private final String journeyInstanceId;
    private final LocalDateTime completionTime;
    
    public JourneyAlreadyCompletedException(String journeyInstanceId, 
                                           LocalDateTime completionTime) {
        super(ErrorCode.JOURNEY_ALREADY_COMPLETED, 
              String.format("Journey instance '%s' has already completed at %s", 
                           journeyInstanceId, completionTime));
        this.journeyInstanceId = journeyInstanceId;
        this.completionTime = completionTime;
        withContext("journeyInstanceId", journeyInstanceId);
        withContext("completionTime", completionTime);
    }
}
```

## Error Response Model

### ProblemDetail Enhancement

```java
@Getter
@Builder
public class ErrorResponseProblemDetail {
    private final ProblemDetail problemDetail;
    private final String errorCode;
    private final Instant timestamp;
    private final String path;
    private final Map<String, Object> additionalContext;
    
    public static ErrorResponseProblemDetail from(DomainException ex, 
                                                 HttpServletRequest request) {
        return ErrorResponseProblemDetail.builder()
            .problemDetail(createProblemDetail(ex))
            .errorCode(ex.getErrorCode().getCode())
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .additionalContext(ex.getContext())
            .build();
    }
    
    private static ProblemDetail createProblemDetail(DomainException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            getHttpStatusForErrorCode(ex.getErrorCode()),
            ex.getDetails()
        );
        
        problemDetail.setTitle(ex.getErrorCode().getDefaultMessage());
        problemDetail.setType(URI.create("https://api.journey-orchestrator.com/errors/" + 
                                        ex.getErrorCode().getCode().toLowerCase()));
        
        // Add error code as a custom property
        problemDetail.setProperty("errorCode", ex.getErrorCode().getCode());
        
        return problemDetail;
    }
    
    private static HttpStatus getHttpStatusForErrorCode(ErrorCode errorCode) {
        return switch (errorCode.getCategory()) {
            case JOURNEY_DEFINITION_NOT_FOUND, JOURNEY_INSTANCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_STATE_TRANSITION, JOURNEY_ALREADY_COMPLETED -> HttpStatus.UNPROCESSABLE_ENTITY;
            case CONCURRENT_MODIFICATION, RESOURCE_LOCKED -> HttpStatus.CONFLICT;
            case VALIDATION_FAILED, INVALID_REQUEST_FORMAT, MISSING_REQUIRED_FIELD -> HttpStatus.BAD_REQUEST;
            case INTERNAL_SERVER_ERROR, DATABASE_ERROR, EXTERNAL_SERVICE_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
```

## Validation Error Model

### Field Validation Error

```java
@Getter
@Builder
public class FieldValidationError {
    private final String field;
    private final String errorCode;
    private final String message;
    private final Object rejectedValue;
}
```

### Validation Error Response

```java
@Getter
@Builder
public class ValidationErrorResponse {
    private final ProblemDetail problemDetail;
    private final String errorCode;
    private final Instant timestamp;
    private final String path;
    private final List<FieldValidationError> fieldErrors;
    
    public static ValidationErrorResponse from(MethodArgumentNotValidException ex, 
                                             HttpServletRequest request) {
        List<FieldValidationError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldValidationError::from)
            .collect(Collectors.toList());
            
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Request validation failed"
        );
        
        problemDetail.setTitle("Validation Failed");
        problemDetail.setType(URI.create("https://api.journey-orchestrator.com/errors/validation_001"));
        problemDetail.setProperty("errorCode", "VALIDATION_001");
        problemDetail.setProperty("fieldCount", fieldErrors.size());
        
        return ValidationErrorResponse.builder()
            .problemDetail(problemDetail)
            .errorCode("VALIDATION_001")
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .fieldErrors(fieldErrors)
            .build();
    }
}
```

## Relationships and Constraints

### Exception Hierarchy Constraints

1. **DomainException** is the root of all business logic exceptions
2. All domain exceptions must have an **ErrorCode** from the enumeration
3. Exception context is immutable once created
4. Error codes are unique across the entire application

### Error Response Constraints

1. All error responses must include **timestamp** and **path**
2. **ProblemDetail** follows RFC 9457 specification
3. **errorCode** is always included as a custom property
4. Sensitive information is never included in error responses

### Validation Constraints

1. Field validation errors include the **rejected value** for debugging
2. Multiple validation errors are aggregated in a single response
3. Validation error codes follow the `VALIDATION_XXX` pattern

## State Transitions

### Error Processing Flow

1. **Exception Occurs** → Domain or framework layer
2. **Global Exception Handler** → Catches and processes exception
3. **Error Code Mapping** → Maps exception to HTTP status
4. **ProblemDetail Creation** → Creates RFC 9457 compliant response
5. **Response Serialization** → Converts to JSON format
6. **Logging** → Logs error with appropriate context

### Validation Error Flow

1. **Request Validation** → Spring validates incoming request
2. **Validation Failure** → MethodArgumentNotValidException thrown
3. **Error Aggregation** → Multiple field errors collected
4. **Response Creation** → ValidationErrorResponse created
5. **Client Response** → Detailed validation errors returned
