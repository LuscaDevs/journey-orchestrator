# OpenAPI Specification Evolution: Incremental Graph Evolution Refactor

**Feature**: 008-graph-evolution-refactor  
**Date**: 2025-04-10  
**Status**: Draft

## Overview

This document describes the required changes to the OpenAPI specification (`api-spec/openapi.yaml`) to support the incremental graph evolution refactor. The changes add optional identifier fields to State and Transition schemas while maintaining full backward compatibility.

## Specification Changes

### State Schema

**File**: `api-spec/openapi.yaml`  
**Component**: `schemas/State`

**Changes**: Add optional `id` field and optional `position` field

```yaml
State:
  type: object
  required:
    - name
    - type
  properties:
    id:
      type: string
      format: uuid
      description: Unique identifier for the state (UUID v4). Auto-generated if not provided.
      example: "550e8400-e29b-41d4-a716-446655440000"
    name:
      type: string
      description: Human-readable name for the state (must be unique within journey definition)
      example: "OrderReceived"
    type:
      type: string
      enum: [INITIAL, INTERMEDIATE, FINAL]
      description: State type
    position:
      type: object
      description: Visual editor position data (optional, for React Flow integration)
      properties:
        x:
          type: number
          description: X coordinate
          example: 100
        y:
          type: number
          description: Y coordinate
          example: 200
      required:
        - x
        - y
```

**Backward Compatibility**:
- `id` field is optional (not in `required` array)
- `position` field is optional (not in `required` array)
- Existing fields (`name`, `type`) remain unchanged
- Existing clients can continue to create states without providing `id` or `position`

### Transition Schema (Request)

**File**: `api-spec/openapi.yaml`  
**Component**: `schemas/TransitionRequest`

**Changes**: Add optional `sourceStateId` and `targetStateId` fields

```yaml
TransitionRequest:
  type: object
  required:
    - source
    - target
    - event
  properties:
    source:
      type: string
      description: Source state reference by name (legacy)
      example: "OrderReceived"
    target:
      type: string
      description: Target state reference by name (legacy)
      example: "Processing"
    sourceStateId:
      type: string
      format: uuid
      description: Source state reference by ID (new, preferred). Takes precedence if both source and sourceStateId are provided.
      example: "550e8400-e29b-41d4-a716-446655440000"
    targetStateId:
      type: string
      format: uuid
      description: Target state reference by ID (new, preferred). Takes precedence if both target and targetStateId are provided.
      example: "550e8400-e29b-41d4-a716-446655440001"
    event:
      type: string
      description: Trigger event name
      example: "ORDER_CREATED"
    condition:
      type: string
      description: Optional condition expression
      example: "order.amount > 1000"
```

**Backward Compatibility**:
- `sourceStateId` and `targetStateId` are optional (not in `required` array)
- Existing fields (`source`, `target`, `event`) remain unchanged
- Existing clients can continue to create transitions using only name-based references
- When both name and ID are provided, ID takes precedence (server-side logic)

### Transition Schema (Response)

**File**: `api-spec/openapi.yaml`  
**Component**: `schemas/TransitionResponse`

**Changes**: Add optional `sourceStateId` and `targetStateId` fields

```yaml
TransitionResponse:
  type: object
  required:
    - source
    - target
    - event
  properties:
    source:
      type: string
      description: Source state reference by name (legacy)
      example: "OrderReceived"
    target:
      type: string
      description: Target state reference by name (legacy)
      example: "Processing"
    sourceStateId:
      type: string
      format: uuid
      description: Source state reference by ID (new). Populated when available.
      example: "550e8400-e29b-41d4-a716-446655440000"
    targetStateId:
      type: string
      format: uuid
      description: Target state reference by ID (new). Populated when available.
      example: "550e8400-e29b-41d4-a716-446655440001"
    event:
      type: string
      description: Trigger event name
      example: "ORDER_CREATED"
    condition:
      type: string
      description: Optional condition expression
      example: "order.amount > 1000"
```

**Backward Compatibility**:
- `sourceStateId` and `targetStateId` are optional in responses
- Existing clients can ignore these new fields
- Response format remains compatible with existing parsers

### JourneyDefinition Schema

**File**: `api-spec/openapi.yaml`  
**Component**: `schemas/JourneyDefinition` and `schemas/JourneyDefinitionResponse`

