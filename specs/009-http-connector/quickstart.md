# Quick Start Guide: Connector Framework (HTTP Connector)

**Feature**: 009-http-connector
**Date**: 2025-06-27
**Purpose**: Quick start guide for developers to understand and use the connector framework

## Overview

The Connector Framework enables journey definitions to execute external integrations automatically when entering SERVICE_TASK states. This guide covers the HTTP Connector implementation.

## Key Concepts

### SERVICE_TASK State

A new state type that triggers connector execution when a journey instance enters it. Unlike other state types that wait for external events, SERVICE_TASK states automatically execute configured connectors.

### Connector Strategy Pattern

The framework uses the Strategy pattern to allow extensibility. Each connector type (HTTP, Kafka, etc.) implements the `Connector` interface, allowing the runtime to delegate execution without knowledge of protocol-specific details.

### Context Variables

Connectors can read from and write to the execution context using placeholder syntax `${variableName}`. This enables data flow between journey steps and external systems.

### Audit Trail

All connector executions are recorded with detailed audit information including timestamps, duration, status, results, and error details.

## Creating a Journey with HTTP Connector

### Step 1: Define a SERVICE_TASK State

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "FetchCustomerData",
  "type": "SERVICE_TASK",
  "connectorConfiguration": {
    "connectorType": "HTTP",
    "timeout": "PT30S",
    "method": "GET",
    "url": "https://api.example.com/customers/${customerId}",
    "headers": {
      "Authorization": "Bearer ${apiToken}",
      "Content-Type": "application/json"
    },
    "queryParams": {
      "include": "profile,preferences"
    }
  }
}
```

### Step 2: Create Journey Definition

```json
{
  "journeyCode": "customer-onboarding",
  "name": "Customer Onboarding Journey",
  "version": 1,
  "states": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Start",
      "type": "INITIAL"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "name": "FetchCustomerData",
      "type": "SERVICE_TASK",
      "connectorConfiguration": {
        "connectorType": "HTTP",
        "timeout": "PT30S",
        "method": "GET",
        "url": "https://api.example.com/customers/${customerId}",
        "headers": {
          "Authorization": "Bearer ${apiToken}"
        }
      }
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "name": "Complete",
      "type": "FINAL"
    }
  ],
  "transitions": [
    {
      "sourceStateId": "550e8400-e29b-41d4-a716-446655440000",
      "targetStateId": "550e8400-e29b-41d4-a716-446655440001",
      "event": "START"
    },
    {
      "sourceStateId": "550e8400-e29b-41d4-a716-446655440001",
      "targetStateId": "550e8400-e29b-41d4-a716-446655440002",
      "event": "CONNECTOR_SUCCESS"
    }
  ]
}
```

### Step 3: Start Journey Instance with Context

```bash
curl -X POST http://localhost:8080/journey-instances \
  -H "Content-Type: application/json" \
  -d '{
    "journeyCode": "customer-onboarding",
    "version": 1,
    "context": {
      "customerId": "CUST-12345",
      "apiToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
  }'
