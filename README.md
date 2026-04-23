# Journey Orchestrator

A platform service responsible for executing configurable product journeys using a state machine model. Journey Orchestrator provides a business-agnostic orchestration engine that manages journey definitions, state transitions, and runtime instances.

The service enables teams to design and execute complex workflows in a declarative and scalable way, without embedding business rules inside the orchestration layer.

## 🏗️ Architecture

This project follows modern enterprise architecture principles:

- **Specification-Driven Development (Spec First)**: API contracts defined in OpenAPI before implementation
- **Clean Architecture**: Hexagonal architecture with clear separation of concerns
- **Domain-Driven Design (DDD)**: Domain-centric modeling with rich domain objects
- **SOLID Principles**: Single responsibility, open/closed, Liskov substitution, interface segregation, dependency inversion
- **Business-Agnostic Orchestration**: No business rules embedded in the orchestrator layer

### Layered Architecture

```
journey-orchestrator
├── api-spec/                 # OpenAPI specification (single source of truth)
├── src/main/java/com/luscadevs/journeyorchestrator/
│   ├── api/                  # API layer (controllers, DTOs, mappers)
│   ├── application/          # Application services and use cases
│   │   ├── engine/          # State machine engine
│   │   ├── port/            # Input/Output ports (interfaces)
│   │   └── service/         # Application services
│   ├── domain/              # Core domain logic
│   │   ├── engine/          # Domain engine interfaces
│   │   ├── journey/         # Journey definition domain
│   │   └── journeyinstance/ # Journey instance domain
│   └── adapters/            # Infrastructure adapters
│       ├── in/web/          # REST controllers
│       ├── out/memory/      # In-memory repositories
│       ├── out/persistence/mongo/  # MongoDB persistence
│       └── observability/   # Logging and monitoring
├── src/test/java/           # Unit and integration tests
├── specs/                   # Feature specifications (spec-driven development)
├── docs/                    # Architecture documentation
└── scripts/                 # Utility scripts
```

## 🚀 Features Implemented

### Feature 001: Standardized Error Handling
- RFC 9457 ProblemDetail compliant error responses
- Centralized exception handling with `@RestControllerAdvice`
- Domain-specific exceptions mapped to HTTP status codes
- Structured error codes and messages
- Comprehensive error logging with MDC context

### Feature 002: MongoDB Persistence
- Migration from in-memory to MongoDB persistence
- Configurable MongoDB connection settings
- Environment-specific configurations
- Data consistency across multiple instances
- Automatic index creation
- Connection pooling and timeout management

### Feature 004: Transition History Tracking
- Complete audit trail of all state transitions
- Persistent history events with full context
- API endpoint for retrieving transition history
- Filtering by date range, event type, and pagination
- Immutable history records
- Chronological ordering preservation

### Feature 005: Execution Observability
- Automatic logging of controller and service method execution
- Structured JSON logging format
- MDC context propagation (correlationId, httpMethod, requestPath, errorCode)
- Execution duration measurement
- Slow operation detection and warnings
- Security-focused (no parameter logging)

### Feature 006: Conditional Transitions
- SpEL (Spring Expression Language) support for transition conditions
- Logical operators (AND, OR, NOT)
- Comparison operators (=, !=, >, <, >=, <=)
- Nested expressions with parentheses
- Runtime context evaluation
- Business-agnostic condition evaluation

### Feature 007: End-to-End Testing Framework
- Comprehensive E2E testing with RestAssured and Testcontainers
- MongoDB Testcontainers integration for isolated test environments
- Test fixtures and utilities for journey definitions and events
- Performance testing and benchmarking
- API contract validation against OpenAPI specification
- Load testing with concurrent request handling
- JaCoCo code coverage reporting (34% overall coverage)

### Feature 008: Graph Evolution Refactor
- Incremental refactor to support graph-based state model
- State identity with unique UUID identifiers
- Dual reference support (name-based and ID-based transitions)
- Backward-compatible API (no breaking changes)
- Optional position data (x, y) for visual editor integration
- State reference resolution with priority to ID over name
- OpenAPI specification updated with new identifier fields

