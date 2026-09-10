# Implementation Plan: Connector Framework (HTTP Connector)

**Branch**: `009-http-connector` | **Date**: 2025-06-27 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/009-http-connector/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

This feature introduces a Connector Framework to the Journey Orchestrator, enabling external integrations to be executed automatically when journey instances enter SERVICE_TASK states. The implementation follows the Strategy pattern to allow extensibility, with HTTP Connector as the first concrete implementation. The runtime will delegate connector execution without knowledge of protocol-specific details, maintaining clean architecture principles. The system will support context variable resolution, context enrichment from connector results, and comprehensive audit trails for all connector executions.

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 4.0.3, Spring Web, MongoDB Driver, Lombok, Jackson (JSON processing)
**Storage**: MongoDB (existing for journey data, will extend for connector execution records)
**Testing**: JUnit 5, RestAssured 5.4.0, Testcontainers 1.19.7, Awaitility 4.2.0, Mockito
**Target Platform**: Linux server (JVM-based)
**Project Type**: web-service (REST API with state machine engine)
**Performance Goals**: <2 second average response time for 100+ concurrent instances, connector execution within 500ms of state entry
**Constraints**: Hexagonal architecture, domain layer purity, no framework dependencies in domain, specification-driven development (OpenAPI first)
**Scale/Scope**: Enterprise-grade orchestration platform, supporting multiple journey definitions and concurrent instances

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Architecture Principles Compliance

- **Specification-Driven Development**: ✅ PASS - Will update OpenAPI specification first before implementation
- **Clean Architecture**: ✅ PASS - Connector framework follows hexagonal architecture with clear layer separation
- **Domain-Driven Design**: ✅ PASS - Connector entities will be domain objects with business logic
- **SOLID Principles**: ✅ PASS - Strategy pattern ensures open/closed, interface segregation for connector types
- **Business-Agnostic Orchestration**: ✅ PASS - Runtime remains protocol-agnostic, delegates to connectors

### Technology Stack Compliance

- **Java 21**: ✅ PASS - Using specified version
- **Spring Boot 4.0.3**: ✅ PASS - Using specified version
- **MongoDB**: ✅ PASS - Extending existing MongoDB persistence for connector execution records
- **OpenAPI 3.0.3**: ✅ PASS - Will update OpenAPI spec before implementation
- **Lombok**: ✅ PASS - Will use for boilerplate reduction
- **Maven**: ✅ PASS - Using existing build system
- **Testing Stack**: ✅ PASS - Will use JUnit 5, RestAssured, Testcontainers, Awaitility

### Package Structure Compliance

- **Base Package**: ✅ PASS - Will use `com.luscadevs.journeyorchestrator`
- **Hexagonal Architecture**: ✅ PASS - Will create connector packages in appropriate layers:
  - `domain/connector/` - Domain entities and interfaces
  - `application/port/` - Connector ports (input/output)
  - `adapters/out/connector/` - Concrete connector implementations (HTTP, future types)
  - `api/dto/` - Connector-related DTOs
- **Domain Layer Purity**: ✅ PASS - Domain entities will have no framework dependencies
- **Interface Segregation**: ✅ PASS - Will use ports for connector boundaries

### Coding Standards Compliance

- **Class Naming**: ✅ PASS - Will follow conventions (Connector, HttpConnector, ConnectorExecutionRecord)
- **Code Style**: ✅ PASS - Will use Lombok, constructor injection, Spring annotations
- **Domain Modeling**: ✅ PASS - Rich domain objects, value objects, repository pattern
- **API Design**: ✅ PASS - RESTful, DTOs, mappers, appropriate status codes
- **Error Handling**: ✅ PASS - Custom exceptions for connector-specific errors
- **Testing**: ✅ PASS - Unit tests for domain, integration for services, E2E for complete flows

### Development Workflow Compliance

- **OpenAPI First**: ✅ PASS - Will update `api-spec/openapi.yaml` before implementation
- **Code Generation**: ✅ PASS - Will run Maven generate-sources after OpenAPI update
- **Domain First**: ✅ PASS - Will implement domain entities before application layer
- **E2E Tests**: ✅ PASS - Will create comprehensive E2E tests for connector workflows
- **Contract Tests**: ✅ PASS - Will validate API compliance with OpenAPI spec

### Key Constraints Compliance

