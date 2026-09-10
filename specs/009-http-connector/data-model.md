# Data Model: Connector Framework (HTTP Connector)

**Feature**: 009-http-connector
**Date**: 2025-06-27
**Purpose**: Define domain entities, value objects, and their relationships for the connector framework

## Entity Overview

The connector framework introduces several new domain entities and extends existing ones to support SERVICE_TASK states and connector execution.

## Domain Entities

### Connector

**Package**: `com.luscadevs.journeyorchestrator.domain.connector`

**Type**: Interface (Strategy Pattern)

**Purpose**: Defines the contract for all connector implementations. Runtime delegates execution to concrete implementations without knowledge of protocol-specific details.

**Methods**:
- `ConnectorResult execute(ConnectorConfiguration config, ExecutionContext context)`: Executes the connector with given configuration and context

**Implementations**:
- `HttpConnector`: HTTP-based connector implementation
- Future: `KafkaConnector`, `RabbitMQConnector`, etc.

**Validation Rules**:
- All implementations must handle null context gracefully
- All implementations must throw `ConnectorException` on failure
- All implementations must return non-null `ConnectorResult`

---

### ConnectorType

**Package**: `com.luscadevs.journeyorchestrator.domain.connector`

**Type**: Enum

**Purpose**: Identifies the type of connector to be used.

**Values**:
- `HTTP`: HTTP-based connector
- Future: `KAFKA`, `RABBITMQ`, `WEBHOOK`, `GRPC`, `GRAPHQL`, `SCRIPT`

**Validation Rules**:
- Cannot be null
- Must map to a valid connector implementation

---

### ConnectorConfiguration

**Package**: `com.luscadevs.journeyorchestrator.domain.connector`

**Type**: Abstract Entity

**Purpose**: Base class for all connector-specific configurations. Stores the parameters needed for connector execution.

**Fields**:
- `connectorType`: `ConnectorType` (required) - Type of connector
- `timeout`: `Duration` (required, default: 30s) - Maximum execution time
- `retryable`: `boolean` (default: false) - Whether execution can be retried (reserved for future use)

**Validation Rules**:
- `connectorType` cannot be null
- `timeout` must be positive (> 0)
- `timeout` cannot exceed maximum configured value (e.g., 5 minutes)

**Subclasses**:
- `HttpConnectorConfiguration`: HTTP-specific configuration

---

### HttpConnectorConfiguration

**Package**: `com.luscadevs.journeyorchestrator.domain.connector.http`

**Type**: Entity (extends ConnectorConfiguration)

**Purpose**: Stores HTTP-specific configuration parameters.

**Fields**:
- `method`: `HttpMethod` (required) - HTTP method (GET, POST, PUT, DELETE, PATCH)
- `url`: `String` (required) - Request URL, may contain context variable placeholders
- `headers`: `Map<String, String>` (optional) - HTTP headers, values may contain placeholders
- `queryParams`: `Map<String, String>` (optional) - Query parameters, values may contain placeholders
- `body`: `String` (optional) - Request body, may contain placeholders

**Validation Rules**:
- `method` cannot be null
- `url` cannot be null or blank
- `url` must be a valid URI after variable resolution
- `headers` keys cannot be null or blank
- `timeout` inherited from parent must be positive

**Placeholders**:
- All string fields support context variable placeholders using `${variableName}` syntax
- Placeholders are resolved at runtime before HTTP request execution

---

### HttpMethod

**Package**: `com.luscadevs.journeyorchestrator.domain.connector.http`

**Type**: Enum

**Purpose**: Defines supported HTTP methods.

**Values**:
- `GET`
- `POST`
- `PUT`
- `DELETE`
- `PATCH`

**Validation Rules**:
- Cannot be null
- Must be one of the supported values

---

### ConnectorExecutionRecord

**Package**: `com.luscadevs.journeyorchestrator.domain.connector`

**Type**: Entity

**Purpose**: Represents a single connector execution with audit information. Stored as part of journey instance for audit trail.

**Fields**:
- `id`: `String` (required, auto-generated) - Unique identifier for the execution record
- `journeyInstanceId`: `String` (required) - Reference to the journey instance
- `stateId`: `String` (required) - ID of the state that triggered the connector
- `connectorType`: `ConnectorType` (required) - Type of connector executed
- `connectorConfiguration`: `String` (required) - Serialized configuration (for audit purposes)
- `startTime`: `Instant` (required) - Execution start timestamp
- `endTime`: `Instant` (required) - Execution end timestamp
- `duration`: `Duration` (required, calculated) - Execution duration (endTime - startTime)
- `status`: `ExecutionStatus` (required) - Execution status (SUCCESS, FAILURE)
- `result`: `String` (optional) - Execution result (response data, may be truncated for large payloads)
- `errorMessage`: `String` (optional) - Error message if execution failed

**Validation Rules**:
- `id` cannot be null
- `journeyInstanceId` cannot be null
- `stateId` cannot be null
- `connectorType` cannot be null
- `connectorConfiguration` cannot be null
- `startTime` cannot be null
- `endTime` cannot be null
- `endTime` must be >= startTime
- `status` cannot be null
- `errorMessage` is required when status is FAILURE
- `result` is optional but recommended when status is SUCCESS

**Relationships**:
- Belongs to: `JourneyInstance` (via journeyInstanceId)
- Associated with: `State` (via stateId)

---

### ConnectorResult

