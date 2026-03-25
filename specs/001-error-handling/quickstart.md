# Quick Start Guide: Standardized Error Handling

This guide demonstrates how to use the standardized error handling mechanism in the Journey Orchestrator service.

## Overview

The error handling system provides:
- RFC 9457-compliant error responses
- Domain-specific exceptions
- Centralized error handling
- Standardized error codes
- Comprehensive logging

## For API Consumers

### Error Response Format

All error responses follow the RFC 9457 ProblemDetail format:

```json
{
  "type": "https://api.journey-orchestrator.com/errors/journey_001",
  "title": "Journey definition not found",
  "status": 404,
  "detail": "Journey definition with ID 'abc123' not found",
  "instance": "/api/journeys/definitions/abc123",
  "errorCode": "JOURNEY_001",
  "timestamp": "2025-03-25T15:30:00Z",
  "path": "/api/journeys/definitions/abc123",
  "additionalContext": {
    "journeyDefinitionId": "abc123"
  }
}
```

### Common Error Codes

| Category | Error Code | HTTP Status | Description |
|----------|------------|-------------|-------------|
| Journey Definition | `JOURNEY_001` | 404 | Journey definition not found |
| Journey Instance | `JOURNEY_101` | 404 | Journey instance not found |
| State Transition | `STATE_001` | 422 | Invalid state transition |
| Validation | `VALIDATION_001` | 400 | Request validation failed |
| Conflict | `CONFLICT_001` | 409 | Concurrent modification |
| System | `SYSTEM_001` | 500 | Internal server error |

### Handling Errors in Client Code

```javascript
// Example: Handling journey definition not found
try {
  const response = await fetch('/api/journeys/definitions/abc123');
  if (!response.ok) {
    const error = await response.json();
    
    // Handle specific error codes
    switch (error.errorCode) {
      case 'JOURNEY_001':
        console.log('Journey definition not found:', error.detail);
        // Show user-friendly message
        break;
      case 'VALIDATION_001':
        console.log('Validation errors:', error.fieldErrors);
        // Display field-specific errors
        break;
      default:
        console.log('Unexpected error:', error.detail);
    }
  }
} catch (error) {
  console.error('Network error:', error);
}
```

## For Developers

### Creating Domain Exceptions

#### 1. Define Error Code

Add to `ErrorCode` enum:

```java
public enum ErrorCode {
    // Existing codes...
    JOURNEY_DEFINITION_EXPIRED("JOURNEY_004", "Journey definition has expired"),
    // ...
    
    private final String code;
    private final String defaultMessage;
    
    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
```

#### 2. Create Exception Class

```java
@Getter
@EqualsAndHashCode(callSuper = true)
public class JourneyDefinitionExpiredException extends DomainException {
    private final String journeyDefinitionId;
    private final LocalDateTime expirationDate;
    
    public JourneyDefinitionExpiredException(String journeyDefinitionId, 
                                           LocalDateTime expirationDate) {
        super(ErrorCode.JOURNEY_DEFINITION_EXPIRED, 
              String.format("Journey definition '%s' expired on %s", 
                           journeyDefinitionId, expirationDate));
        this.journeyDefinitionId = journeyDefinitionId;
        this.expirationDate = expirationDate;
        withContext("journeyDefinitionId", journeyDefinitionId);
        withContext("expirationDate", expirationDate);
    }
}
```

#### 3. Use in Domain Logic

```java
@Service
public class JourneyDefinitionService {
    
    public JourneyDefinition getJourneyDefinition(String id) {
        JourneyDefinition definition = repository.findById(id)
            .orElseThrow(() -> new JourneyDefinitionNotFoundException(id));
            
        if (definition.isExpired()) {
            throw new JourneyDefinitionExpiredException(id, definition.getExpirationDate());
        }
        
        return definition;
    }
}
```

### Handling Validation Errors

#### Request DTO with Validation

```java
@Data
@Builder
public class CreateJourneyDefinitionRequest {
    
    @NotBlank(message = "Journey definition name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;
    
    @NotBlank(message = "Journey definition version is required")
    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "Version must follow semantic versioning (x.y.z)")
    private String version;
    
    @Valid
    @NotEmpty(message = "At least one state must be defined")
    private List<StateDefinition> states;
}
```

#### Controller with Validation

```java
@RestController
@RequestMapping("/api/journeys/definitions")
@Validated
public class JourneyDefinitionController {
    
    @PostMapping
    public ResponseEntity<JourneyDefinitionResponse> createJourneyDefinition(
            @Valid @RequestBody CreateJourneyDefinitionRequest request) {
        // Validation errors are automatically handled by GlobalExceptionHandler
        JourneyDefinition created = service.create(request);
        return ResponseEntity.ok(mapper.toResponse(created));
    }
}
```

### Custom Exception Handling

