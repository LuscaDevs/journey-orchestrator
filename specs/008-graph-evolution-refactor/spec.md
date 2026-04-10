# Feature Specification: Incremental Graph Evolution Refactor

**Feature Branch**: `008-graph-evolution-refactor`
**Created**: 2025-04-10
**Status**: Draft
**Input**: User description: "Journey Orchestrator Backend Refactor (Incremental Graph Evolution) - Quero refatorar o backend existente do Journey Orchestrator de forma incremental e sem breaking changes, mantendo total compatibilidade com a API atual e banco vazio (sem dados legados relevantes). O objetivo é evoluir o modelo atual de State e Transition para suportar uma estrutura mais próxima de um graph model, preparando o sistema para integração com editor visual (React Flow), sem reescrever o sistema."

## Clarifications

### Session 2025-04-10

- Q: How should the refactor handle the existing OpenAPI-driven code generation? → A: The openapi.yaml file must be updated as part of the refactor to ensure the generated classes reflect the new model (with State IDs and dual reference support for Transitions).

## Out of Scope

This refactor does NOT include:
- Implementation of a full graph engine or graph database migration
- Changes to the journey execution engine or state machine logic
- Migration of existing data (database is empty, no legacy data to migrate)
- API versioning changes or new endpoints (only internal model evolution)
- Changes to the visual editor (React Flow) integration - this prepares the backend for future integration but does not implement it
- Performance optimizations beyond the baseline requirements specified in success criteria
- Changes to authentication, authorization, or security mechanisms

## Assumptions

- Database is currently empty with no legacy data requiring migration
- MongoDB is the primary persistence layer and will continue to be used
- Existing OpenAPI code generation workflow is already established and functional
- Current API clients are using name-based state references exclusively
- The system uses UUID v4 format for unique identifiers
- State names are currently unique within a journey definition (enforced by existing validation)
- No concurrent editing of journey definitions occurs (or is handled by existing mechanisms)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Backward-Compatible State Identity (Priority: P1)

Developers creating journey definitions through the API can continue using the existing name-based approach while the system automatically generates and manages unique identifiers for states, enabling future graph-based features without disrupting current workflows.

**Why this priority**: This is the foundation for all subsequent enhancements. Without backward-compatible state identity, no other graph evolution features can be safely implemented. It ensures zero disruption to production systems while enabling future capabilities.

**Independent Test**: Can be fully tested by creating journey definitions via the existing API endpoints and verifying that states receive unique identifiers while name-based transitions continue to function correctly. Delivers value by maintaining system stability while adding identity capability.

**Acceptance Scenarios**:

1. **Given** a developer creates a journey definition with states using only names, **When** the definition is saved, **Then** each state automatically receives a unique identifier while the name field remains populated and functional
2. **Given** an existing journey definition created before the refactor, **When** the definition is retrieved, **Then** all states have both name and identifier fields populated
3. **Given** a journey definition with duplicate state names, **When** the definition is created, **Then** the system rejects the request with a clear validation error

---

### User Story 2 - Dual Reference Transition Support (Priority: P1)

Developers can define transitions that reference states either by name (legacy approach) or by identifier (new approach), with the system resolving both reference types correctly, enabling gradual migration to identifier-based references.

**Why this priority**: This enables the incremental migration path from name-based to identifier-based references. It's critical for allowing visual editors to use identifiers while maintaining compatibility with existing name-based definitions.

**Independent Test**: Can be fully tested by creating journey definitions with transitions using name references, identifier references, and mixed references, then validating that all transitions resolve correctly to their target states. Delivers value by enabling coexistence of legacy and new reference patterns.

**Acceptance Scenarios**:

1. **Given** a journey definition with transitions referencing states by name, **When** the definition is processed, **Then** all transitions resolve correctly to their target states
2. **Given** a journey definition with transitions referencing states by identifier, **When** the definition is processed, **Then** all transitions resolve correctly to their target states
3. **Given** a journey definition with mixed name and identifier references, **When** the definition is processed, **Then** all transitions resolve correctly regardless of reference type
4. **Given** a transition references a non-existent state name, **When** the definition is created, **Then** the system returns a validation error indicating the missing state

---

### User Story 3 - Consistency Validation (Priority: P2)

The system automatically validates that state names and identifiers remain consistent throughout the journey definition lifecycle, preventing data corruption and ensuring reliable state resolution.

**Why this priority**: Data integrity is essential for system reliability. Inconsistent state references could cause journey execution failures. This validation prevents data corruption scenarios that could impact production systems.

**Independent Test**: Can be fully tested by attempting to create journey definitions with inconsistent state references (mismatched names/identifiers) and verifying that validation errors prevent invalid data from being persisted. Delivers value by ensuring data integrity and preventing runtime errors.

**Acceptance Scenarios**:

1. **Given** a journey definition with a transition referencing a state identifier that doesn't match any state, **When** the definition is created, **Then** the system rejects the request with a validation error
2. **Given** a journey definition is being updated, **When** a state name is changed, **Then** the system validates that all transitions referencing that state are updated or an error is returned
3. **Given** a journey definition with circular state references, **When** the definition is created, **Then** the system either accepts it (if valid for the domain) or returns a clear validation error

---

### User Story 4 - Legacy API Compatibility (Priority: P1)

Existing API clients continue to function without any changes, with all endpoints maintaining their current request/response contracts while the backend internally handles the new identifier-based model.

