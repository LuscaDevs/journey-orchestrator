# Data Model: Incremental Graph Evolution Refactor

**Feature**: 008-graph-evolution-refactor  
**Date**: 2025-04-10  
**Status**: Draft

## Overview

This document describes the data model changes required to support the incremental graph evolution refactor. The changes add unique identifiers (UUID) to States, dual reference support for Transitions, and optional position data for visual editor integration.

## Entity Changes

### State

**Purpose**: Represents a node in the journey graph with identity

**Changes**: Add `id` field (UUID) and `position` field (optional)

**Fields**:

| Field | Type | Optional | Description | Validation |
|-------|------|----------|-------------|------------|
| id | UUID | No | Unique identifier for the state | Auto-generated if not provided, immutable once set |
| name | String | No | Human-readable identifier | Must be unique within journey definition |
| type | Enum | No | State type: INITIAL \| INTERMEDIATE \| FINAL | Must be valid enum value |
| position | Position | Yes | Visual editor position {x, y} | Optional, not required for execution |

**Validation Rules**:
- `id` must be valid UUID v4 format
- `name` must be unique within the journey definition (FR-009)
- `id` is immutable once set (FR-015)
- `position` is optional and does not affect execution logic (FR-023)

**Relationships**:
- Referenced by Transition.source and Transition.target
- Belongs to JourneyDefinition

### Transition

**Purpose**: Represents a directed edge between states

**Changes**: Add `sourceStateId` and `targetStateId` fields (optional) for ID-based references

**Fields**:

| Field | Type | Optional | Description | Validation |
|-------|------|----------|-------------|------------|
| source | String | No | Source state reference by name (legacy) | Must resolve to valid state name |
| target | String | No | Target state reference by name (legacy) | Must resolve to valid state name |
| sourceStateId | UUID | Yes | Source state reference by ID (new) | Must resolve to valid state ID if provided |
| targetStateId | UUID | Yes | Target state reference by ID (new) | Must resolve to valid state ID if provided |
| event | String | No | Trigger event name | Must not be empty |
| condition | String | Yes | Optional condition expression | Valid if provided |

**Validation Rules**:
- At least one reference type (name or ID) must be provided for source and target
- If both name and ID are provided, ID takes precedence (FR-024)
- If ID and name refer to different states, reject request (conflict resolution)
- All references must resolve to valid states (FR-006)
- Circular references are allowed if valid for the domain

**Conflict Resolution**:
- When both `source` and `sourceStateId` are provided:
  - Use `sourceStateId` as the primary reference
  - If `sourceStateId` and `source` refer to different states → validation error
- When both `target` and `targetStateId` are provided:
  - Use `targetStateId` as the primary reference
  - If `targetStateId` and `target` refer to different states → validation error

**Relationships**:
- References State via source/sourceStateId
- References State via target/targetStateId
- Belongs to JourneyDefinition

### JourneyDefinition

**Purpose**: Container for the complete journey graph

**Changes**: Add support for position data in states (no direct field changes to JourneyDefinition itself)

**Fields**:

| Field | Type | Optional | Description |
|-------|------|----------|-------------|
| id | UUID | No | Unique identifier for the definition |
| journeyCode | String | No | Business code for the journey |
| version | Integer | No | Version number |
| status | Enum | No | Status: DRAFT \| PUBLISHED \| ARCHIVED |
| states | List<State> | No | Collection of state nodes |
| transitions | List<Transition> | No | Collection of transition edges |

**Validation Rules**:
- All state names must be unique within the definition
- All transition references must resolve to valid states
- At least one INITIAL state must exist
- At least one FINAL state must exist
- All states must be reachable from INITIAL states (graph connectivity)

**Relationships**:
- Contains multiple State entities
- Contains multiple Transition entities

### StateReference (New Value Object)

**Purpose**: Flexible reference supporting both ID and name-based resolution

**Fields**:

| Field | Type | Optional | Description |
|-------|------|----------|-------------|
| id | UUID | Yes | State identifier (preferred) |
| name | String | Yes | State name (legacy compatibility) |

**Behavior**:
- Resolves to a State entity
- Prioritizes ID when both are provided
- Used internally for normalized transition resolution
- Immutable value object

**Validation Rules**:
- At least one of `id` or `name` must be provided
- If both provided, must refer to the same state
- References must resolve to valid states

### Position (New Value Object)

**Purpose**: Visual editor position data for graph rendering

**Fields**:

| Field | Type | Optional | Description |
|-------|------|----------|-------------|
| x | Number | No | X coordinate |
| y | Number | No | Y coordinate |

**Behavior**:
- Simple coordinate object
- Used only for visual rendering
- Does not affect execution logic
- Optional on State entity

**Validation Rules**:
- x and y must be valid numbers
- No range constraints (editor-specific)

## MongoDB Document Schema

### StateDocument

