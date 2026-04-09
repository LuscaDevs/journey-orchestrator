# Journey Orchestrator - Project Constitution

## Architecture Principles

This project follows modern enterprise architecture patterns:

- **Specification-Driven Development (Spec First)**: API contracts MUST be defined in the central OpenAPI specification (`api-spec/openapi.yaml`) BEFORE any implementation. All code generation flows from this single source of truth.
- **Clean Architecture**: Hexagonal architecture with clear separation of concerns
- **Domain-Driven Design (DDD)**: Domain-centric modeling with rich domain objects
- **SOLID Principles**: Single responsibility, open/closed, Liskov substitution, interface segregation, dependency inversion
- **Business-Agnostic Orchestration**: No business rules embedded in the orchestrator layer

## Technology Stack

- **Java 21**: Latest LTS version with modern language features
- **Spring Boot 4.0.3**: Main framework with auto-configuration
- **MongoDB**: Primary persistence layer for journey data
- **OpenAPI 3.0.3**: API specification and code generation
- **Lombok**: Boilerplate code reduction
- **Maven**: Build and dependency management
- **RestAssured 5.4.0**: API testing framework for E2E testing
- **Testcontainers 1.19.7**: Containerized test environments for E2E testing
- **JUnit 5**: Modern testing framework with nested tests and method ordering
- **Awaitility 4.2.0**: Asynchronous testing support for E2E scenarios
- **JSON Schema Validator 1.0.87**: OpenAPI contract validation in tests

## Project Structure

```
src/main/java/com/luscadevs/journeyorchestrator/
├── JourneyorchestratorApplication.java          # Main Spring Boot application
├── api/                                         # API layer (controllers, DTOs, mappers)
├── application/                                 # Application services and use cases
│   ├── engine/                                 # State machine engine
│   ├── port/                                   # Input/Output ports (interfaces)
│   └── service/                                # Application services
├── domain/                                     # Core domain logic
│   ├── engine/                                 # Domain engine interfaces
│   ├── journey/                                # Journey definition domain
│   └── journeyinstance/                        # Journey instance domain
└── adapters/                                   # Infrastructure adapters
    ├── in/web/                                 # REST controllers
    ├── out/memory/                             # In-memory repositories
    └── out/persistence/mongo/                   # MongoDB persistence
```

## Coding Standards

## Observability and Logging Standards

The system must provide consistent and structured logging to ensure observability, traceability, and operational debugging.

### Logging Framework

- Logging MUST use SLF4J with Logback.
- Log formatting and appenders MUST be configured through `logback-spring.xml`.
- Log levels MUST be configurable via `application.yml`.

### Correlation and Context

All logs generated during a request MUST include the following MDC fields:

- `correlationId`: Unique identifier for the request lifecycle
- `httpMethod`: HTTP method of the request
- `requestPath`: Requested API path
- `errorCode`: Domain error code when applicable

A request filter MUST populate these fields at the beginning of each request.

### Execution Logging

The system SHOULD provide automatic execution logging for important execution boundaries such as:

- REST controllers
- application services

Execution logs SHOULD capture:

- method start
- method completion
- execution duration
- error cases

### Sensitive Data Protection

Logs MUST NOT expose sensitive data such as:

- authentication tokens
- passwords
- personal identifiable information (PII)
- secrets or credentials

Logging arguments or payloads MUST be sanitized when necessary.

### Logging Responsibility by Layer

- **Controllers**: log request handling and high-level operations
- **Application Services**: log orchestration and business flow
- **Domain Layer**: SHOULD avoid logging unless strictly necessary
- **Infrastructure**: log external interactions (database, messaging, APIs)

### Log Levels

Use the following guidelines:

- `DEBUG`: internal diagnostics and development information
- `INFO`: normal system operations
- `WARN`: recoverable issues or unexpected states
- `ERROR`: failures that prevent the expected behavior

### Observability Goals

Logging must enable:

- request traceability using correlation IDs
- diagnosis of production issues
- measurement of execution latency
- auditing of critical system operations

### Package Structure
- Base package: `com.luscadevs.journeyorchestrator`
- Follow hexagonal architecture with clear layer separation
- Domain layer contains only business logic, no framework dependencies
- Application layer orchestrates use cases
- Adapters handle infrastructure concerns