**Changes**: No direct field changes to JourneyDefinition itself. The `states` array will include the new optional fields, and the `transitions` array will include the new optional fields.

```yaml
JourneyDefinition:
  type: object
  required:
    - journeyCode
    - version
    - status
    - states
    - transitions
  properties:
    id:
      type: string
      format: uuid
      description: Unique identifier for the journey definition
      example: "550e8400-e29b-41d4-a716-446655440000"
    journeyCode:
      type: string
      description: Business code for the journey
      example: "ORDER_PROCESSING"
    version:
      type: integer
      description: Version number
      example: 1
    status:
      type: string
      enum: [DRAFT, PUBLISHED, ARCHIVED]
      description: Journey definition status
    states:
      type: array
      items:
        $ref: '#/components/schemas/State'
    transitions:
      type: array
      items:
        $ref: '#/components/schemas/TransitionRequest'
```

## Endpoint Changes

### POST /journey-definitions

**Request Body Changes**:
- States may optionally include `id` field
- States may optionally include `position` field
- Transitions may optionally include `sourceStateId` and `targetStateId` fields

**Response Body Changes**:
- States will include `id` field (auto-generated if not provided)
- States will include `position` field if provided in request
- Transitions will include `sourceStateId` and `targetStateId` fields (populated from resolved states)

**Example Request (Legacy - Name-based)**:
```json
{
  "journeyCode": "ORDER_PROCESSING",
  "version": 1,
  "status": "DRAFT",
  "states": [
    {
      "name": "OrderReceived",
      "type": "INITIAL"
    },
    {
      "name": "Processing",
      "type": "INTERMEDIATE"
    }
  ],
  "transitions": [
    {
      "source": "OrderReceived",
      "target": "Processing",
      "event": "ORDER_CREATED"
    }
  ]
}
```

**Example Request (New - ID-based)**:
```json
{
  "journeyCode": "ORDER_PROCESSING",
  "version": 1,
  "status": "DRAFT",
  "states": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "OrderReceived",
      "type": "INITIAL",
      "position": { "x": 100, "y": 200 }
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "name": "Processing",
      "type": "INTERMEDIATE",
      "position": { "x": 300, "y": 200 }
    }
  ],
  "transitions": [
    {
      "sourceStateId": "550e8400-e29b-41d4-a716-446655440000",
      "targetStateId": "550e8400-e29b-41d4-a716-446655440001",
      "event": "ORDER_CREATED"
    }
  ]
}
```

**Example Request (Mixed)**:
```json
{
  "journeyCode": "ORDER_PROCESSING",
  "version": 1,
  "status": "DRAFT",
  "states": [
    {
      "name": "OrderReceived",
      "type": "INITIAL"
    },
    {
      "name": "Processing",
      "type": "INTERMEDIATE"
    }
  ],
  "transitions": [
    {
      "source": "OrderReceived",
      "targetStateId": "550e8400-e29b-41d4-a716-446655440001",
      "event": "ORDER_CREATED"
    }
  ]
}
```

