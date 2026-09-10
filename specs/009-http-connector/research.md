# Research: Connector Framework (HTTP Connector)

**Feature**: 009-http-connector
**Date**: 2025-06-27
**Purpose**: Document technical decisions and best practices for implementing the connector framework

## HTTP Client Library Selection

### Decision: Spring WebClient

**Rationale**:
- Spring WebClient is the modern, non-blocking HTTP client recommended by Spring
- Built-in support for reactive programming (though we'll use it synchronously for this implementation)
- Excellent integration with Spring Boot 4.0.3
- Better testability with mock servers
- Future-proof for potential async execution in future features
- Superior timeout and error handling capabilities compared to RestTemplate

**Alternatives Considered**:
- **RestTemplate**: Deprecated in Spring Boot 2.x, not recommended for new projects
- **Apache HttpClient**: More configuration overhead, less Spring integration
- **OkHttp**: Excellent library but requires additional dependency, less Spring-native integration

**Implementation Notes**:
- Use WebClient.Builder for configuration (timeouts, connection pool)
- Configure default timeout of 30 seconds (configurable via application.yml)
- Use synchronous block() calls for this implementation (async is out of scope)
- Implement proper exception handling for network errors, timeouts, HTTP errors

## Context Variable Resolution Strategy

### Decision: Simple String Interpolation with Placeholder Syntax

**Rationale**:
- Simple and intuitive for journey designers
- Standard placeholder syntax `${variableName}` matches common templating patterns
- Easy to implement and test
- No external dependencies required
- Aligns with assumption in spec that context variables use standard placeholder syntax

**Implementation Approach**:
- Use regex pattern `\$\{([^}]+)\}` to identify placeholders
- Resolve placeholders by looking up keys in ExecutionContext
- Replace placeholders with actual values before HTTP request execution
- Handle missing variables gracefully (fail with clear error message)
- Support nested object access using dot notation (e.g., `${customer.id}`)

**Alternatives Considered**:
- **SpEL (Spring Expression Language)**: More powerful but adds complexity and potential security concerns
- **Mustache/Handlebars**: External dependencies, overkill for simple variable substitution
- **Custom DSL**: Would require more implementation effort and user training

## JSON Parsing for Response Handling

### Decision: Jackson ObjectMapper (Spring Boot Default)

**Rationale**:
- Jackson is the default JSON library in Spring Boot
- Already included as a transitive dependency
- Excellent performance and reliability
- Strong type safety with POJO mapping
- Supports complex nested structures
- Well-maintained and widely adopted

**Implementation Approach**:
- Parse HTTP response body as JSON when Content-Type is application/json
- Convert JSON response to Map<String, Object> for context enrichment
- Handle malformed JSON gracefully with clear error messages
- Support JSONPath-style extraction for specific fields (future enhancement)
- Preserve non-JSON responses as raw strings in context

**Alternatives Considered**:
- **Gson**: Good alternative but Jackson is Spring Boot default
- **JSON-B**: Standard API but less flexible than Jackson
- **Manual parsing**: Too error-prone and time-consuming

## MongoDB Document Structure for Connector Execution Records

### Decision: Embedded Document in Journey Instance

**Rationale**:
- Connector execution records are tightly coupled to journey instances
- Embedded documents provide better read performance for audit queries
- Simplifies query patterns (single document per journey instance)
- Aligns with existing MongoDB usage in the project
- Natural fit for audit trail requirements

**Document Structure**:
```java
@Document(collection = "journey_instances")
class JourneyInstanceDocument {
    // existing fields...
    
    @Field("connector_executions")
    private List<ConnectorExecutionDocument> connectorExecutions;
}

class ConnectorExecutionDocument {
    private String id;
    private String connectorType;
    private String connectorConfiguration;
    private Instant startTime;
    private Instant endTime;
    private Duration duration;
    private String status; // SUCCESS, FAILURE
    private String result;
    private String errorMessage;
}
```

**Alternatives Considered**:
- **Separate collection**: Would require additional queries and joins, more complex
- **Separate database**: Overkill for this use case, adds operational complexity

## Strategy Pattern Implementation for Connector Extensibility

### Decision: Interface-Based Strategy with Spring Bean Resolution

**Rationale**:
- Clean separation between runtime and connector implementations
- Spring's dependency injection makes strategy resolution trivial
- Easy to add new connector types by implementing the interface
- Supports configuration-driven connector selection
- Maintains hexagonal architecture principles

**Implementation Approach**:
```java
// Domain interface
public interface Connector {
    ConnectorResult execute(ConnectorConfiguration config, ExecutionContext context);
}

// HTTP implementation
@Component
public class HttpConnector implements Connector {
    // HTTP-specific implementation
}

// Resolver using Spring's ApplicationContext
@Service
public class ConnectorResolver {
    private final Map<ConnectorType, Connector> connectors;
    
    public Connector getConnector(ConnectorType type) {
        return connectors.get(type);
    }
}
```

**Alternatives Considered**:
- **Factory pattern**: Similar benefits but more boilerplate
- **ServiceLoader**: Requires META-INF configuration, less Spring-native
- **Reflection-based**: More complex, harder to debug, less type-safe

## Error Handling and Timeout Strategies

### Decision: Layered Exception Handling with Custom Domain Exceptions

**Rationale**:
- Clear separation between technical errors and business errors
- Enables specific error handling at different layers
- Provides meaningful error messages for audit trails
- Supports different error responses for different failure types
- Aligns with constitution's error handling guidelines

**Exception Hierarchy**:
```java
// Base exception
public class ConnectorException extends RuntimeException {
    private final ConnectorType connectorType;
    private final String connectorId;
}

// Specific exceptions
public class ConnectorTimeoutException extends ConnectorException { }
public class ConnectorConfigurationException extends ConnectorException { }
public class ConnectorExecutionException extends ConnectorException { }
public class ConnectorNetworkException extends ConnectorException { }
```

**Timeout Strategy**:
- Default timeout: 30 seconds (configurable)
- Use WebClient's timeout configuration
- Throw ConnectorTimeoutException on timeout
- Record timeout in audit trail with duration
- Stop journey execution on timeout

**Alternatives Considered**:
- **Generic exceptions**: Less informative, harder to debug
- **Retry immediately**: Out of scope for this delivery
- **Circuit breaker**: Out of scope for this delivery

## Audit Trail Persistence Strategy

### Decision: Immediate Persistence with MongoDB Transactions

**Rationale**:
- Audit trail is critical for observability and debugging
- Immediate persistence ensures no data loss on system failure
- MongoDB transactions ensure consistency with journey instance state
- Supports real-time audit queries
- Aligns with constitution's audit trail requirements

**Implementation Approach**:
- Persist connector execution record immediately after execution
- Use MongoDB transactions if journey instance update is also happening
- Include all required fields: connector type, timestamps, duration, result, status, error
- Index on journeyInstanceId for efficient queries
- Index on timestamp for time-based audit queries

**Alternatives Considered**:
- **Async persistence**: Risk of data loss, violates audit requirements
- **Event sourcing**: Overkill for this use case, adds complexity
- **Separate audit service**: Adds operational complexity, not needed

## State Type Extension Strategy

### Decision: Enum Extension with Backward Compatibility

**Rationale**:
- Existing State enum needs SERVICE_TASK type
- Enum extension maintains type safety
- Backward compatibility with existing journey definitions
- Simple and straightforward implementation
- No migration required for existing data

**Implementation Approach**:
```java
public enum StateType {
    START,
    END,
    WAITING_FOR_EVENT,
    SERVICE_TASK, // NEW
    // existing types...
}
```

**Alternatives Considered**:
- **String-based types**: Less type-safe, more error-prone
- **Separate enum for connector states**: Unnecessary complexity
- **Polymorphic state classes**: Overkill for this use case

## Context Enrichment Strategy

### Decision: Deep Merge with Conflict Resolution

**Rationale**:
- Preserves existing context data
- Allows connectors to add new data without overwriting
- Supports nested object structures
- Clear conflict resolution strategy (last write wins)
- Aligns with spec requirement to preserve existing data

**Implementation Approach**:
- Use deep merge algorithm for Map<String, Object>
- New keys from connector result are added to context
- Existing keys are overwritten by connector result (last write wins)
- Support nested object merging
- Log merge operations for debugging

**Alternatives Considered**:
- **Shallow merge**: Doesn't handle nested objects correctly
- **Replace entire context**: Violates spec requirement to preserve existing data
- **Strict merge (fail on conflict)**: Too restrictive, hard to use

## Testing Strategy

### Decision: Multi-Layer Testing with Testcontainers

**Rationale**:
- Aligns with constitution's testing guidelines
- Testcontainers provides real MongoDB for integration tests
- RestAssured for API contract testing
- Mockito for unit testing
- E2E tests validate complete workflows
- Ensures quality and prevents regressions

**Test Coverage**:
- Unit tests for domain entities and business logic
- Integration tests for application services
- Contract tests for API compliance
- E2E tests for complete connector workflows
- Performance tests for connector execution thresholds

## Summary

All technical decisions align with:
- Project constitution and architecture principles
- Spring Boot 4.0.3 best practices
- Hexagonal architecture patterns
- Clean architecture principles
- Technology stack constraints
- Performance and scalability requirements

No technical blockers identified. Ready to proceed to Phase 1 design.