- **No Business Logic in Adapters**: ✅ PASS - Connector implementations will be in adapters layer but protocol-specific only
- **Domain Layer Purity**: ✅ PASS - Domain entities will have no framework dependencies
- **Interface Segregation**: ✅ PASS - Will use ports for connector boundaries
- **Event-Driven Transitions**: ✅ PASS - Connector execution triggered by state entry events
- **Audit Trail**: ✅ PASS - Will record all connector executions with required fields
- **Versioned Definitions**: ✅ PASS - Journey definitions already support versioning

### Quality Gates Compliance

- **Package Structure**: ✅ PASS - Will follow established structure
- **OpenAPI First**: ✅ PASS - Will update spec before implementation
- **Domain Testability**: ✅ PASS - Domain logic will be testable without infrastructure
- **Interface Dependencies**: ✅ PASS - Services will depend on ports, not concretions
- **API Validation**: ✅ PASS - Will have appropriate validation
- **E2E Test Compliance**: ✅ PASS - Will create E2E tests for connector workflows
- **E2E Regression Prevention**: ✅ PASS - Will ensure existing E2E tests pass
- **Performance Gate**: ✅ PASS - Will validate <500ms connector execution threshold
- **Coverage Gate**: ✅ PASS - Will achieve 95%+ coverage of connector workflows

### Observability Compliance

- **Logging Framework**: ✅ PASS - Will use SLF4J with Logback
- **Correlation Context**: ✅ PASS - Will include correlationId in connector execution logs
- **Execution Logging**: ✅ PASS - Will log connector execution start, completion, duration, errors
- **Sensitive Data Protection**: ✅ PASS - Will sanitize sensitive data in logs (auth tokens, passwords)
- **Log Levels**: ✅ PASS - Will use appropriate levels (INFO for operations, ERROR for failures)

### Constitution Check Result

**STATUS**: ✅ **PASS** - All constitution gates satisfied. No violations requiring justification.

Proceeding to Phase 0 research.

## Project Structure

### Documentation (this feature)

```text
specs/009-http-connector/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── openapi.yaml    # OpenAPI specification updates for connector APIs
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/luscadevs/journeyorchestrator/
├── domain/
│   ├── connector/                              # NEW: Connector domain
│   │   ├── Connector.java                      # Connector interface (Strategy)
│   │   ├── ConnectorType.java                  # Connector type enum
│   │   ├── ConnectorConfiguration.java         # Base configuration entity
│   │   ├── ConnectorExecutionRecord.java       # Execution audit record
│   │   ├── ConnectorResult.java                # Execution result value object
│   │   └── http/                               # HTTP-specific domain
│   │       ├── HttpConnectorConfiguration.java # HTTP configuration entity
│   │       ├── HttpMethod.java                 # HTTP method enum
│   │       └── HttpConnectorResult.java        # HTTP-specific result
│   ├── journey/
│   │   └── State.java                          # EXTEND: Add SERVICE_TASK type
│   └── journeyinstance/
│       └── ExecutionContext.java               # EXTEND: Add connector result handling
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── ConnectorPort.java              # NEW: Input port for connector execution
│   │   └── out/
│   │       ├── ConnectorRepositoryPort.java    # NEW: Repository for execution records
│   │       └── ConnectorResolverPort.java      # NEW: Strategy resolver for connector implementations
│   ├── service/
│   │   └── ConnectorExecutionService.java     # NEW: Orchestrates connector execution
│   └── engine/
│       └── StateTransitionHandler.java        # EXTEND: Add connector execution on SERVICE_TASK entry
├── adapters/
│   ├── out/
│   │   ├── connector/                          # NEW: Connector implementations
│   │   │   ├── http/
│   │   │   │   ├── HttpConnector.java          # HTTP connector implementation
│   │   │   │   ├── HttpClientAdapter.java      # HTTP client wrapper
│   │   │   │   └── ContextVariableResolver.java # Variable resolution utility
│   │   │   └── ConnectorResolver.java           # Strategy resolver implementation
│   │   └── persistence/
│   │       └── mongo/
│   │           ├── ConnectorExecutionDocument.java  # NEW: MongoDB document
│   │           ├── ConnectorExecutionMongoRepository.java # NEW: Repository
│   │           └── ConnectorExecutionRepositoryImpl.java # NEW: Port implementation
│   └── in/
│       └── web/
│           └── dto/
│               ├── ConnectorExecutionResponse.java     # NEW: DTO for audit display
│               └── HttpConnectorConfigurationRequest.java # NEW: DTO for HTTP config
└── api/
    ├── dto/
    │   └── connector/                           # NEW: Connector DTOs
    │       ├── ConnectorConfigurationDto.java
    │       └── HttpConnectorConfigurationDto.java
    └── mapper/
        └── ConnectorMapper.java                # NEW: Domain to DTO mapping

src/test/java/com/luscadevs/journeyorchestrator/
├── domain/
│   └── connector/                              # NEW: Domain tests
│       ├── ConnectorTest.java
│       └── http/
│           └── HttpConnectorConfigurationTest.java
├── application/
│   └── service/
│       └── ConnectorExecutionServiceTest.java  # NEW: Service tests
├── adapters/
│   └── out/
│       └── connector/
│           └── http/
│               └── HttpConnectorTest.java       # NEW: Adapter tests
└── e2e/
    └── ConnectorWorkflowE2ETest.java            # NEW: E2E tests for connector workflows

api-spec/
└── openapi.yaml                                 # UPDATE: Add connector-related endpoints
```