**Example Response**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440002",
  "journeyCode": "ORDER_PROCESSING",
  "version": 1,
  "status": "DRAFT",
  "states": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "OrderReceived",
      "type": "INITIAL"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "name": "Processing",
      "type": "INTERMEDIATE"
    }
  ],
  "transitions": [
    {
      "source": "OrderReceived",
      "target": "Processing",
      "sourceStateId": "550e8400-e29b-41d4-a716-446655440000",
      "targetStateId": "550e8400-e29b-41d4-a716-446655440001",
      "event": "ORDER_CREATED"
    }
  ]
}
```

### GET /journey-definitions/{id}

**Response Body Changes**:
- States will include `id` field
- States will include `position` field if available
- Transitions will include `sourceStateId` and `targetStateId` fields

### PUT /journey-definitions/{id}

**Request Body Changes**:
- Same as POST /journey-definitions
- State IDs are immutable (cannot be changed)

**Response Body Changes**:
- Same as POST /journey-definitions

## Validation Rules

### State Validation

- `id` (if provided) must be valid UUID v4 format
- `name` must be unique within the journey definition
- `type` must be one of: INITIAL, INTERMEDIATE, FINAL
- `position.x` and `position.y` must be valid numbers (if provided)

### Transition Validation

- At least one of `source` or `sourceStateId` must be provided
- At least one of `target` or `targetStateId` must be provided
- If both `source` and `sourceStateId` are provided, they must refer to the same state
- If both `target` and `targetStateId` are provided, they must refer to the same state
- All references must resolve to valid states within the journey definition
- `event` must not be empty

### Error Responses

**Invalid UUID Format**:
```json
{
  "error": "INVALID_UUID_FORMAT",
  "message": "Invalid UUID format for state id",
  "field": "states[0].id"
}
```

**Duplicate State Name**:
```json
{
  "error": "DUPLICATE_STATE_NAME",
  "message": "State name 'OrderReceived' is not unique within the journey definition",
  "field": "states[1].name"
}
```

**State Reference Conflict**:
```json
{
  "error": "STATE_REFERENCE_CONFLICT",
  "message": "Transition source references conflict: ID refers to state 'OrderReceived' but name refers to state 'Processing'",
  "field": "transitions[0].source"
}
```

**Invalid State Reference**:
```json
{
  "error": "INVALID_STATE_REFERENCE",
  "message": "Transition source references non-existent state 'UnknownState'",
  "field": "transitions[0].source"
}
```

## Code Generation Impact

### Generated Classes

After updating the OpenAPI specification and running `mvn generate-sources`, the following generated classes will be updated:

**State.java** (or equivalent):
- Add `id` field (String, UUID format)
- Add `position` field (Position object)
- Both fields will be optional (no @NotNull annotation)

**TransitionRequest.java** (or equivalent):
- Add `sourceStateId` field (String, UUID format)
- Add `targetStateId` field (String, UUID format)
- Both fields will be optional

**TransitionResponse.java** (or equivalent):
- Add `sourceStateId` field (String, UUID format)
- Add `targetStateId` field (String, UUID format)
- Both fields will be optional

**Position.java** (new generated class):
- Add `x` field (Number)
- Add `y` field (Number)
- Both fields will be required

### Mapper Updates

The following mappers will need to be updated to handle the new fields:

**JourneyDefinitionMapper**:
- Map `State.id` from domain to DTO and vice versa
- Map `State.position` from domain to DTO and vice versa
- Map `Transition.sourceStateId` and `targetStateId` from domain to DTO
- Handle null values for optional fields

## Backward Compatibility Verification

### Contract Validation

To verify backward compatibility:

1. **Compare OpenAPI specifications**:
   - Use a diff tool to compare `api-spec/openapi.yaml` before and after changes
   - Verify no existing fields were removed or renamed
   - Verify no existing fields changed type or became required

2. **Test with legacy clients**:
   - Create journey definition using old contract (no IDs, no position data)
   - Verify request succeeds
   - Verify response includes new optional fields
   - Verify existing E2E tests pass without modification

3. **Test with new clients**:
   - Create journey definition using new contract (with IDs and position data)
   - Verify request succeeds
   - Verify response includes all fields
   - Verify ID-based references work correctly

### Breaking Change Detection

The following changes are **NOT** breaking:
- Adding optional fields to schemas
- Adding new schemas (Position)
- Adding new fields to response bodies

The following changes **WOULD BE** breaking (must be avoided):
- Removing existing fields
- Renaming existing fields
- Changing field types
- Making optional fields required
- Removing existing endpoints

## Success Criteria Alignment

- **SC-006**: Zero breaking changes detected in OpenAPI specification comparison → Verified by schema diff
- **SC-007**: OpenAPI specification successfully updated → This document describes the required changes
- **SC-008**: Code generation produces classes reflecting new model → Verified by running `mvn generate-sources`

## Implementation Order

1. Update `api-spec/openapi.yaml` with the changes described in this document
2. Run `mvn generate-sources` to regenerate code
3. Verify generated classes include new fields
4. Update mappers to handle new fields
5. Implement domain changes (State.id, State.position, Transition ID references)
6. Implement validation logic (dual reference resolution, conflict detection)
7. Update tests to cover both legacy and new patterns
8. Run E2E tests to verify backward compatibility

## Summary

The OpenAPI specification changes are incremental and backward-compatible:
- Add optional fields without removing existing ones
- Support dual reference patterns for gradual migration
- Maintain existing contract for legacy clients
- Enable new capabilities for visual editor integration
- Zero breaking changes to existing API contracts