**Package**: `com.luscadevs.journeyorchestrator.domain.connector`

**Type**: Value Object

**Purpose**: Represents the result of a connector execution. Used for context enrichment.

**Fields**:
- `success`: `boolean` (required) - Whether execution succeeded
- `data`: `Map<String, Object>` (optional) - Result data for context enrichment
- `errorMessage`: `String` (optional) - Error message if execution failed
- `statusCode`: `Integer` (optional) - HTTP status code (for HTTP connectors)

**Validation Rules**:
- `success` cannot be null
- `data` cannot be null when success is true
- `errorMessage` is required when success is false
- `statusCode` is required for HTTP connectors

**Usage**:
- On success: `data` is merged into execution context
- On failure: `errorMessage` is recorded in audit trail

---

### HttpConnectorResult

**Package**: `com.luscadevs.journeyorchestrator.domain.connector.http`

**Type**: Value Object (extends ConnectorResult)

**Purpose**: HTTP-specific result with HTTP response details.

**Additional Fields**:
- `statusCode`: `int` (required) - HTTP status code
- `responseHeaders`: `Map<String, String>` (optional) - Response headers
- `responseBody`: `String` (optional) - Raw response body

**Validation Rules**:
- `statusCode` cannot be null
- `statusCode` must be a valid HTTP status code (100-599)
- Inherits all validation rules from `ConnectorResult`

---

## Extended Entities

### State (Extension)

**Package**: `com.luscadevs.journeyorchestrator.domain.journey`

**Type**: Enum Extension

**Purpose**: Extended to support SERVICE_TASK state type.

**Changes**:
- Add `SERVICE_TASK` to `StateType` enum
- Add `connectorConfiguration` field to `State` entity (optional, only for SERVICE_TASK)

**New Field**:
- `connectorConfiguration`: `ConnectorConfiguration` (optional) - Connector configuration for SERVICE_TASK states

**Validation Rules**:
- `connectorConfiguration` is required when `stateType` is SERVICE_TASK
- `connectorConfiguration` must be null for other state types

---

### ExecutionContext (Extension)

**Package**: `com.luscadevs.journeyorchestrator.domain.journeyinstance`

**Type**: Entity Extension

**Purpose**: Extended to support context enrichment from connector results.

**Changes**:
- Add method to merge data into context
- Add method to retrieve variables for placeholder resolution

**New Methods**:
- `mergeData(Map<String, Object> data)`: Deep merges new data into context
- `getVariable(String key)`: Retrieves a variable value (supports dot notation for nested access)
- `hasVariable(String key)`: Checks if a variable exists

**Validation Rules**:
- `mergeData` must preserve existing data (deep merge)
- `getVariable` returns null if key doesn't exist
- `getVariable` supports nested access (e.g., "customer.id")

---

## Relationships

```
JourneyInstance (1) ----< (N) ConnectorExecutionRecord
State (1) ----< (1) ConnectorConfiguration
ConnectorConfiguration (1) <---- (1) HttpConnectorConfiguration
Connector (interface) <---- (N) HttpConnector
ExecutionContext (1) ----< (N) ConnectorResult
```

## Persistence Model

### MongoDB Document Structure

**ConnectorExecutionDocument** (embedded in JourneyInstanceDocument):

```java
@Document(collection = "journey_instances")
class JourneyInstanceDocument {
    // existing fields...
    
    @Field("connector_executions")
    private List<ConnectorExecutionDocument> connectorExecutions;
}

class ConnectorExecutionDocument {
    @Id
    private String id;
    
    @Field("journey_instance_id")
    private String journeyInstanceId;
    
    @Field("state_id")
    private String stateId;
    
    @Field("connector_type")
    private String connectorType;
    
    @Field("connector_configuration")
    private String connectorConfiguration; // JSON string
    
    @Field("start_time")
    private Instant startTime;
    
    @Field("end_time")
    private Instant endTime;
    
    @Field("duration_ms")
    private Long durationMs;
    
    @Field("status")
    private String status;
    
    @Field("result")
    private String result; // JSON string, may be truncated
    
    @Field("error_message")
    private String errorMessage;
}
```

**Indexes**:
- Compound index on `journeyInstanceId` and `startTime` for efficient audit queries
- Index on `status` for filtering by execution status

## Data Flow

1. **Configuration**: Journey designer creates SERVICE_TASK state with HttpConnectorConfiguration
2. **Execution**: Runtime detects state entry, resolves connector via ConnectorResolver
3. **Resolution**: ContextVariableResolver resolves placeholders in configuration
4. **Execution**: HttpConnector executes HTTP request using WebClient
5. **Result**: HttpConnectorResult created from HTTP response
6. **Enrichment**: ConnectorResult.data merged into ExecutionContext
7. **Audit**: ConnectorExecutionRecord created and persisted
8. **Transition**: Runtime advances to next state on success, stops on failure

## Validation Summary

| Entity | Key Validation Rules |
|--------|---------------------|
| ConnectorConfiguration | connectorType not null, timeout > 0 |
| HttpConnectorConfiguration | methodnot null, url not blank, valid URI |
| ConnectorExecutionRecord | timestamps valid, errorMessage required on failure |
| ConnectorResult | success not null, data required on success |
| ExecutionContext | deep merge preserves existing data |

## Migration Notes

No data migration required for existing journey instances. New entities are additive only.

Existing journey definitions without SERVICE_TASK states will continue to work without modification.