If you need to handle specific exceptions differently:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // Standard domain exception handling
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(
            DomainException ex, WebRequest request) {
        ErrorResponseProblemDetail response = ErrorResponseProblemDetail.from(ex, request);
        return ResponseEntity.status(response.getProblemDetail().getStatus())
                             .body(response.getProblemDetail());
    }
    
    // Custom handling for specific exceptions
    @ExceptionHandler(JourneyDefinitionExpiredException.class)
    public ResponseEntity<ProblemDetail> handleJourneyExpired(
            JourneyDefinitionExpiredException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.GONE, // 410 Gone is more appropriate for expired resources
            ex.getDetails()
        );
        
        problemDetail.setTitle("Journey Definition Expired");
        problemDetail.setType(URI.create("https://api.journey-orchestrator.com/errors/journey_004"));
        problemDetail.setProperty("errorCode", ex.getErrorCode().getCode());
        problemDetail.setProperty("expirationDate", ex.getExpirationDate());
        
        return ResponseEntity.status(HttpStatus.GONE).body(problemDetail);
    }
}
```

### Testing Exception Handling

#### Unit Tests for Domain Exceptions

```java
@ExtendWith(MockitoExtension.class)
class JourneyDefinitionServiceTest {
    
    @Test
    void shouldThrowNotFoundExceptionWhenDefinitionNotExists() {
        // Given
        String definitionId = "nonexistent";
        when(repository.findById(definitionId)).thenReturn(Optional.empty());
        
        // When & Then
        JourneyDefinitionNotFoundException exception = assertThrows(
            JourneyDefinitionNotFoundException.class,
            () -> service.getJourneyDefinition(definitionId)
        );
        
        assertEquals(ErrorCode.JOURNEY_DEFINITION_NOT_FOUND, exception.getErrorCode());
        assertEquals(definitionId, exception.getJourneyDefinitionId());
        assertTrue(exception.getContext().containsKey("journeyDefinitionId"));
    }
}
```

#### Integration Tests for Exception Handler

```java
@SpringBootTest
@AutoConfigureTestDatabase
class GlobalExceptionHandlerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldReturn404WhenJourneyDefinitionNotFound() {
        // When
        ResponseEntity<ProblemDetail> response = restTemplate.getForEntity(
            "/api/journeys/definitions/nonexistent", 
            ProblemDetail.class
        );
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();
        assertNotNull(problemDetail);
        assertEquals("JOURNEY_001", problemDetail.getProperties().get("errorCode"));
        assertEquals("Journey definition not found", problemDetail.getTitle());
    }
    
    @Test
    void shouldReturn400WhenValidationFails() {
        // Given
        CreateJourneyDefinitionRequest invalidRequest = CreateJourneyDefinitionRequest.builder()
            .name("") // Invalid: empty name
            .version("invalid") // Invalid: doesn't match pattern
            .states(Collections.emptyList()) // Invalid: empty states
            .build();
        
        // When
        ResponseEntity<ProblemDetail> response = restTemplate.postForEntity(
            "/api/journeys/definitions", 
            invalidRequest, 
            ProblemDetail.class
        );
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();
        assertNotNull(problemDetail);
        assertEquals("VALIDATION_001", problemDetail.getProperties().get("errorCode"));
        assertTrue(problemDetail.getDetail().contains("validation failed"));
    }
}
```

## Error Code Reference

### Journey Definition Errors
- `JOURNEY_001`: Journey definition not found
- `JOURNEY_002`: Journey definition already exists
- `JOURNEY_003`: Journey definition is invalid
- `JOURNEY_004`: Journey definition has expired

### Journey Instance Errors
- `JOURNEY_101`: Journey instance not found
- `JOURNEY_102`: Journey has already completed
- `JOURNEY_103`: Journey has already started

### State Transition Errors
- `STATE_001`: Invalid state transition
- `STATE_002`: State transition not allowed in current state

### Validation Errors
- `VALIDATION_001`: Validation failed
- `VALIDATION_002`: Invalid request format
- `VALIDATION_003`: Missing required field

### Conflict Errors
- `CONFLICT_001`: Concurrent modification detected
- `CONFLICT_002`: Resource is currently locked

### System Errors
- `SYSTEM_001`: Internal server error
- `SYSTEM_002`: Database operation failed
- `SYSTEM_003`: External service error

## Best Practices

1. **Use Specific Exceptions**: Always use the most specific exception type for your use case
2. **Provide Context**: Include relevant business context in exceptions
3. **Don't Expose Sensitive Data**: Error messages should not contain passwords, tokens, or internal system details
4. **Log Appropriately**: Use appropriate log levels (ERROR for system errors, WARN for business violations)
5. **Handle Errors Gracefully**: Always handle potential exceptions and provide meaningful feedback to users
6. **Use Error Codes**: Use error codes for programmatic error handling rather than parsing error messages
7. **Test Error Scenarios**: Include both positive and negative test cases for all error conditions
