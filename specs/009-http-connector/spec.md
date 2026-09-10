# Feature Specification: Connector Framework (HTTP Connector)

**Feature Branch**: `009-http-connector`
**Created**: 2025-06-27
**Status**: Draft
**Input**: User description: "Connector Framework (HTTP Connector) - Introduzir o conceito de Connectors no Journey Orchestrator, permitindo que uma jornada execute integrações externas de forma desacoplada, extensível e orientada a estratégias. Esta entrega deverá implementar a infraestrutura de Connectors e disponibilizar a primeira implementação: HTTP Connector."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Configure HTTP Connector in Journey Designer (Priority: P1)

As a journey designer, I want to configure a SERVICE_TASK state with an HTTP Connector so that my journey can automatically execute HTTP requests when entering that state.

**Why this priority**: This is the foundational capability - without the ability to configure connectors, the entire feature cannot be used. It enables users to define integration points in their journeys.

**Independent Test**: Can be tested by creating a journey definition with a SERVICE_TASK state, configuring it with HTTP Connector parameters (method, URL, headers, body), and verifying the configuration is persisted correctly.

**Acceptance Scenarios**:

1. **Given** a journey designer is creating a new journey, **When** they add a SERVICE_TASK state, **Then** they can select "HTTP" as the connector type
2. **Given** a SERVICE_TASK state with HTTP Connector selected, **When** the designer configures method, URL, headers, query parameters, and body, **Then** the configuration is saved and validated
3. **Given** a configured HTTP Connector, **When** the designer uses context variables in the configuration (e.g., `${customerId}`), **Then** the variables are accepted and stored

---

### User Story 2 - Runtime Executes HTTP Connector Automatically (Priority: P1)

As a journey runtime, I want to automatically execute the configured HTTP Connector when a journey instance enters a SERVICE_TASK state, so that external integrations happen without manual intervention.

**Why this priority**: This is the core runtime behavior - automatic execution of connectors is what makes the feature valuable. Without this, connectors would require manual triggering.

**Independent Test**: Can be tested by starting a journey instance that reaches a SERVICE_TASK state with HTTP Connector configuration, and verifying the HTTP request is executed automatically.

**Acceptance Scenarios**:

1. **Given** a journey instance enters a SERVICE_TASK state with HTTP Connector, **When** the runtime processes the state, **Then** the HTTP request is executed with the configured parameters
2. **Given** context variables are used in the HTTP configuration, **When** the connector executes, **Then** the variables are resolved with actual values from the execution context
3. **Given** the HTTP request succeeds, **When** the connector completes, **Then** the journey instance automatically advances to the next state

---

### User Story 3 - Context Enrichment from Connector Results (Priority: P2)

As a journey designer, I want to use the results from HTTP Connector executions in subsequent journey steps, so that data from external systems can flow through the journey.

**Why this priority**: This enables data flow between systems, making journeys more powerful. Without context enrichment, connectors would be fire-and-forget with limited utility.

**Independent Test**: Can be tested by configuring an HTTP Connector that returns data, and verifying that the data is available in the context for subsequent states.

**Acceptance Scenarios**:

1. **Given** an HTTP Connector executes successfully and returns data, **When** the execution completes, **Then** the response data is added to the execution context
2. **Given** the context already contains data, **When** new data is added from a connector result, **Then** existing context data is preserved (merge behavior)
3. **Given** a subsequent state in the journey, **When** it accesses the context, **Then** it can use data added by the previous connector execution

---

### User Story 4 - Audit Trail for Connector Executions (Priority: P2)

As a system administrator, I want to view detailed audit information about connector executions, so that I can troubleshoot issues and monitor integration performance.

**Why this priority**: Observability is critical for production systems. Without audit trails, debugging integration failures would be extremely difficult.

**Independent Test**: Can be tested by executing a journey with a connector, then viewing the execution details and verifying the audit information is present and accurate.

**Acceptance Scenarios**:

1. **Given** a connector execution completes, **When** viewing the execution details, **Then** the audit shows connector type, start time, execution duration, result, and status
2. **Given** a connector execution fails, **When** viewing the execution details, **Then** the audit includes the error message and failure reason
3. **Given** multiple connector executions in a journey, **When** viewing the execution timeline, **Then** each connector execution is listed with its audit information

---

### Edge Cases