**Structure Decision**: Single project structure following existing hexagonal architecture. Connector domain entities in `domain/connector/`, application services in `application/service/`, concrete implementations in `adapters/out/connector/`. This maintains clean separation of concerns and allows future connector types to be added without modifying core runtime logic.

## Post-Design Constitution Check

*GATE: Re-evaluated after Phase 1 design completion.*

### Design Decisions vs Constitution Compliance

**Research Phase Decisions**:
- **Spring WebClient**: ✅ PASS - Aligns with Spring Boot 4.0.3, modern non-blocking client
- **Jackson ObjectMapper**: ✅ PASS - Spring Boot default JSON library, already in dependencies
- **MongoDB Embedded Documents**: ✅ PASS - Extends existing MongoDB usage, maintains consistency
- **Strategy Pattern**: ✅ PASS - SOLID principles, open/closed for extensibility
- **Simple Variable Interpolation**: ✅ PASS - No external dependencies, maintainable
- **Layered Exception Handling**: ✅ PASS - Custom exceptions, clear separation

**Data Model Decisions**:
- **Domain Entities in domain/connector/**: ✅ PASS - Domain layer purity maintained
- **Interface-Based Strategy**: ✅ PASS - Dependency inversion, interface segregation
- **Value Objects for Results**: ✅ PASS - DDD principles, immutability
- **Embedded MongoDB Documents**: ✅ PASS - Consistent with existing persistence pattern
- **Deep Merge for Context**: ✅ PASS - Preserves existing data as required

**API Contract Decisions**:
- **OpenAPI First**: ✅ PASS - contracts/openapi.yaml created before implementation
- **Backward Compatibility**: ✅ PASS - All changes are additive, no breaking changes
- **RESTful Endpoints**: ✅ PASS - Follows resource-based naming conventions
- **DTOs for Request/Response**: ✅ PASS - Separation of API and domain layers
- **Audit Trail Endpoint**: ✅ PASS - Supports observability requirements

**Architecture Decisions**:
- **Hexagonal Architecture**: ✅ PASS - Clear layer separation maintained
  - Domain: Connector entities and interfaces
  - Application: Services and ports
  - Adapters: Concrete implementations and persistence
- **No Framework Dependencies in Domain**: ✅ PASS - Domain entities use only Java standard library
- **Interface Segregation**: ✅ PASS - Ports for input/output boundaries
- **Business-Agnostic Runtime**: ✅ PASS - Runtime delegates to connectors, no protocol knowledge

**Observability Decisions**:
- **Comprehensive Audit Trail**: ✅ PASS - All required fields recorded (timestamps, status, result, error)
- **SLF4J with Logback**: ✅ PASS - Aligns with constitution logging standards
- **Correlation ID in Logs**: ✅ PASS - Will include in connector execution logs
- **Sensitive Data Protection**: ✅ PASS - Will sanitize auth tokens and passwords in logs

**Testing Decisions**:
- **Multi-Layer Testing**: ✅ PASS - Unit, integration, E2E, contract tests
- **Testcontainers for MongoDB**: ✅ PASS - Real database for integration tests
- **RestAssured for API Tests**: ✅ PASS - Aligns with constitution testing stack
- **E2E for Connector Workflows**: ✅ PASS - Validates complete user journeys

### Post-Design Constitution Check Result

**STATUS**: ✅ **PASS** - All design decisions comply with constitution requirements. No violations identified.

**Design Quality Assessment**:
- Architecture: Clean hexagonal separation maintained
- Extensibility: Strategy pattern enables future connector types without runtime changes
- Performance: WebClient and embedded documents support <500ms execution threshold
- Observability: Comprehensive audit trail and logging
- Testing: Multi-layer approach ensures quality and prevents regressions

**Ready for Phase 2**: Proceed to `/speckit.tasks` to generate implementation tasks.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
