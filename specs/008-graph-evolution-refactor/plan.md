# Implementation Plan: Incremental Graph Evolution Refactor

**Branch**: `008-graph-evolution-refactor` | **Date**: 2025-04-10 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/008-graph-evolution-refactor/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

This feature incrementally evolves the Journey Orchestrator backend model to support graph-based structures by adding unique identifiers (UUID) to States and dual reference support (name + id) for Transitions. The refactor maintains 100% backward compatibility with existing API contracts while preparing the system for future visual editor integration. The approach follows specification-driven development by updating the OpenAPI specification first, then regenerating code stubs, followed by domain and application layer updates.

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 4.0.3, MongoDB, OpenAPI 3.0.3, Lombok, Maven, RestAssured 5.4.0, Testcontainers 1.19.7, JUnit 5
**Storage**: MongoDB (primary persistence layer)
**Testing**: JUnit 5, RestAssured (E2E), Testcontainers (integration)
**Target Platform**: Server (Linux)
**Project Type**: Web service (REST API)
**Performance Goals**: <10% performance impact on journey definition creation, sub-millisecond state reference resolution, <100ms validation response time
**Constraints**: Zero breaking changes to API contracts, 100% backward compatibility, database is empty (no migration needed)
**Scale/Scope**: Backend refactor for existing production system, affecting State and Transition domain entities

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Pre-Design Evaluation

| Principle | Status | Notes |
|-----------|--------|-------|
| Specification-Driven Development | ✓ PASS | OpenAPI specification update is FR-016, must be done first |
| Clean Architecture | ✓ PASS | Hexagonal architecture maintained, no layer violations |
| Domain-Driven Design | ✓ PASS | Domain-centric modeling with State/Transition entities |
| SOLID Principles | ✓ PASS | Interface segregation maintained with ports |
| Business-Agnostic Orchestration | ✓ PASS | No business rules in orchestrator layer |
| No Business Logic in Adapters | ✓ PASS | Infrastructure concerns separated |
| Domain Layer Purity | ✓ PASS | No framework dependencies in domain |
| Interface Segregation | ✓ PASS | Ports for input/output boundaries |
| Event-Driven Transitions | ✓ PASS | Existing pattern maintained |
| Audit Trail | ✓ PASS | Existing audit trail not modified |
| Versioned Definitions | ✓ PASS | Existing versioning not modified |
| E2E Test Compliance | ✓ PASS | SC-002 requires all existing E2E tests pass |
| E2E Regression Prevention | ✓ PASS | SC-002 requires no modification to existing tests |
| Performance Gate | ✓ PASS | SC-004, SC-005, SC-010 define performance thresholds |
| Coverage Gate | ✓ PASS | SC-003 requires 90%+ coverage for resolution logic |

**Result**: ✓ ALL GATES PASSED - Proceed to Phase 0

### Post-Design Evaluation

| Principle | Status | Notes |
|-----------|--------|-------|
| Specification-Driven Development | ✓ PASS | OpenAPI evolution documented in contracts/openapi-evolution.md |
| Clean Architecture | ✓ PASS | Hexagonal architecture maintained, StateReference value object added |
| Domain-Driven Design | ✓ PASS | Domain entities updated with id and position fields |
| SOLID Principles | ✓ PASS | Interface segregation maintained, single responsibility |
| Business-Agnostic Orchestration | ✓ PASS | No business rules in orchestrator layer |
| No Business Logic in Adapters | ✓ PASS | Infrastructure concerns separated, mappers updated |
| Domain Layer Purity | ✓ PASS | No framework dependencies in domain, value objects added |
| Interface Segregation | ✓ PASS | Ports unchanged, new value objects for references |
| Event-Driven Transitions | ✓ PASS | Existing pattern maintained |
| Audit Trail | ✓ PASS | Existing audit trail not modified |
| Versioned Definitions | ✓ PASS | Existing versioning not modified |
| E2E Test Compliance | ✓ PASS | E2E tests documented in quickstart.md |
| E2E Regression Prevention | ✓ PASS | Legacy compatibility E2E test included |
| Performance Gate | ✓ PASS | Performance targets defined and validated in quickstart |
| Coverage Gate | ✓ PASS | 90%+ coverage requirement documented in quickstart |

**Result**: ✓ ALL GATES PASSED - Design approved

## Project Structure

### Documentation (this feature)

```text
specs/008-graph-evolution-refactor/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── openapi-evolution.md  # OpenAPI specification changes
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/luscadevs/journeyorchestrator/
├── api/                                         # API layer (controllers, DTOs, mappers)
│   ├── model/                                   # Generated from OpenAPI
│   │   ├── State.java                          # Will have new id field
│   │   ├── Transition.java                     # Will have new id-based reference fields
│   │   └── JourneyDefinition.java              # Will include position data
│   └── mapper/
│       └── JourneyDefinitionMapper.java         # Updated for new fields
├── application/                                 # Application services and use cases
│   ├── service/
│   │   └── JourneyDefinitionService.java       # Updated for dual reference resolution
│   └── port/
│       └── JourneyDefinitionRepositoryPort.java # Interface unchanged
├── domain/                                     # Core domain logic
│   ├── journey/
│   │   ├── State.java                          # Add id field (UUID), position field
│   │   ├── Transition.java                     # Add id-based reference support
│   │   ├── JourneyDefinition.java              # Add position support
│   │   └── StateReference.java                 # New value object for dual reference
│   └── journeyinstance/                        # Unchanged
└── adapters/                                   # Infrastructure adapters
    ├── in/web/                                 # REST controllers
    │   └── JourneyDefinitionController.java    # Updated for new fields
    └── out/persistence/mongo/                   # MongoDB persistence
        ├── StateDocument.java                  # Updated MongoDB document
        ├── TransitionDocument.java             # Updated MongoDB document
        └── JourneyDefinitionDocument.java      # Updated MongoDB document

api-spec/
└── openapi.yaml                                 # Updated with new optional fields

src/test/java/com/luscadevs/journeyorchestrator/
├── unit/
│   └── journey/
│       ├── StateTest.java                      # Updated for id field
│       ├── TransitionTest.java                 # Updated for dual reference
│       └── StateReferenceTest.java              # New test class
├── integration/
│   └── JourneyDefinitionServiceTest.java        # Updated for dual reference
└── e2e/
    ├── DualReferenceE2ETest.java                # New E2E test for mixed references
    ├── LegacyCompatibilityE2ETest.java         # New E2E test for backward compatibility
    └── PositionDataE2ETest.java                 # New E2E test for position persistence
```

**Structure Decision**: Single project structure (Option 1) - This is a backend refactor of an existing Spring Boot application. The hexagonal architecture is maintained with clear separation between domain, application, and adapter layers. No new projects or modules are created.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No constitution violations detected. Complexity tracking not required.
