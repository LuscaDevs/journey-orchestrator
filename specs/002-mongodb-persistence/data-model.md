# Data Model: MongoDB Persistence

**Feature**: 002-mongodb-persistence  
**Date**: 2026-03-26  

## MongoDB Collections

### 1. journey_definitions

Stores journey templates with states, transitions, and metadata.

```json
{
  "_id": "ObjectId",
  "id": "UUID",
  "name": "string",
  "description": "string",
  "version": "string",
  "states": [
    {
      "id": "string",
      "name": "string",
      "description": "string",
      "type": "START|INTERMEDIATE|END",
      "metadata": "object"
    }
  ],
  "transitions": [
    {
      "id": "string",
      "fromState": "string",
      "toState": "string",
      "event": "string",
      "condition": "string",
      "metadata": "object"
    }
  ],
  "metadata": "object",
  "createdAt": "ISODate",
  "updatedAt": "ISODate",
  "createdBy": "string",
  "updatedBy": "string"
}
```

**Indexes**:
- Compound: `{ "name": 1, "version": 1 }` (unique)
- Single: `{ "createdAt": -1 }`

### 2. journey_instances

Stores active journey executions with current state and history.

```json
{
  "_id": "ObjectId",
  "id": "UUID",
  "journeyDefinitionId": "UUID",
  "currentState": "string",
  "status": "ACTIVE|COMPLETED|FAILED|SUSPENDED",
  "context": "object",
  "metadata": "object",
  "startedAt": "ISODate",
  "completedAt": "ISODate",
  "lastActivityAt": "ISODate",
  "createdAt": "ISODate",
  "updatedAt": "ISODate",
  "createdBy": "string",
  "updatedBy": "string"
}
```

**Indexes**:
- Compound: `{ "journeyDefinitionId": 1, "currentState": 1 }`
- Compound: `{ "status": 1, "lastActivityAt": -1 }`
- Single: `{ "createdAt": -1 }`

### 3. journey_events

Stores audit trail of all state transitions and events.

```json
{
  "_id": "ObjectId",
  "id": "UUID",
  "journeyInstanceId": "UUID",
  "eventType": "STATE_CHANGED|EVENT_APPLIED|INSTANCE_CREATED|INSTANCE_COMPLETED",
  "previousState": "string",
  "newState": "string",
  "eventData": "object",
  "context": "object",
  "timestamp": "ISODate",
  "userId": "string",
  "metadata": "object"
}
```

**Indexes**:
- Compound: `{ "journeyInstanceId": 1, "timestamp": -1 }`
- Single: `{ "eventType": 1 }`
- Single: `{ "timestamp": -1 }`

## Domain to Document Mapping

### JourneyDefinition Entity

**Domain Fields** → **Document Fields**:
- `id` → `id`
- `name` → `name`
- `description` → `description`
- `version` → `version`
- `states` → `states` (array)
- `transitions` → `transitions` (array)
- `metadata` → `metadata`
- `createdAt` → `createdAt`
- `updatedAt` → `updatedAt`

### JourneyInstance Entity

**Domain Fields** → **Document Fields**:
- `id` → `id`
- `journeyDefinitionId` → `journeyDefinitionId`
- `currentState` → `currentState`
- `status` → `status`
- `context` → `context`
- `metadata` → `metadata`
- `startedAt` → `startedAt`
- `completedAt` → `completedAt`
- `lastActivityAt` → `lastActivityAt`
- `createdAt` → `createdAt`
- `updatedAt` → `updatedAt`

### Event Entity

**Domain Fields** → **Document Fields**:
- `id` → `id`
- `journeyInstanceId` → `journeyInstanceId`
- `eventType` → `eventType`
- `previousState` → `previousState`
- `newState` → `newState`
- `eventData` → `eventData`
- `context` → `context`
- `timestamp` → `timestamp`
- `userId` → `userId`
- `metadata` → `metadata`

## Validation Rules

### JourneyDefinition
- `name`: Required, max 100 characters
- `version`: Required, semantic version format (x.y.z)
- `states`: At least one START and one END state
- `transitions`: Valid references to existing states

### JourneyInstance
- `journeyDefinitionId`: Required, valid UUID
- `currentState`: Required, must exist in journey definition
- `status`: Required, valid enum value
- `context`: Optional, JSON object

### Event
- `journeyInstanceId`: Required, valid UUID
- `eventType`: Required, valid enum value
- `timestamp`: Required, ISO date

## Data Consistency

### Referential Integrity
- Journey instances reference valid journey definitions
- Events reference valid journey instances
- State transitions follow defined journey definition

### Atomic Operations
- State changes are atomic with event creation
- Journey instance updates include audit trail
- Batch operations use transactions where appropriate

## Performance Considerations

### Query Optimization
- Indexes support common query patterns
- Pagination for large result sets
- Projection to limit returned fields
- Aggregation pipelines for complex queries

### Storage Optimization
- Embedded documents for related data
- Appropriate data types (dates, numbers)
- Compression for large text fields
- TTL indexes for temporary data

## Schema Evolution

- Version field in documents for schema changes
- Backward compatibility for existing data
- Migration scripts for schema updates
- Validation during application startup