### Class Naming Conventions
- **Domain Entities**: `JourneyDefinition`, `JourneyInstance`, `State`, `Transition`, `Event`
- **Services**: `JourneyInstanceService`, `JourneyDefinitionService`
- **Controllers**: `JourneyController`, `JourneyDefinitionController`
- **Repositories**: `JourneyInstanceRepositoryPort`, `JourneyDefinitionRepositoryPort`
- **DTOs**: `JourneyInstanceResponse`, `StartJourneyRequest`, `ApplyEventRequest`
- **Mappers**: `JourneyInstanceMapper`

### Code Style
- Use Lombok annotations (`@Getter`, `@Builder`) to reduce boilerplate
- Domain objects should be immutable where possible
- Use constructor injection for dependencies
- Follow Spring Boot conventions for component scanning
- Use `@Service` for application services, `@RestController` for web controllers

### Domain Modeling
- Domain entities contain business logic and validation
- Use value objects for concepts without identity
- Domain services for complex business operations
- Repository pattern for persistence abstraction
- Events represent external triggers for state transitions

### API Design
- RESTful endpoints following resource-based naming
- OpenAPI specification is the single source of truth
- Use DTOs for request/response objects
- Map domain objects to DTOs using dedicated mappers
- Return appropriate HTTP status codes

### Error Handling
- Use runtime exceptions for business rule violations
- Provide meaningful error messages
- Consider creating custom exception classes for domain-specific errors

### Testing
- Unit tests for domain logic
- Integration tests for application services
- Contract tests for API compliance
- **End-to-End (E2E) Tests**: Comprehensive testing of complete journey workflows using RestAssured, Testcontainers, and JUnit 5
- Use Spring Boot test annotations for integration testing
- **E2E Test Framework**: All flow-affecting features MUST maintain functional E2E tests that validate complete user journeys

## Development Workflow

1. **Define API Contract**: Update the central `api-spec/openapi.yaml` FIRST - this is the single source of truth
2. **Generate Code**: Run Maven generate-sources to create API stubs from the OpenAPI specification
3. **Implement Domain**: Create domain entities and business logic
4. **Implement Application**: Create services and use cases
5. **Implement Adapters**: Create controllers and repositories
6. **Test**: Write unit and integration tests
7. **E2E Test Validation**: Create/update E2E tests to validate complete journey workflows for all flow-affecting changes
8. **Validate**: Run contract tests to ensure API compliance with the OpenAPI specification
9. **E2E Regression**: Run full E2E test suite to ensure no regressions in journey functionality

## Configuration Management

- Use Spring Boot's application.properties/yaml for configuration
- Environment-specific profiles for different deployment environments
- MongoDB connection configuration
- OpenAPI generation configuration in Maven plugin

## Documentation

- API documentation generated from OpenAPI specification
- Architecture Decision Records (ADRs) in `docs/adr/`
- Domain model documentation in `docs/domain-model.md`
- Journey DSL documentation in `docs/journey-dsl.md`

## Key Constraints

- **No Business Logic in Adapters**: Keep infrastructure concerns separate
- **Domain Layer Purity**: No framework dependencies in domain
- **Interface Segregation**: Use ports for input/output boundaries
- **Event-Driven Transitions**: State changes triggered by external events
- **Audit Trail**: All state transitions MUST be recorded. Each transition record MUST include:
  - journeyInstanceId
  - transitionId
  - fromState
  - toState
  - timestamp
- **Versioned Definitions**: Journey definitions support versioning

## Quality Gates

- All code MUST follow the established package structure
- API changes MUST be reflected in the central `api-spec/openapi.yaml` specification FIRST before any implementation
- Domain logic MUST be testable without infrastructure
- Services MUST depend on abstractions (ports), not concretions
- All public APIs MUST have appropriate validation
- **Specification-Driven Development**: API contracts MUST be defined in OpenAPI before implementation - this is MANDATORY, not optional
- **E2E Test Compliance**: All flow-affecting features MUST pass the complete E2E test suite before merge
- **E2E Regression Prevention**: No new feature can be merged without ensuring existing E2E tests continue to pass
- **Performance Gate**: E2E tests must validate performance thresholds (<2 second average response time for 100+ concurrent instances)
- **Coverage Gate**: E2E tests must achieve 95%+ coverage of critical journey workflows