```javascript
{
  _id: ObjectId,
  id: String (UUID),           // NEW - unique identifier
  name: String,                // EXISTING - human-readable name
  type: String,                // EXISTING - INITIAL|INTERMEDIATE|FINAL
  position: {                  // NEW - optional position data
    x: Number,
    y: Number
  },
  journeyDefinitionId: ObjectId // EXISTING - parent reference
}
```

**Indexes**:
- Unique index on `{ journeyDefinitionId: 1, name: 1 }` (existing)
- Index on `{ journeyDefinitionId: 1, id: 1 }` (NEW)
- Index on `{ id: 1 }` (NEW for global lookups)

### TransitionDocument

```javascript
{
  _id: ObjectId,
  source: String,              // EXISTING - source state name
  target: String,              // EXISTING - target state name
  sourceStateId: String,       // NEW - source state UUID
  targetStateId: String,       // NEW - target state UUID
  event: String,               // EXISTING - trigger event
  condition: String,           // EXISTING - optional condition
  journeyDefinitionId: ObjectId // EXISTING - parent reference
}
```

**Indexes**:
- Index on `{ journeyDefinitionId: 1 }` (existing)
- Index on `{ sourceStateId: 1 }` (NEW)
- Index on `{ targetStateId: 1 }$ (NEW)

### JourneyDefinitionDocument

```javascript
{
  _id: ObjectId,
  id: String (UUID),           // EXISTING
  journeyCode: String,         // EXISTING
  version: Integer,            // EXISTING
  status: String,              // EXISTING
  createdAt: Date,             // EXISTING
  updatedAt: Date              // EXISTING
}
```

**No changes required** - States and Transitions are stored as separate documents with parent references.

## Reference Resolution Logic

### Normalization Process

When a JourneyDefinition is created or updated:

1. **Load all states** into memory indexed by both `id` and `name`
2. **For each transition**:
   - If `sourceStateId` is provided, resolve by ID
   - Else if `source` is provided, resolve by name
   - If both provided, validate they refer to the same state
   - Repeat for target
3. **Normalize internally**: Replace all references with resolved State entities
4. **Validate**: Ensure all references resolved successfully
5. **Persist**: Store original request data (both name and ID if provided)

### Conflict Detection

**Scenario**: Both ID and name provided but refer to different states

**Detection**:
- Resolve ID → State A
- Resolve name → State B
- If State A.id != State B.id → validation error

**Error Response**:
```
{
  "error": "STATE_REFERENCE_CONFLICT",
  "message": "Transition source references conflict: ID refers to state 'State A' but name refers to state 'State B'",
  "field": "source"
}
```

## Data Consistency Rules

### Name ↔ ID Consistency (FR-010)

**Invariant**: For any State, the combination of `id` and `name` must be consistent throughout the journey definition lifecycle.

**Enforcement**:
- State names are immutable (existing behavior)
- State IDs are immutable (new behavior)
- Transitions cannot reference a state by a different name/ID combination than what exists

### State Uniqueness (FR-009)

**Invariant**: State names must be unique within a journey definition.

**Enforcement**:
- Unique index on `{ journeyDefinitionId: 1, name: 1 }`
- Validation before persistence
- Clear error message on duplicate name

### ID Uniqueness (FR-012, SC-011)

**Invariant**: State IDs must be globally unique.

**Enforcement**:
- UUID v4 provides collision-resistant generation
- Index on `{ id: 1 }` for global lookups
- Validation of UUID format when client-provided

## Migration Notes

**No database migration required** - Database is currently empty (assumption).

If migration were needed:
1. Add new fields as optional to MongoDB documents
2. Generate UUIDs for existing states (batch process)
3. Update indexes
4. Validate data consistency
5. Provide rollback capability

## Performance Considerations

### Index Strategy

**New Indexes**:
- `{ journeyDefinitionId: 1, id: 1 }` - For ID-based lookups within a definition
- `{ id: 1 }` - For global ID lookups (rare, but needed for validation)
- `{ sourceStateId: 1 }` and `{ targetStateId: 1 }` - For transition resolution

**Impact**: Minimal - indexes are small and query patterns are optimized

### Reference Resolution Performance

**Target**: Sub-millisecond resolution (SC-005)

**Strategy**:
- Load all states into memory during validation (single query)
- Build HashMap indexed by ID and name for O(1) lookups
- Single-pass validation for all transitions
- No repeated database queries

### UUID Generation Performance

**Target**: <10% performance impact (SC-004)

**Strategy**:
- UUID.randomUUID() is highly optimized in Java
- Generate during object construction (no additional I/O)
- Minimal overhead compared to existing operations

## Summary

The data model changes are incremental and backward-compatible:
- Add optional fields without removing existing ones
- Support dual reference patterns for gradual migration
- Maintain data consistency through validation
- Optimize performance through strategic indexing
- No breaking changes to existing data or contracts
