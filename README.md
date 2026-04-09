# Journey Orchestrator

Journey Orchestrator is a platform service responsible for executing configurable product journeys using a state machine model.
It provides a business-agnostic orchestration engine that manages journey definitions, state transitions, and runtime instances.

The service enables teams to design and execute complex workflows in a declarative and scalable way, without embedding business rules inside the orchestration layer.

---

# Architecture Principles

This project follows modern enterprise architecture principles:

- **Specification-Driven Development (Spec First)**
- **SOLID principles**
- **Clean Architecture**
- **Hexagonal Architecture**
- **Domain Driven Design (DDD) inspired modeling**

The API contract is defined using an OpenAPI specification and acts as the single source of truth for the service interface.

---

# Core Concepts

### Journey Definition

Represents the structure of a workflow, including its states and transitions.

### Journey Instance

Represents the execution of a journey for a specific context.

### Event

An external signal sent to the orchestrator to trigger a state transition.

### State Machine

The underlying mechanism responsible for controlling transitions between states.

---

# Project Structure

The project is organized using a layered architecture:

```
journey-orchestrator
├── api-spec
│   └── openapi.yaml
│
├── src
│   └── main/java
│       ├── api
│       ├── application
│       ├── domain
│       └── infrastructure
│
├── contract-tests
└── docs
```

---

# Development Approach

The development workflow follows a **contract-first approach**:

1. Define the API contract in `openapi.yaml`
2. Validate the specification
3. Generate server stubs
4. Implement application and domain logic
5. Validate implementation using contract tests

---

# Technology Stack

- Java 21
- Spring Boot
- Spring State Machine
- PostgreSQL
- OpenAPI Specification
- Docker

---

# Status

🚧 Project under active development.

---

# End-to-End (E2E) Testing & Coverage

## How to Run E2E Tests

You can run all E2E tests and generate a code coverage report using the provided scripts:

### On Linux/macOS:

```bash
./scripts/e2e/run-e2e-tests.sh
```

### On Windows (PowerShell):

```powershell
./scripts/e2e/run-e2e-tests.ps1
```

This will execute all E2E, integration, and unit tests, and generate a JaCoCo coverage report at:

- `target/site/jacoco/index.html`

## Coverage Analyzer

This project uses the **JaCoCo** Maven plugin for code coverage. There is no need for a custom CoverageAnalyzer—JaCoCo is the industry standard, fully integrated with Maven, and supports E2E, integration, and unit test coverage.

**How to generate the coverage report manually:**

```bash
mvn verify
# or
mvn test
# The report will be available at target/site/jacoco/index.html
```

**Why JaCoCo?**

- Industry standard, auditable, and fully integrated with Maven.
- Generates detailed reports (line, branch, method coverage).
- Compatible with CI/CD pipelines and IDEs.
- Avoids duplicated logic and extra maintenance.

---

# E2E Test Quickstart

See `specs/007-e2e-journey-tests/quickstart.md` for a complete guide on writing, running, and organizing E2E tests, including:

- Example test classes
- Fixture usage
- Error handling
- Performance testing
- Test data management
- Configuration and best practices
