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
- Use Spring Boot test annotations for integration testing

## Development Workflow

1. **Define API Contract**: Update the central `api-spec/openapi.yaml` FIRST - this is the single source of truth
2. **Generate Code**: Run Maven generate-sources to create API stubs from the OpenAPI specification
3. **Implement Domain**: Create domain entities and business logic
4. **Implement Application**: Create services and use cases
5. **Implement Adapters**: Create controllers and repositories
6. **Test**: Write unit and integration tests
7. **Validate**: Run contract tests to ensure API compliance with the OpenAPI specification

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
- **Audit Trail**: All state transitions must be recorded
- **Versioned Definitions**: Journey definitions support versioning

## Quality Gates

- All code MUST follow the established package structure
- API changes MUST be reflected in the central `api-spec/openapi.yaml` specification FIRST before any implementation
- Domain logic MUST be testable without infrastructure
- Services MUST depend on abstractions (ports), not concretions
- All public APIs MUST have appropriate validation
- **Specification-Driven Development**: API contracts MUST be defined in OpenAPI before implementation - this is MANDATORY, not optional