```

### Step 4: Automatic Execution

When the journey instance enters the "FetchCustomerData" SERVICE_TASK state:
1. Runtime resolves context variables in the URL and headers
2. HTTP Connector executes the GET request
3. Response data is merged into the execution context
4. Journey automatically advances to the next state on success

### Step 5: View Connector Execution History

```bash
curl http://localhost:8080/journey-instances/{instanceId}/connector-executions
```

Response:
```json
{
  "instanceId": "inst-12345",
  "executions": [
    {
      "id": "exec-67890",
      "journeyInstanceId": "inst-12345",
      "stateId": "550e8400-e29b-41d4-a716-446655440001",
      "connectorType": "HTTP",
      "connectorConfiguration": "{\"connectorType\":\"HTTP\",\"method\":\"GET\",\"url\":\"https://api.example.com/customers/CUST-12345\"}",
      "startTime": "2025-06-27T10:00:00Z",
      "endTime": "2025-06-27T10:00:00.250Z",
      "duration": "PT0.25S",
      "status": "SUCCESS",
      "result": "{\"id\":\"CUST-12345\",\"name\":\"John Doe\",\"email\":\"john@example.com\"}"
    }
  ],
  "totalCount": 1
}
```

## Context Enrichment

### Reading from Context

Use `${variableName}` placeholders in connector configuration:

```json
{
  "url": "https://api.example.com/customers/${customerId}",
  "headers": {
    "Authorization": "Bearer ${authToken}"
  }
}
```

### Writing to Context

Successful connector responses are automatically merged into the execution context. If the HTTP response is:

```json
{
  "customerId": "CUST-12345",
  "profile": {
    "name": "John Doe",
    "tier": "GOLD"
  }
}
```

The execution context will be enriched with this data, available to subsequent states.

### Nested Access

Use dot notation for nested object access:

```json
{
  "url": "https://api.example.com/customers/${customer.id}"
}
```

## HTTP Connector Configuration

### Supported HTTP Methods

- GET
- POST
- PUT
- DELETE
- PATCH

### Configuration Options

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| connectorType | enum | Yes | Must be "HTTP" |
| timeout | duration | Yes | Maximum execution time (ISO 8601 format, default: PT30S) |
| method | enum | Yes | HTTP method to use |
| url | string | Yes | Request URL, supports placeholders |
| headers | object | No | HTTP headers, values support placeholders |
| queryParams | object | No | Query parameters, values support placeholders |
| body | string | No | Request body, supports placeholders |

### Timeout Configuration

Timeout is specified in ISO 8601 duration format:
- `PT30S` - 30 seconds
- `PT1M` - 1 minute
- `PT5M` - 5 minutes

## Error Handling

### Timeout

If the HTTP request exceeds the configured timeout:
- Journey execution stops
- Audit record shows FAILURE status
- Error message indicates timeout

### HTTP Errors

For 4xx and 5xx responses:
- Journey execution stops
- Audit record shows FAILURE status
- Error message includes HTTP status code and response body

### Missing Context Variables

If a referenced context variable doesn't exist:
- Journey execution stops
- Audit record shows FAILURE status
- Error message indicates missing variable

## Testing Locally

### Prerequisites

- Journey Orchestrator running on localhost:8080
- MongoDB instance running
- External HTTP endpoint available (or use mock server)

### Test Journey

1. Create a journey definition with SERVICE_TASK
2. Start a journey instance with initial context
3. Monitor connector execution via audit endpoint
4. Verify context enrichment in journey instance response

### Mock HTTP Server

For testing without real external APIs, use a mock server:

```bash
# Using json-server
npm install -g json-server
echo '{"customers": [{"id": "CUST-12345", "name": "Test Customer"}]}' > db.json
json-server --port 3000
```

Configure connector to use `http://localhost:3000/customers/${customerId}`.

## Common Patterns

### API Authentication

```json
{
  "headers": {
    "Authorization": "Bearer ${authToken}",
    "X-API-Key": "${apiKey}"
  }
}
```

### POST with JSON Body

```json
{
  "method": "POST",
  "url": "https://api.example.com/orders",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": "{\"customerId\": \"${customerId}\", \"items\": ${orderItems}}"
}
```

### Query Parameters

```json
{
  "queryParams": {
    "limit": "10",
    "offset": "${offset}",
    "filter": "${filterValue}"
  }
}
```

## Troubleshooting

### Connector Not Executing

- Verify state type is SERVICE_TASK
- Check connectorConfiguration is present and valid
- Ensure connectorType matches an implemented connector

### Context Variables Not Resolving

- Verify variable exists in execution context
- Check placeholder syntax: `${variableName}`
- Use dot notation for nested access: `${customer.id}`

### HTTP Request Failing

- Check URL is valid after variable resolution
- Verify external endpoint is accessible
- Review audit record for error details
- Check timeout configuration

### Context Not Enriched

- Verify HTTP response is JSON format
- Check response status is 2xx (success)
- Review audit record for result data

## Next Steps

- Review [data-model.md](./data-model.md) for entity details
- Review [research.md](./research.md) for technical decisions
- Review [contracts/openapi.yaml](./contracts/openapi.yaml) for API specifications
- Proceed to [tasks.md](./tasks.md) for implementation tasks (generated by /speckit.tasks)

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Journey Runtime                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐    ┌──────────────────────────────────┐  │
│  │ State Entry  │───▶│ Connector Execution Service       │  │
│  └──────────────┘    └──────────────────────────────────┘  │
│                              │                               │
│                              ▼                               │
│                    ┌──────────────────────┐                 │
│                    │ Connector Resolver   │                 │
│                    └──────────────────────┘                 │
│                              │                               │
│                              ▼                               │
│              ┌───────────────────────────────┐               │
│              │   Connector (Strategy)       │               │
│              └───────────────────────────────┘               │
│                              │                               │
│              ┌───────────────┴───────────────┐               │
│              ▼                               ▼               │
│  ┌─────────────────────┐      ┌─────────────────────┐     │
│  │   HTTP Connector    │      │  Future Connectors   │     │
│  │  (Implementation)   │      │  (Kafka, RabbitMQ)   │     │
│  └─────────────────────┘      └─────────────────────┘     │
│              │                                                   │
│              ▼                                                   │
│  ┌─────────────────────┐                                       │
│  │  External HTTP API  │                                       │
│  └─────────────────────┘                                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Support

For questions or issues:
1. Check audit records for detailed execution information
2. Review logs with correlationId for request tracing
3. Consult architecture documentation in `docs/adr/`
