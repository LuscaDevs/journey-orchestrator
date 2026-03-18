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