- What happens when the HTTP request times out?
- How does the system handle invalid HTTP Connector configuration (missing required fields)?
- What happens when context variables referenced in the HTTP configuration don't exist?
- How does the system handle HTTP errors (4xx, 5xx responses)?
- What happens when the HTTP response body is malformed or cannot be parsed?
- How does the system handle network connectivity issues during connector execution?
- What happens when multiple connectors attempt to update the same context variable?
- How does the system handle very large HTTP response payloads?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST support a new state type called SERVICE_TASK for journey definitions
- **FR-002**: System MUST allow journey designers to configure a SERVICE_TASK with a connector type
- **FR-003**: System MUST provide HTTP Connector as the first connector implementation
- **FR-004**: HTTP Connector MUST support configuration of HTTP method (GET, POST, PUT, DELETE, PATCH)
- **FR-005**: HTTP Connector MUST support configuration of URL
- **FR-006**: HTTP Connector MUST support configuration of HTTP headers
- **FR-007**: HTTP Connector MUST support configuration of query parameters
- **FR-008**: HTTP Connector MUST support configuration of request body
- **FR-009**: HTTP Connector MUST support configuration of timeout duration
- **FR-010**: System MUST allow context variables to be used in HTTP Connector configuration fields
- **FR-011**: System MUST resolve context variables at runtime with actual values from execution context
- **FR-012**: Runtime MUST automatically execute the configured connector when a journey instance enters a SERVICE_TASK state
- **FR-013**: Runtime MUST locate the appropriate connector implementation based on connector type
- **FR-014**: Runtime MUST delegate execution to the connector without knowledge of protocol-specific details
- **FR-015**: Connector MUST be able to read variables from the execution context
- **FR-016**: Connector MUST be able to produce new data from execution results
- **FR-017**: System MUST update the execution context with connector results while preserving existing data
- **FR-018**: Runtime MUST automatically advance to the next state when connector execution succeeds
- **FR-019**: Runtime MUST stop execution when connector execution fails
- **FR-020**: System MUST record audit information for each connector execution
- **FR-021**: Audit information MUST include connector type used
- **FR-022**: Audit information MUST include start and end timestamps
- **FR-023**: Audit information MUST include execution duration
- **FR-024**: Audit information MUST include execution result
- **FR-025**: Audit information MUST include execution status (success/failure)
- **FR-026**: Audit information MUST include error details when execution fails
- **FR-027**: System MUST display audit information in the execution details view
- **FR-028**: System MUST allow new connector types to be added without modifying runtime core logic
- **FR-029**: System MUST validate HTTP Connector configuration before saving

### Key Entities

- **SERVICE_TASK State**: Represents a state in a journey that triggers connector execution. Contains connector type and connector-specific configuration.
- **Connector Configuration**: Stores the parameters needed for a specific connector type to execute (e.g., HTTP method, URL, headers for HTTP Connector).
- **Connector Execution Record**: Represents a single execution of a connector, containing audit information such as timestamps, status, result, and error details.
- **Execution Context**: A key-value data structure that stores data available during journey execution, allowing connectors to read and write data.

## Assumptions

- The journey runtime already has a mechanism to detect when a journey instance enters a state
- The execution context already exists and is used by other journey features
- The Journey Designer UI already exists and can be extended to support new state types
- HTTP requests will be synchronous for this initial implementation
- Context variables use a standard placeholder syntax (e.g., `${variableName}`)
- The system has network access to external HTTP endpoints
- HTTP responses will be in JSON format for context enrichment (assumption for initial implementation)

## Out of Scope

The following features are explicitly out of scope for this delivery and will be addressed in future features:

- Retry mechanisms for failed connector executions
- Circuit breaker patterns for fault tolerance
- Fallback strategies when connectors fail
- Asynchronous connector execution
- Kafka Connector
- RabbitMQ Connector
- Webhook Connector
- gRPC Connector
- GraphQL Connector
- Script Connector
- Human Tasks
- Timer Events

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Journey designers can configure a SERVICE_TASK with HTTP Connector in under 3 minutes
- **SC-002**: Runtime executes HTTP Connector within 500ms of entering a SERVICE_TASK state
- **SC-003**: 100% of successful connector executions automatically advance to the next state
- **SC-004**: 100% of failed connector executions stop journey execution and record error details
- **SC-005**: Context variables are resolved correctly in 100% of connector executions
- **SC-006**: Connector results are available in the execution context for subsequent states in 100% of cases
- **SC-007**: Audit information is recorded for 100% of connector executions
- **SC-008**: Audit information is displayed in execution details within 1 second of request
- **SC-009**: New connector types can be added without modifying runtime core logic
- **SC-010**: HTTP Connector configuration validation prevents 100% of invalid configurations from being saved