## 📋 Core Concepts

### Journey Definition
Represents the structure of a workflow, including:
- States (INITIAL, INTERMEDIATE, FINAL)
- Transitions with optional conditions
- Versioning support
- Status management (ATIVA, INATIVA, RASCUNHO)
- Unique identifiers for states (UUID v4)
- Position data for visual editor rendering

### Journey Instance
Represents the execution of a journey for a specific context:
- Current state tracking
- Status (RUNNING, COMPLETED, FAILED, CANCELLED)
- Context data storage
- Transition history linkage

### Event
An external signal sent to the orchestrator to trigger a state transition:
- Event type identification
- Optional payload data
- Context-aware evaluation

### State Machine
The underlying mechanism responsible for:
- Controlling transitions between states
- Evaluating transition conditions
- Maintaining state consistency
- Recording transition history

## 🛠️ Technology Stack

### Core Framework
- **Java 21**: Latest LTS version with modern language features
- **Spring Boot 4.0.3**: Main framework with auto-configuration
- **Spring Expression Language (SpEL)**: For conditional transition evaluation
- **Spring Data MongoDB**: MongoDB integration
- **Spring Validation**: Input validation
- **Spring Actuator**: Application monitoring and management

### Persistence
- **MongoDB**: Primary persistence layer for journey data
- **Spring Data MongoDB**: MongoDB repository abstraction

### API & Documentation
- **OpenAPI 3.0.3**: API specification and code generation
- **SpringDoc OpenAPI**: API documentation with Swagger UI
- **OpenAPI Generator Maven Plugin**: Automatic code generation from OpenAPI spec

### Testing
- **JUnit 5**: Unit and integration testing framework
- **Testcontainers**: Integration testing with real MongoDB containers
- **RestAssured**: REST API testing
- **AssertJ**: Fluent assertion library
- **Awaitility**: Asynchronous testing support
- **JaCoCo**: Code coverage reporting

### Utilities
- **Lombok**: Boilerplate code reduction
- **Jackson**: JSON serialization/deserialization
- **AspectJ**: AOP support for observability
- **Logback JSON**: Structured JSON logging

## 📦 Installation & Setup

### Prerequisites
- Java 21 or higher
- Maven 3.8+
- MongoDB 4.4+ (running on localhost:27017 or configured URI)

### Build the Project

```bash
# Clone the repository
git clone <repository-url>
cd journey-orchestrator

# Build with Maven
./mvnw clean install

# Or on Windows
mvnw.cmd clean install
```

### Run the Application

```bash
# Using Maven
./mvnw spring-boot:run

# Or using the JAR file
java -jar target/journeyorchestrator-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### Access API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **Actuator Health**: http://localhost:8080/actuator/health

## 🔧 Configuration

### Application Configuration

Configuration is managed through `src/main/resources/application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/journey-orchestrator
      database: journey-orchestrator
      auto-index-creation: true

observability:
  logging:
    enabled: true
    slow-operation-threshold: 1000  # milliseconds
    log-parameters: false  # Security: never log parameters
```

### MongoDB Configuration

```yaml
journey-orchestrator:
  persistence:
    mongodb:
      connection-timeout: 5000
      max-pool-size: 20
      min-pool-size: 5
      uri: mongodb://localhost:27017/journey-orchestrator
```

## 📚 API Endpoints

### Journey Definitions
- `POST /journeys` - Create a new journey definition
- `GET /journeys` - List all journey definitions
- `GET /journeys/{journeyCode}` - Get all versions of a journey definition
- `PUT /journeys/{id}` - Update a journey definition
- `PATCH /journeys/{id}` - Update journey definition status
- `DELETE /journeys/{id}` - Delete a journey definition

### Journey Instances
- `GET /journey-instances` - List all journey instances
- `POST /journey-instances` - Start a new journey instance
- `GET /journey-instances/{instanceId}` - Get journey instance by id
- `POST /journey-instances/{instanceId}/events` - Send event to a journey instance

### Transition History
- `GET /journey-instances/{instanceId}/history` - Get transition history with filtering
  - Query parameters: `from`, `to`, `eventType`, `limit`, `offset`

## 🧪 Testing

### Run All Tests

```bash
# Run unit tests
./mvnw test

