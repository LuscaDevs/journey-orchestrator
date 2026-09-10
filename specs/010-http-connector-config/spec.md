# Feature Specification: HTTP Connector Configuration in Journey Designer

**Feature Branch**: `010-http-connector-config`  
**Created**: 2026-06-28  
**Status**: Draft  
**Input**: User description: "Feature: Configuração de Connectors no Journey Designer - Adicionar suporte à configuração de Service Tasks no Journey Designer, permitindo que o usuário configure um HTTP Connector para uma etapa da jornada. Esta entrega deve consumir a API já implementada no backend."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Configure HTTP Connector for Service Task (Priority: P1)

As a journey designer, I want to configure an HTTP connector for a SERVICE_TASK node so that the journey can execute external HTTP requests during runtime.

**Why this priority**: This is the core functionality that enables the entire feature - without it, users cannot configure connectors at all.

**Independent Test**: Can be fully tested by creating a SERVICE_TASK node, configuring an HTTP connector with all fields, saving the journey, and verifying the configuration is persisted and retrievable via the backend API.

**Acceptance Scenarios**:

1. **Given** a journey designer with an open journey, **When** I select a SERVICE_TASK node, **Then** the properties panel displays a Connector section
2. **Given** the Connector section is visible, **When** I select HTTP as the connector type, **Then** configuration fields for Method, URL, Headers, Query Parameters, Body, and Timeout are displayed
3. **Given** I have filled in all required HTTP connector fields, **When** I save the journey, **Then** the configuration is persisted via the backend API
4. **Given** a journey with a configured HTTP connector, **When** I reopen the journey, **Then** the connector configuration is loaded and displayed correctly in the properties panel

---

### User Story 2 - Dynamic Management of Headers and Query Parameters (Priority: P2)

As a journey designer, I want to dynamically add and remove headers and query parameters so that I can configure complex HTTP requests without limitations.

**Why this priority**: This enhances usability and flexibility but is not required for basic HTTP connector functionality.

**Independent Test**: Can be fully tested by adding multiple headers and query parameters, removing some, and verifying the final configuration matches the expected structure.

**Acceptance Scenarios**:

1. **Given** the HTTP connector configuration panel is open, **When** I click "Add Header", **Then** a new key/value pair row is added to the headers list
2. **Given** I have multiple headers configured, **When** I click "Remove" on a header row, **Then** that header is removed from the configuration
3. **Given** I have configured headers and query parameters, **When** I save the journey, **Then** the payload sent to the backend API includes all configured headers and query parameters in the correct format

---

### User Story 3 - Validation of Connector Configuration (Priority: P2)

As a journey designer, I want to receive validation feedback when configuring connectors so that I can identify and fix configuration errors before saving.

**Why this priority**: This prevents invalid configurations from being saved and improves user experience, but basic functionality can work without it.

**Independent Test**: Can be fully tested by attempting to save a connector configuration with missing required fields and verifying appropriate validation messages are displayed.

**Acceptance Scenarios**:

1. **Given** the HTTP connector configuration panel is open, **When** I attempt to save without filling required fields (Method, URL), **Then** validation errors are displayed indicating which fields are required
2. **Given** I have entered an invalid URL format, **When** I navigate away from the URL field, **Then** a validation error is displayed indicating the URL format is invalid
3. **Given** all required fields are filled with valid values, **When** I save the journey, **Then** the save succeeds without validation errors

---

### Edge Cases

- What happens when the backend API is unavailable during save?
- How does the system handle invalid JSON in the Body field?
- What happens when a user switches between connector types (e.g., HTTP to a future type)?
- How does the system handle very large numbers of headers or query parameters?
- What happens when the journey definition version changes between save and load?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display a Connector configuration section in the properties panel when a SERVICE_TASK node is selected
- **FR-002**: System MUST allow selection of connector type (initially only HTTP available)
- **FR-003**: System MUST provide configuration fields for HTTP connector: Method (dropdown), URL (text input), Headers (dynamic key/value list), Query Parameters (dynamic key/value list), Body (text area for JSON), Timeout (numeric input)
- **FR-004**: System MUST validate required fields (Method, URL) before allowing save
- **FR-005**: System MUST allow dynamic addition and removal of header and query parameter entries
- **FR-006**: System MUST serialize connector configuration to the format expected by the backend API
- **FR-007**: System MUST deserialize connector configuration from the backend API response and populate the form fields
- **FR-008**: System MUST hide connector configuration section for non-SERVICE_TASK node types
- **FR-009**: System MUST use existing UI components from the project where applicable
- **FR-010**: System MUST maintain the visual design pattern of the Journey Designer

### Key Entities

- **Service Task Node**: A journey node type that represents an external service call, containing connector configuration
- **HTTP Connector Configuration**: Configuration data including method, URL, headers, query parameters, body, and timeout
- **Header Entry**: A key/value pair representing an HTTP header
- **Query Parameter Entry**: A key/value pair representing a URL query parameter

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create a SERVICE_TASK node and configure an HTTP connector in under 2 minutes
- **SC-002**: 100% of configured connector configurations are successfully persisted and retrieved without data loss
- **SC-003**: 95% of users successfully configure a valid HTTP connector on their first attempt (based on validation feedback)
- **SC-004**: The UI structure allows adding a new connector type with less than 4 hours of development effort

## Assumptions

- The backend API for connector configuration is already implemented and stable
- The Journey Designer has an existing properties panel system that can be extended
- Existing UI components (dropdowns, text inputs, key/value lists) are available for reuse
- The backend API contract matches the domain model defined in the backend implementation

## Out of Scope

- Testing HTTP requests directly from the frontend
- Retry logic configuration
- Circuit breaker configuration
- Connector types other than HTTP
- Human Tasks configuration
- Timer Events configuration
- Real-time validation of HTTP endpoint availability
