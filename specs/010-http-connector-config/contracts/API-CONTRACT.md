# API Contract: HTTP Connector Configuration

**Feature**: 010-http-connector-config  
**Date**: 2026-09-10  
**Source**: OpenAPI 3.0.3 specification at `api-spec/openapi.yaml`

---

## Overview

The HTTP Connector Configuration feature consumes existing backend APIs for:

1. **Updating journey definitions** with connector configuration
2. **Retrieving journey definitions** with connector configuration
3. **Executing connector** (runtime, not part of UI but affects configuration design)

The backend API contract is the single source of truth, defined in OpenAPI 3.0.3 format.

---

## API Endpoints Used

### Update Journey Definition

**Endpoint**: `PUT /journeys/{id}`

**Purpose**: Save journey definition with updated connector configuration for a SERVICE_TASK node

**Request Body** (excerpt):

```json
{
  "code": "string",
  "description": "string",
  "version": "integer",
  "nodes": [
    {
      "id": "string",
      "type": "SERVICE_TASK",
      "connectorConfiguration": {
        "connectorType": "HTTP",
        "method": "POST",
        "url": "https://api.example.com/endpoint",
        "headers": {
          "Authorization": "Bearer ${token}",
          "Content-Type": "application/json"
        },
        "queryParams": {
          "limit": "10"
        },
        "body": "{\"data\": \"${data}\"}",
        "timeout": "PT30S"
      },
      "onSuccess": {...},
      "onError": {...}
    }
  ],
  "transitions": [...]
}
```

**Response** (Success - 200):

```json
{
  "id": "string",
  "code": "string",
  "version": "integer",
  "createdAt": "2026-09-10T10:00:00Z",
  "updatedAt": "2026-09-10T10:05:00Z",
  "nodes": [...],
  "transitions": [...]
}
```

**Error Responses**:

- `400 Bad Request`: Invalid connector configuration (invalid timeout format, missing required fields, etc.)
- `404 Not Found`: Journey definition not found
- `409 Conflict`: Journey version mismatch (optimistic locking)

### Get Journey Definition

**Endpoint**: `GET /journeys/{id}`

**Purpose**: Retrieve journey definition including connector configuration

**Response** (Success - 200):

```json
{
  "id": "string",
  "code": "string",
  "version": "integer",
  "nodes": [
    {
      "id": "node-id",
      "type": "SERVICE_TASK",
      "connectorConfiguration": {
        "connectorType": "HTTP",
        "method": "POST",
        "url": "https://api.example.com/endpoint",
        "headers": {...},
        "queryParams": {...},
        "body": "...",
        "timeout": "PT30S"
      },
      ...
    }
  ],
  ...
}
```

---

## Schema Definitions

### HttpConnectorConfiguration

Full schema as defined in OpenAPI:

```yaml
HttpConnectorConfiguration:
  allOf:
    - $ref: "#/components/schemas/ConnectorConfiguration"
    - type: object
      required:
        - method
        - url
      properties:
        method:
          $ref: "#/components/schemas/HttpMethod"
        url:
          type: string
          description: Request URL, may contain context variable placeholders (e.g., ${customerId})
          example: "https://api.example.com/customers/${customerId}"
        headers:
          type: object
          additionalProperties:
            type: string
          description: HTTP headers, values may contain placeholders
          example:
            Authorization: "Bearer ${token}"
            Content-Type: "application/json"
        queryParams:
          type: object
          additionalProperties:
            type: string
          description: Query parameters, values may contain placeholders
          example:
            limit: "10"
            offset: "${offset}"
        body:
          type: string
          description: Request body, may contain placeholders
          example: '{"customerId": "${customerId}", "action": "update"}'

ConnectorConfiguration:
  type: object
  discriminator:
    propertyName: connectorType
  required:
    - connectorType
    - timeout
  properties:
    connectorType:
      $ref: "#/components/schemas/ConnectorType"
    timeout:
      type: string
      format: duration
      description: Maximum execution time (ISO 8601 duration format)
      default: "PT30S"
      example: "PT30S"

HttpMethod:
  type: string
  enum:
    - GET
    - POST
    - PUT
    - DELETE
    - PATCH
  description: HTTP method for the request
```

---

## Error Handling Contracts

### Validation Error Response (400)

When backend validates and rejects invalid connector configuration:

```json
{
  "timestamp": "2026-09-10T10:05:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid journey definition",
  "errors": [
    {
      "field": "nodes[0].connectorConfiguration.url",
      "message": "Invalid URL format",
      "code": "INVALID_URL_FORMAT"
    },
    {
      "field": "nodes[0].connectorConfiguration.timeout",
      "message": "Timeout must be valid ISO 8601 duration",
      "code": "INVALID_TIMEOUT_FORMAT"
    }
  ],
  "traceId": "abc123"
}
```

### API Error Response (5xx)

When backend service is unavailable:

```json
{
  "timestamp": "2026-09-10T10:05:00Z",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Backend service is temporarily unavailable",
  "traceId": "abc123"
}
```

---

## UI Implementation Notes

### Request Building

Before sending UPDATE request, transform form state to backend format:

```typescript
const formState: ConnectorFormState = {...};
const backendPayload: HttpConnectorConfiguration = {
  connectorType: 'HTTP',
  method: formState.method,
  url: formState.url,
  headers: Object.fromEntries(formState.headers.map(e => [e.key, e.value])),
  queryParams: Object.fromEntries(formState.queryParams.map(e => [e.key, e.value])),
  body: formState.body || undefined,
  timeout: formState.timeout
};
```

### Response Handling

After UPDATE response:

1. Validate response schema matches OpenAPI
2. Update local Zustand store with new journey definition
3. Update form state with retrieved config
4. Clear dirty/saving flags

### Error Handling

On API error:

1. Parse error response
2. Extract field-level errors if present (400 response)
3. Display errors in form:
   - Field-level errors next to respective input
   - General API errors in toast notification
4. Enable retry with updated form data

---

## Backward Compatibility

- No previous versions of this connector configuration exist in production
- Future connector types (S3, Kafka, etc.) will use same discriminator pattern
- Client must handle unknown connector types gracefully (pass-through to backend)

---

## Related Documentation

- **Backend Implementation**: Feature 009-http-connector
- **OpenAPI Specification**: `journey-orchestrator/api-spec/openapi.yaml`
- **Backend ADR**: Document decision rationale for connector pattern

---

## Testing Strategy

### Contract Testing

- Use backend's generated TypeScript API client
- Validate request/response shapes match OpenAPI schema
- Test error scenarios: invalid URLs, malformed timeouts, missing required fields

### Integration Testing

- Mock backend API responses
- Test form submission → API call → response handling
- Test error recovery and retry flows

See `quickstart.md` for runnable test scenarios.
