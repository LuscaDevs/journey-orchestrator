# API Contracts: Conditional Transitions in Journey State Machine

**Feature**: `006-conditional-transitions`  
**Date**: 2025-03-30  
**Status**: Complete

## Overview

This document defines the API contracts for managing conditional transitions in the journey orchestration system. The contracts extend the existing journey definition APIs to support condition-based transition evaluation while maintaining backward compatibility.

## REST API Endpoints

### Journey Definition Management (Enhanced)

#### Create Journey Definition with Conditional Transitions

**Endpoint**: `POST /api/v1/journey-definitions`  
**Description**: Create a new journey definition with optional conditional transitions  
**Request Body**: `CreateJourneyDefinitionRequest`  
**Response**: `JourneyDefinitionResponse`  
**Status Codes**: 201 (Created), 400 (Bad Request), 422 (Unprocessable Entity)

#### Update Journey Definition with Conditions

**Endpoint**: `PUT /api/v1/journey-definitions/{id}`  
**Description**: Update an existing journey definition, including conditional transitions  
**Request Body**: `UpdateJourneyDefinitionRequest`  
**Response**: `JourneyDefinitionResponse`  
**Status Codes**: 200 (OK), 404 (Not Found), 400 (Bad Request), 422 (Unprocessable Entity)

### Condition Management

#### Validate Condition Expression

**Endpoint**: `POST /api/v1/conditions/validate`  
**Description**: Validate a condition expression syntax and security  
**Request Body**: `ValidateConditionRequest`  
**Response**: `ValidateConditionResponse`  
**Status Codes**: 200 (OK), 400 (Bad Request), 422 (Unprocessable Entity)

#### Get Condition Evaluation History

**Endpoint**: `GET /api/v1/journey-instances/{instanceId}/condition-evaluations`  
**Description**: Retrieve condition evaluation history for a journey instance  
**Query Parameters**: 
- `page` (int, default: 0): Page number
- `size` (int, default: 20): Page size
- `fromDate` (string, optional): Filter by date (ISO-8601)
- `toDate` (string, optional): Filter by date (ISO-8601)  
**Response**: `ConditionEvaluationHistoryResponse`  
**Status Codes**: 200 (OK), 404 (Not Found)

#### Test Condition Evaluation

**Endpoint**: `POST /api/v1/conditions/test`  
**Description**: Test condition evaluation against sample context data  
**Request Body**: `TestConditionRequest`  
**Response**: `TestConditionResponse`  
**Status Codes**: 200 (OK), 400 (Bad Request), 422 (Unprocessable Entity)

## Request/Response Models

### CreateJourneyDefinitionRequest

```json
{
  "name": "string",
  "description": "string",
  "version": "string",
  "states": [
    {
      "id": "string",
      "name": "string",
      "type": "START|INTERMEDIATE|END"
    }
  ],
  "transitions": [
    {
      "id": "string",
      "fromState": "string",
      "toState": "string",
      "event": "string",
      "condition": {
        "expression": "string",
        "description": "string"
      },
      "conditionOrder": "integer"
    }
  ]
}
```

### UpdateJourneyDefinitionRequest

```json
{
  "name": "string",
  "description": "string",
  "states": [
    {
      "id": "string",
      "name": "string",
      "type": "START|INTERMEDIATE|END"
    }
  ],
  "transitions": [
    {
      "id": "string",
      "fromState": "string",
      "toState": "string",
      "event": "string",
      "condition": {
        "expression": "string",
        "description": "string"
      },
      "conditionOrder": "integer"
    }
  ]
}
```

### JourneyDefinitionResponse

```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "version": "string",
  "status": "DRAFT|ACTIVE|INACTIVE",
  "states": [
    {
      "id": "string",
      "name": "string",
      "type": "START|INTERMEDIATE|END"
    }
  ],
  "transitions": [
    {
      "id": "string",
      "fromState": "string",
      "toState": "string",
      "event": "string",
      "condition": {
        "id": "string",
        "expression": "string",
        "description": "string",
        "validationStatus": "VALID|INVALID|PENDING",
        "validationErrors": ["string"]
      },
      "conditionOrder": "integer",
      "createdAt": "string",
      "updatedAt": "string"
    }
  ],
  "createdAt": "string",
  "updatedAt": "string"
}
```