**Why this priority**: This is a critical requirement since the API is in production. Any breaking change would cause immediate disruption to production systems. This ensures zero impact on existing integrations.

**Independent Test**: Can be fully tested by running the existing API contract test suite and verifying that all tests pass without modification. Delivers value by guaranteeing production stability during the refactor.

**Acceptance Scenarios**:

1. **Given** an existing API client creates a journey definition using the old contract, **When** the request is sent, **Then** the request succeeds and returns the expected response format
2. **Given** an existing API client retrieves a journey definition, **When** the response is received, **Then** the response format matches the existing contract (with new identifier fields as additional optional fields)
3. **Given** an existing API client applies events to journey instances, **When** the request is sent, **Then** the request succeeds and the instance transitions correctly
4. **Given** the OpenAPI specification, **When** compared to the actual API behavior, **Then** all existing endpoints remain compliant

---

### Edge Cases

- What happens when a journey definition is created with states that have identical names but different identifiers?
- How does the system handle transitions that reference both name and identifier for the same state with conflicting values?
- What happens when a state name is changed after transitions have been created using name references?
- How does the system handle orphaned transitions (referencing deleted states) during definition updates?
- What happens when the system receives a journey definition with identifier fields that don't match the server-generated format?
- How does the system handle concurrent updates to the same journey definition with different reference patterns?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST automatically generate a unique identifier for each State when a JourneyDefinition is created
- **FR-002**: System MUST preserve the existing name field on State without any changes to its behavior
- **FR-003**: System MUST support transitions that reference states by name (legacy pattern)
- **FR-004**: System MUST support transitions that reference states by identifier (new pattern)
- **FR-005**: System MUST resolve state references correctly regardless of whether they use name or identifier
- **FR-006**: System MUST validate that all transition references resolve to valid states before persisting a JourneyDefinition
- **FR-007**: System MUST maintain full backward compatibility with all existing API endpoints
- **FR-008**: System MUST include identifier fields in API responses as optional fields (not breaking existing clients)
- **FR-009**: System MUST reject journey definitions with duplicate state names
- **FR-010**: System MUST ensure consistency between State.name and State.id throughout the definition lifecycle
- **FR-011**: System MUST support mixed reference patterns (some transitions by name, some by identifier) within the same JourneyDefinition
- **FR-012**: System MUST generate identifiers using a standard format (UUID) that is globally unique
- **FR-013**: System MUST allow identifier fields to be provided by clients (for visual editor integration) while also supporting auto-generation
- **FR-014**: System MUST validate that provided identifiers match the expected format when supplied by clients
- **FR-015**: System MUST maintain identifier immutability once a State is created (identifiers cannot be changed)
- **FR-016**: System MUST update the OpenAPI specification (api-spec/openapi.yaml) to include the new identifier fields in State and Transition schemas as optional fields
- **FR-017**: System MUST ensure that code generation from the updated OpenAPI specification produces classes that reflect the new model (with State IDs and dual reference support)
- **FR-018**: System MUST update all existing test cases to cover both legacy (name-based) and new (identifier-based) reference patterns
- **FR-019**: System MUST ensure that all E2E tests pass with both reference patterns
- **FR-020**: System MUST support optional position data (x, y) for each State to enable visual editor rendering
- **FR-021**: System MUST persist position data when provided by clients
- **FR-022**: System MUST return position data in API responses when available
- **FR-023**: System MUST treat position data as optional and not required for execution logic
- **FR-024**: System MUST prioritize state resolution by id when both id and name are provided in a transition
- **FR-025**: System MUST normalize transitions internally to use state identifiers

### Key Entities

### Key Entities

- **State**: Represents a node in the journey graph with identity, containing:
  - id (UUID) → unique identifier (new)
  - name → human-readable identifier (existing)
  - type → INITIAL | INTERMEDIATE | FINAL
  - position → { x: number, y: number } (optional, for visual editor)

- **Transition**: Represents a directed edge between states, containing:
  - source → state reference (by id or name)
  - target → state reference (by id or name)
  - event → trigger name
  - condition → optional expression

- **JourneyDefinition**: Container for the complete journey graph, holding:
  - metadata (id, journeyCode, version, status)
  - states (nodes)
  - transitions (edges)

- **StateReference**: A flexible reference supporting:
  - id (preferred)
  - name (legacy compatibility)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of existing API endpoints continue to function without any changes to client code
- **SC-002**: All existing E2E tests pass without modification
- **SC-003**: New test coverage achieves 90%+ for the dual reference resolution logic
- **SC-004**: Journey definition creation time increases by less than 10% compared to baseline (due to identifier generation)
- **SC-005**: State reference resolution maintains sub-millisecond performance for both name and identifier lookups
- **SC-006**: Zero breaking changes detected in OpenAPI specification comparison
- **SC-007**: OpenAPI specification (api-spec/openapi.yaml) successfully updated to include optional identifier fields in State and Transition schemas
- **SC-008**: Code generation from updated OpenAPI specification produces classes that correctly reflect the new model with State IDs and dual reference support
- **SC-009**: 100% of journey definitions can be created using either name-based or identifier-based transitions
- **SC-010**: Validation errors for invalid state references are returned within 100ms
- **SC-011**: All state identifiers are globally unique (UUID collision rate of 0%)
- **SC-012**: Mixed reference patterns (name and identifier in same definition) resolve correctly in 100% of test cases
- **SC-013**: Frontend (React Flow editor) can persist and reload node positions without data loss
- **SC-014**: All transitions resolve correctly using id-based references in 100% of cases
