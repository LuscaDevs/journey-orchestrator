# Research: Incremental Graph Evolution Refactor

**Feature**: 008-graph-evolution-refactor  
**Date**: 2025-04-10  
**Status**: Complete

## Overview

This document consolidates research findings and technical decisions for the incremental graph evolution refactor. All technical context was available from the project constitution and feature specification, so no external research was required.

## Technical Decisions

### UUID Format for State Identifiers

**Decision**: Use UUID v4 format for state identifiers

**Rationale**: 
- UUID v4 is the standard format specified in the project assumptions
- Provides globally unique identifiers without requiring coordination
- Java's built-in `java.util.UUID` class provides excellent support
- Compatible with MongoDB's ObjectId and standard UUID storage

**Alternatives Considered**:
- UUID v7 (time-ordered): Not selected to maintain consistency with existing assumptions
- Custom string identifiers: Rejected due to collision risk and complexity
- MongoDB ObjectId: Rejected to maintain API-agnostic identifier format

### Dual Reference Resolution Strategy

**Decision**: Prioritize ID-based references when both ID and name are provided, normalize internally to use IDs

**Rationale**:
- Aligns with FR-024 (prioritize state resolution by id)
- Enables gradual migration from name-based to ID-based references
- Simplifies internal logic by using single source of truth (ID)
- Maintains backward compatibility by accepting both reference types

**Alternatives Considered**:
- Name-based only: Rejected, doesn't support visual editor integration
- ID-based only: Rejected, breaks backward compatibility
- Parallel resolution paths: Rejected, adds complexity without benefit

### Position Data Storage

**Decision**: Store position data as optional field `{ x: number, y: number }` on State entity

**Rationale**:
- Aligns with FR-020 through FR-023
- Simple structure compatible with React Flow editor
- Optional field ensures no impact on execution logic
- Stored alongside State for single-query retrieval

**Alternatives Considered**:
- Separate Position entity: Rejected, adds unnecessary complexity
- JSON blob storage: Rejected, loses type safety and queryability
- No position support: Rejected, required for visual editor integration

### OpenAPI Evolution Approach

**Decision**: Add optional fields to existing schemas without removing or renaming existing fields

**Rationale**:
- Ensures 100% backward compatibility (FR-007, SC-001)
- Follows specification-driven development principle
- Generated code will include new fields as optional
- Existing clients continue to work without modification

**Alternatives Considered**:
- New version of schemas: Rejected, violates no API versioning constraint
- Replace fields: Rejected, breaks backward compatibility
- Separate endpoint: Rejected, adds unnecessary complexity

## Technology Choices

### UUID Generation

**Library**: Java's built-in `java.util.UUID`

**Rationale**:
- No additional dependencies required
- Thread-safe and performant
- Standard library with proven reliability
- Compatible with Lombok annotations

### State Reference Implementation

**Pattern**: Value object (StateReference) with flexible resolution

**Rationale**:
- Aligns with DDD principles (value objects for concepts without identity)
- Encapsulates dual reference logic
- Testable in isolation
- Follows existing domain modeling patterns

### Validation Strategy

**Approach**: Domain-level validation with early rejection

**Rationale**:
- Aligns with FR-006 (validate before persisting)
- Prevents data corruption (FR-010)
- Domain layer purity (no framework dependencies)
- Consistent with existing validation patterns

## Performance Considerations

### State Reference Resolution

**Target**: Sub-millisecond performance (SC-005)

**Strategy**:
- Build in-memory index of states by ID and name during validation
- Single-pass validation for all transitions
- Cache resolved references during journey definition processing
- Avoid repeated database lookups

### UUID Generation

**Target**: <10% performance impact (SC-004)

**Strategy**:
- UUID.randomUUID() is highly optimized in Java
- Generate IDs during domain object construction
- No additional I/O or external calls
- Minimal overhead compared to existing operations

## Migration Strategy

**Database Migration**: Not required

**Rationale**:
- Database is currently empty (assumption)
- No legacy data to migrate
- New fields are optional, existing documents remain valid
- MongoDB schema is flexible, no schema migration needed

**Future Considerations**:
- If database had data, would implement migration script to generate IDs for existing states
- Would use batch processing for large datasets
- Would maintain rollback capability

## Testing Strategy

### Unit Tests

**Focus**: Domain entity behavior and reference resolution logic

**Coverage Target**: 90%+ for dual reference resolution logic (SC-003)

**Key Test Scenarios**:
- UUID generation and immutability
- State reference resolution by ID
- State reference resolution by name
- Mixed reference patterns
- Conflict detection (ID vs name mismatch)
- Validation of invalid references

### Integration Tests

**Focus**: Application service behavior and MongoDB persistence

**Key Test Scenarios**:
- Journey definition creation with auto-generated IDs
- Journey definition creation with client-provided IDs
- Position data persistence and retrieval
- Dual reference resolution in service layer
- Validation error handling

### E2E Tests

**Focus**: Complete API workflows with both reference patterns

**Key Test Scenarios**:
- Legacy compatibility (name-based references only)
- New pattern (ID-based references only)
- Mixed patterns (both reference types)
- Position data persistence through API
- Performance validation (response times)

## OpenAPI Code Generation

**Tool**: OpenAPI Generator Maven Plugin (existing)

**Configuration**:
- Update api-spec/openapi.yaml with new optional fields
- Run `mvn generate-sources` to regenerate code
- Verify generated classes include new fields
- Update mappers if needed for new fields

**Validation**:
- Compare generated classes before/after
- Ensure no breaking changes in generated code
- Verify optional fields are properly annotated
- Test code generation workflow end-to-end

## Risk Mitigation

### Risk: Breaking Existing API Clients

**Mitigation**:
- All new fields are optional in OpenAPI spec
- Existing fields are not removed or renamed
- Comprehensive E2E tests for legacy compatibility
- OpenAPI contract validation before merge

### Risk: Performance Degradation

**Mitigation**:
- Performance targets defined in success criteria
- Benchmark current baseline before implementation
- Profile reference resolution logic
- Optimize if targets not met

### Risk: Data Inconsistency

**Mitigation**:
- Domain-level validation before persistence
- Immutable IDs once created
- Conflict detection for ID/name mismatches
- Comprehensive test coverage for edge cases

## Conclusion

All technical decisions align with the project constitution, feature specification, and architectural principles. No external research was required as all necessary information was available from the project documentation. The refactor is designed to be incremental, backward-compatible, and low-risk.