### ValidateConditionRequest

```json
{
  "expression": "string",
  "contextSample": {
    "journeyData": {
      "property1": "value1",
      "property2": "value2"
    },
    "eventData": {
      "property1": "value1",
      "property2": "value2"
    },
    "systemData": {
      "timestamp": "string",
      "correlationId": "string"
    }
  }
}
```

### ValidateConditionResponse

```json
{
  "valid": "boolean",
  "errors": [
    {
      "type": "SYNTAX|SECURITY|SEMANTIC",
      "message": "string",
      "line": "integer",
      "column": "integer"
    }
  ],
  "warnings": [
    {
      "type": "PERFORMANCE|COMPLEXITY",
      "message": "string"
    }
  ],
  "complexityScore": "integer",
  "referencedProperties": ["string"]
}
```

### TestConditionRequest

```json
{
  "expression": "string",
  "contextData": {
    "journeyInstanceId": "string",
    "currentState": "string",
    "journeyData": {
      "property1": "value1",
      "property2": "value2"
    },
    "eventData": {
      "property1": "value1",
      "property2": "value2"
    },
    "systemData": {
      "timestamp": "string",
      "correlationId": "string"
    }
  }
}
```

### TestConditionResponse

```json
{
  "result": "boolean",
  "success": "boolean",
  "executionTime": "string",
  "evaluatedAt": "string",
  "error": {
    "type": "SYNTAX_ERROR|RUNTIME_ERROR|SECURITY_VIOLATION|TIMEOUT",
    "message": "string"
  }
}
```

### ConditionEvaluationHistoryResponse

```json
{
  "content": [
    {
      "id": "string",
      "journeyInstanceId": "string",
      "transitionId": "string",
      "conditionId": "string",
      "conditionExpression": "string",
      "result": "boolean",
      "success": "boolean",
      "executionTime": "string",
      "evaluatedAt": "string",
      "error": {
        "type": "SYNTAX_ERROR|RUNTIME_ERROR|SECURITY_VIOLATION|TIMEOUT",
        "message": "string"
      },
      "contextData": {
        "currentState": "string",
        "eventData": "object",
        "journeyData": "object"
      }
    }
  ],
  "page": {
    "number": "integer",
    "size": "integer",
    "totalElements": "integer",
    "totalPages": "integer"
  }
}
```

## Error Response Format

All error responses follow the RFC 9457 ProblemDetail format:

```json
{
  "type": "string",
  "title": "string",
  "status": "integer",
  "detail": "string",
  "timestamp": "string",
  "path": "string",
  "errorCode": "string",
  "instance": "string"
}
```

### Error Codes

#### Condition-Related Errors

- `CONDITION_INVALID_SYNTAX` - Condition expression has invalid syntax
- `CONDITION_SECURITY_VIOLATION` - Condition attempts to access restricted resources
- `CONDITION_EVALUATION_TIMEOUT` - Condition evaluation exceeded time limit
- `CONDITION_TOO_COMPLEX` - Condition complexity exceeds allowed limits
- `CONDITION_PROPERTY_NOT_FOUND` - Condition references non-existent property
- `CONDITION_TYPE_MISMATCH` - Condition has type compatibility issues

#### Journey Definition Errors

- `JOURNEY_DEFINITION_NOT_FOUND` - Journey definition not found
- `TRANSITION_NOT_FOUND` - Transition not found in journey definition
- `INVALID_TRANSITION_CONDITION` - Invalid transition condition configuration
- `CONDITION_ORDER_CONFLICT` - Multiple conditions have same order value

## OpenAPI Specification Updates

The following OpenAPI 3.0.3 specification updates are required:

```yaml
openapi: 3.0.3
info:
  title: Journey Orchestrator API
  version: 1.0.0
  description: API for managing journey orchestrations with conditional transitions

paths:
  /api/v1/journey-definitions:
    post:
      summary: Create journey definition
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateJourneyDefinitionRequest'
      responses:
        '201':
          description: Journey definition created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/JourneyDefinitionResponse'
        '400':
          description: Bad request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProblemDetail'
        '422':
          description: Unprocessable entity
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProblemDetail'

  /api/v1/journey-definitions/{id}:
    put:
      summary: Update journey definition
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateJourneyDefinitionRequest'
      responses:
        '200':
          description: Journey definition updated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/JourneyDefinitionResponse'
        '404':
          description: Journey definition not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProblemDetail'

  /api/v1/conditions/validate:
    post:
      summary: Validate condition expression
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ValidateConditionRequest'
      responses:
        '200':
          description: Condition validation result
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ValidateConditionResponse'

  /api/v1/conditions/test:
    post:
      summary: Test condition evaluation
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/TestConditionRequest'
      responses:
        '200':
          description: Condition test result
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TestConditionResponse'

  /api/v1/journey-instances/{instanceId}/condition-evaluations:
    get:
      summary: Get condition evaluation history
      parameters:
        - name: instanceId
          in: path
          required: true
          schema:
            type: string
        - name: page
          in: query
          schema:
            type: integer
            default: 0
        - name: size
          in: query
          schema:
            type: integer
            default: 20
        - name: fromDate
          in: query
          schema:
            type: string
            format: date-time
        - name: toDate
          in: query
          schema:
            type: string
            format: date-time
      responses:
        '200':
          description: Condition evaluation history
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ConditionEvaluationHistoryResponse'

components:
  schemas:
    # ... existing schemas ...
    
    TransitionCondition:
      type: object
      properties:
        id:
          type: string
        expression:
          type: string
          description: SpEL expression for condition evaluation
        description:
          type: string
          description: Human-readable description of the condition
        validationStatus:
          type: string
          enum: [VALID, INVALID, PENDING]
        validationErrors:
          type: array
          items:
            type: string
      required:
        - expression
    
    Transition:
      type: object
      properties:
        id:
          type: string
        fromState:
          type: string
        toState:
          type: string
        event:
          type: string
        condition:
          $ref: '#/components/schemas/TransitionCondition'
        conditionOrder:
          type: integer
          minimum: 1
      required:
        - id
        - fromState
        - toState
        - event
    
    # ... other schema definitions ...
```

## Security Considerations

### Authentication and Authorization

- All endpoints require valid authentication tokens
- Condition management endpoints require `JOURNEY_DEFINITION_MANAGE` permission
- Condition evaluation history requires `JOURNEY_INSTANCE_READ` permission
- Condition validation and testing require `JOURNEY_DEFINITION_READ` permission

### Input Validation

- All string inputs are validated for length and allowed characters
- Condition expressions are validated for syntax and security
- Context data is sanitized before evaluation
- Request size limits are enforced to prevent DoS attacks

### Rate Limiting

- Condition validation endpoints are rate-limited per user
- Condition testing endpoints have stricter rate limits
- History retrieval endpoints have pagination limits

## Performance Considerations

### Response Time Targets

- Condition validation: < 100ms
- Condition testing: < 50ms
- Journey definition CRUD: < 200ms
- Condition evaluation history: < 300ms

### Pagination

- History endpoints use cursor-based pagination for large datasets
- Default page size: 20, maximum: 100
- Pagination metadata included in responses

### Caching

- Validation results cached for 5 minutes
- Compiled expressions cached in memory
- History queries cached for 1 minute

## Monitoring and Observability

### Metrics

- Condition validation success/failure rates
- Condition evaluation performance metrics
- API endpoint response times
- Error rates by error type

### Logging

- All condition evaluations logged with correlation IDs
- Security violations logged with user context
- Performance metrics logged for monitoring
- Error details logged for debugging

### Health Checks

- Condition evaluation service health check
- Database connectivity for condition storage
- Expression cache health status
