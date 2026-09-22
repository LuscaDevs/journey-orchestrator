# journey-orchestrator Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-09-10

## Active Technologies
- Java 21 (from constitution) + Spring Boot 4.0.3, Spring Data MongoDB, MongoDB Java Driver (002-mongodb-persistence)
- MongoDB (replacing in-memory storage) (002-mongodb-persistence)
- Java 21 + Spring Boot 4.0.3, MongoDB, OpenAPI 3.0.3, Lombok (004-transition-history)
- MongoDB (primary persistence layer) (004-transition-history)
- Java 21 + Spring Boot 4.0.3, SLF4J, Logback, Spring AOP (005-execution-observability)
- MongoDB (existing) (005-execution-observability)
- Java 21 (LTS) + Spring Boot 4.0.3, MongoDB, Lombok, OpenAPI 3.0.3 (006-conditional-transitions)
- MongoDB for journey definitions and instances (006-conditional-transitions)
- Java 21 (LTS) + RestAssured, Testcontainers, JUnit 5, Spring Boot Test, MongoDB Testcontainers (007-e2e-journey-tests)
- MongoDB (via Testcontainers for testing) (007-e2e-journey-tests)
- Java 21 (LTS) + Spring Boot 4.0.3, MongoDB, OpenAPI 3.0.3, Lombok, Maven, RestAssured 5.4.0, Testcontainers 1.19.7, JUnit 5 (008-graph-evolution-refactor)
- Java 21 + Spring Boot 4.0.3, Spring Web, MongoDB Driver, Lombok, Jackson (JSON processing) (009-http-connector)
- MongoDB (existing for journey data, will extend for connector execution records) (009-http-connector)
- TypeScript 5.x / React 19 + React, Vite, Zustand, Tailwind CSS, Radix UI, ReactFlow (010-http-connector-config)
- MongoDB (via Journey Orchestrator backend API) (010-http-connector-config)

- Java 21 (LTS) + Spring Boot 4.0.3, Spring Web, Spring Validation, Lombok (001-error-handling)

## Project Structure

```text
backend/
frontend/
tests/
```

## Commands

# Add commands for Java 21 (LTS)

## Code Style

Java 21 (LTS): Follow standard conventions

## Recent Changes
- 010-http-connector-config: Added TypeScript 5.x / React 19 + React, Vite, Zustand, Tailwind CSS, Radix UI, ReactFlow
- 009-http-connector: Added Java 21 + Spring Boot 4.0.3, Spring Web, MongoDB Driver, Lombok, Jackson (JSON processing)
- 008-graph-evolution-refactor: Added Java 21 (LTS) + Spring Boot 4.0.3, MongoDB, OpenAPI 3.0.3, Lombok, Maven, RestAssured 5.4.0, Testcontainers 1.19.7, JUnit 5


<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