# Run integration tests
./mvnw verify

# Run specific test class
./mvnw test -Dtest=JourneyDefinitionServiceTest
```

### Run E2E Tests

```bash
# On Linux/macOS
./scripts/e2e/run-e2e-tests.sh

# On Windows (PowerShell)
./scripts/e2e/run-e2e-tests.ps1
```

### Generate Coverage Report

```bash
# Generate JaCoCo coverage report
./mvnw jacoco:report

# View HTML report
open target/site/jacoco/index.html
```

### Testcontainers Setup

The project uses Testcontainers for integration testing with MongoDB. See `docs/testcontainers-setup.md` for detailed setup instructions.

## 📖 Postman Collection

A comprehensive Postman collection is available for testing all API endpoints:

- **Collection File**: `postman-collection.json`
- **Environment File**: `postman-environment.json`
- **Documentation**: `POSTMAN-README.md`

Import the collection into Postman to test:
- Journey definition CRUD operations
- Journey instance lifecycle
- Event-driven state transitions
- Transition history with filters
- Complete workflow examples

## 📄 Documentation

### Architecture Documentation
- `docs/adr/0001-architecture-principles.md` - Architecture Decision Records
- `docs/domain-model.md` - Domain model documentation
- `docs/journey-dsl.md` - Journey Definition DSL documentation
- `docs/observability.md` - Execution observability documentation
- `docs/testcontainers-setup.md` - Testcontainers setup guide

### Feature Specifications
All features follow spec-driven development. Specifications are located in `specs/`:
- `001-error-handling/` - Standardized error handling mechanism
- `002-mongodb-persistence/` - MongoDB persistence migration
- `004-transition-history/` - Transition history tracking
- `005-execution-observability/` - Execution observability
- `006-conditional-transitions/` - Conditional transitions
- `007-e2e-journey-tests/` - End-to-end testing framework
- `008-graph-evolution-refactor/` - Graph evolution refactor

Each spec directory contains:
- `spec.md` - Feature specification
- `plan.md` - Implementation plan
- `tasks.md` - Actionable tasks
- `research.md` - Research findings
- `quickstart.md` - Quick start guide

## 🎯 Development Workflow

### Contract-First Development

1. **Define API Contract**: Update `api-spec/openapi.yaml`
2. **Generate Code**: Run Maven generate-sources to create API stubs
3. **Implement Domain**: Create domain entities and business logic
4. **Implement Application**: Create services and use cases
5. **Implement Adapters**: Create controllers and repositories
6. **Test**: Write unit and integration tests
7. **Validate**: Run contract tests to ensure API compliance

### Code Generation

```bash
# Generate API stubs from OpenAPI specification
./mvnw generate-sources
```

Generated code is located in `target/generated-sources/openapi/`

## 🔍 Monitoring & Observability

### Structured Logging

All logs are output in structured JSON format with:
- Correlation IDs for request tracing
- Execution duration measurement
- Component identification (controller/service)
- MDC context propagation

### Performance Monitoring

- Slow operation detection (configurable threshold)
- Execution duration tracking
- Component-level performance metrics

### Actuator Endpoints

- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

## 🤝 Contributing

This project follows specification-driven development. All features must:

1. Start with a specification in `specs/`
2. Follow the hexagonal architecture principles
3. Maintain backward compatibility
4. Include comprehensive tests
5. Update documentation

See the project constitution in memory for detailed guidelines.

## 📝 License

This project is licensed under the terms specified in the LICENSE file.

## 🙏 Acknowledgments

Built with modern enterprise architecture principles and best practices for distributed systems.
