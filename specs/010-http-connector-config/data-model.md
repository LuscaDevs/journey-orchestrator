# Data Model: HTTP Connector Configuration

**Feature**: 010-http-connector-config  
**Date**: 2026-09-10  
**Source**: Derived from feature spec and backend OpenAPI contract

---

## Domain Entities

### HttpConnectorConfiguration

The core domain object representing HTTP connector settings for a SERVICE_TASK node.

**Responsibilities**:

- Encapsulates all HTTP request configuration
- Validates required fields (method, url)
- Stores dynamic headers and query parameters
- Preserves placeholder variables for runtime substitution

**Fields**:

| Field           | Type                       | Required | Constraints                                                              | Example                                                                    |
| --------------- | -------------------------- | -------- | ------------------------------------------------------------------------ | -------------------------------------------------------------------------- |
| `connectorType` | string (enum)              | ✓        | Always "HTTP" for this feature                                           | "HTTP"                                                                     |
| `method`        | HttpMethod enum            | ✓        | One of: GET, POST, PUT, DELETE, PATCH                                    | "POST"                                                                     |
| `url`           | string                     | ✓        | Valid HTTP/HTTPS URL; may contain ${placeholders}                        | `"https://api.example.com/customers/${customerId}"`                        |
| `headers`       | object<string, string>     | ✗        | Key-value pairs; values may contain ${placeholders}                      | `{"Authorization": "Bearer ${token}", "Content-Type": "application/json"}` |
| `queryParams`   | object<string, string>     | ✗        | Key-value pairs for URL query string; values may contain ${placeholders} | `{"limit": "10", "offset": "${offset}"}`                                   |
| `body`          | string                     | ✗        | JSON string; may contain ${placeholders}                                 | `'{"customerId": "${customerId}", "action": "update"}'`                    |
| `timeout`       | string (ISO 8601 duration) | ✓        | Format: PT[n]H[n]M[n]S (e.g., PT30S)                                     | "PT30S"                                                                    |

**Relationships**:

- **Associated with**: SERVICE_TASK node in journey definition
- **Persisted in**: Journey instance via backend API
- **Referenced by**: ConnectorExecutionRecord (for audit trail)

### HttpMethod

Enumeration of supported HTTP methods.

**Values**: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`

### ConnectorConfigEntry (Header/Query Parameter)

A key-value pair representing a single header or query parameter.

**Fields**:

- `key`: string (alphanumeric, hyphen, underscore)
- `value`: string (may contain ${placeholders})

**Validation**:

- Key is required and must be unique within list
- Key format: Only alphanumeric, hyphen, underscore (matching HTTP header naming standards)
- Value is optional but if present, should be non-empty string

### FormState

Runtime state for the connector configuration form.

**Fields**:

| Field              | Type                   | Purpose                            |
| ------------------ | ---------------------- | ---------------------------------- |
| `method`           | HttpMethod \| null     | Currently selected HTTP method     |
| `url`              | string                 | Current URL input                  |
| `headers`          | ConnectorConfigEntry[] | List of header entries             |
| `queryParams`      | ConnectorConfigEntry[] | List of query parameter entries    |
| `body`             | string                 | Current body input                 |
| `timeout`          | string                 | Current timeout input              |
| `validationErrors` | object<string, string> | Field-level validation errors      |
| `isDirty`          | boolean                | True if form has unsaved changes   |
| `isSaving`         | boolean                | True while API call is in progress |
| `saveError`        | string \| null         | API error message if save failed   |

**State Transitions**:

```bash
Initial → UserEditsForm → ValidateOnBlur → UserClicksSave → CallAPI → Success|Error
  ↓                                                                      ↓
  └──────────────────────────────────────────────────────────────────────┘
```

---

## Validation Rules

### Required Field Validation

- **Method**: Must be selected (not null)
- **URL**: Must not be empty and must be valid URL

### URL Validation

- Must start with `http://` or `https://`
- Must be syntactically valid URL
- Allows placeholder syntax: `${variable}`
- Example valid URLs:
  - `https://api.example.com/endpoint`
  - `https://api.example.com/customers/${customerId}`
  - `http://internal-service:9000/process`

### Header Key Validation

- Must be non-empty string
- Must contain only: alphanumeric, hyphen (-), underscore (\_)
- Must be unique within headers list
- Must not exceed 256 characters

### Query Parameter Key Validation

- Must be non-empty string
- Must contain only: alphanumeric, hyphen (-), underscore (\_), dot (.)
- Must be unique within queryParams list

### Body Validation

- If present, should contain valid JSON or be empty
- Warning (non-blocking) if malformed JSON
- May contain placeholder syntax: `${variable}`

### Timeout Validation

- Must be valid ISO 8601 duration format
- Examples: PT30S, PT1M, PT2M30S, PT1H
- Default: PT30S if not specified
- Must be positive duration

---

## State Serialization

### To Backend (Update Journey Definition)

When saving, the form state is transformed to `HttpConnectorConfiguration` format:

```typescript
// Form state
{
  method: "POST",
  url: "https://api.example.com/customers/${customerId}",
  headers: [
    { key: "Authorization", value: "Bearer ${token}" },
    { key: "Content-Type", value: "application/json" }
  ],
  queryParams: [
    { key: "limit", value: "10" }
  ],
  body: '{"action": "update"}',
  timeout: "PT30S"
}

// Serialized to backend
{
  "connectorType": "HTTP",
  "method": "POST",
  "url": "https://api.example.com/customers/${customerId}",
  "headers": {
    "Authorization": "Bearer ${token}",
    "Content-Type": "application/json"
  },
  "queryParams": {
    "limit": "10"
  },
  "body": "{\"action\": \"update\"}",
  "timeout": "PT30S"
}
```

### From Backend (Load Journey Definition)

When loading an existing journey with connector config:

```typescript
// API response
{
  "connectorType": "HTTP",
  "method": "POST",
  "url": "...",
  "headers": {...},
  "queryParams": {...},
  "body": "...",
  "timeout": "..."
}

// Deserialized to form state
{
  method: "POST",
  url: "...",
  headers: [ { key: "Authorization", value: "..." }, ... ],
  queryParams: [ { key: "limit", value: "10" }, ... ],
  body: "...",
  timeout: "..."
}
```

---

## Edge Cases & Handling

| Scenario                                  | Handling                                                         |
| ----------------------------------------- | ---------------------------------------------------------------- |
| Empty headers list                        | Allowed; results in `"headers": {}` in API call                  |
| Empty query params list                   | Allowed; results in `"queryParams": {}` in API call              |
| Empty body field                          | Allowed; results in `"body": ""` or omitted from API call        |
| URL without protocol                      | Validation error before submit                                   |
| Invalid placeholder syntax `${incomplete` | Stored as-is; backend handles validation at execution time       |
| Very large headers list (100+)            | Allowed but UI may paginate for performance (future enhancement) |
| Duplicate header keys                     | Validation error shown in UI                                     |
| Switching connector types                 | Not applicable for this feature (only HTTP supported)            |
| Concurrent edits from multiple users      | Not handled in this release; last-write-wins via backend         |

---

## Type Definitions (TypeScript)

```typescript
// From OpenAPI-generated types
type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH";

interface HttpConnectorConfiguration {
  connectorType: "HTTP";
  method: HttpMethod;
  url: string;
  headers?: Record<string, string>;
  queryParams?: Record<string, string>;
  body?: string;
  timeout: string; // ISO 8601 duration
}

// UI-specific form state
interface ConnectorFormState {
  method: HttpMethod | null;
  url: string;
  headers: ConnectorConfigEntry[];
  queryParams: ConnectorConfigEntry[];
  body: string;
  timeout: string;
}

interface ConnectorConfigEntry {
  key: string;
  value: string;
}

interface FormValidationErrors {
  method?: string;
  url?: string;
  headers?: Record<number, { key?: string; value?: string }>;
  queryParams?: Record<number, { key?: string; value?: string }>;
  body?: string;
  timeout?: string;
}
```

---

## Related Entities (Context)

### SERVICE_TASK Node

A journey node type that can have a connector configuration. The HTTP connector is executed when this node is reached during journey execution.

**Properties**:

- `type`: "SERVICE_TASK"
- `connectorConfiguration`: HttpConnectorConfiguration (this feature)
- `onSuccess`: Transition to next state
- `onError`: Error handling state

### Journey Definition

Contains SERVICE_TASK nodes, each of which can have HTTP connector configurations. The entire connector config is persisted as part of the journey definition.

### Journey Instance

A runtime execution of a journey. When a SERVICE_TASK with HTTP connector is reached, the connector executes and creates a ConnectorExecutionRecord.

---

## Constraints & Limits

- Maximum URL length: 2000 characters
- Maximum total headers size: 8KB
- Maximum total query parameters size: 2KB
- Maximum body size: 512KB
- Maximum header key length: 256 characters
- Maximum header value length: 2048 characters
- Maximum timeout value: PT24H (24 hours)
- Minimum timeout value: PT1S (1 second)
