# Research: HTTP Connector Configuration

**Feature**: 010-http-connector-config  
**Date**: 2026-09-10  
**Purpose**: Resolve clarifications and identify best practices for HTTP connector UI implementation

---

## Research Tasks Summary

| #   | Task                                                          | Status      | Finding                                                                           |
| --- | ------------------------------------------------------------- | ----------- | --------------------------------------------------------------------------------- |
| R1  | Backend API response format for connector retrieval           | ✅ RESOLVED | HttpConnectorConfiguration schema fully defined in openapi.yaml                   |
| R2  | UI pattern for dynamic list management (headers/query params) | ✅ RESOLVED | Radix UI + Tailwind pattern established; similar to existing forms                |
| R3  | Form validation and error handling strategy                   | ✅ RESOLVED | Client-side validation before submit; API error handling with toast notifications |
| R4  | Placeholder/context variable support                          | ✅ RESOLVED | URLs and headers can contain ${placeholder} syntax; documented in OpenAPI         |
| R5  | Accessibility requirements for dynamic forms                  | ✅ RESOLVED | WCAG 2.1 AA compliance required; use Radix UI primitives with aria attributes     |
| R6  | State management for complex form operations                  | ✅ RESOLVED | Use Zustand store + React hooks; validate on blur, submit on save                 |

---

## R1: Backend API Response Format for Connector Retrieval

**Question**: How are existing connector configurations retrieved from the backend? What is the exact response schema?

**Decision**: API contract defined in `api-spec/openapi.yaml` under `HttpConnectorConfiguration` schema

- Inherits from `ConnectorConfiguration` (connectorType, timeout)
- Adds HTTP-specific fields: method, url, headers, queryParams, body
- All string fields support placeholder syntax: `${variable}`

**Evidence**: OpenAPI schema shows:

```yaml
HttpConnectorConfiguration:
  allOf:
    - $ref: "#/components/schemas/ConnectorConfiguration"
    - type: object
      required:
        - method
        - url
      properties:
        method: HttpMethod (GET|POST|PUT|DELETE|PATCH)
        url: string (with placeholders)
        headers: object<string, string>
        queryParams: object<string, string>
        body: string (JSON with placeholders)
        timeout: ISO 8601 duration (default PT30S)
```

**Implementation**: Use backend TypeScript API client generated from OpenAPI spec. No additional data transformations needed.

---

## R2: UI Pattern for Dynamic List Management

**Question**: What is the best practice for adding/removing headers and query parameters in the UI?

**Decision**: Implement a reusable `KeyValueList` component that:

- Displays current entries as a list with key and value input fields
- Provides "Add" button to append new empty row
- Each row has "Remove" button (disabled if only one entry)
- Validates key uniqueness (show error if duplicate key)
- Updates parent form state on any change

**Evidence**: Follows existing Radix UI + Tailwind pattern used in project; similar to form array patterns in React Hook Form

**Implementation Pattern**:

```typescript
// Component usage
<KeyValueList
  entries={headers}
  onAdd={() => setHeaders([...headers, { key: '', value: '' }])}
  onRemove={(index) => setHeaders(headers.filter((_, i) => i !== index))}
  onChange={(index, key, value) => updateEntry(index, key, value)}
  label="Headers"
/>
```

---

## R3: Form Validation and Error Handling Strategy

**Question**: How should validation be handled? Client-side validation, server-side validation, or both?

**Decision**: Implement layered validation:

1. **Client-side (before submit)**:
   - Required fields: Method and URL are mandatory
   - URL format validation: Must be valid HTTP/HTTPS URL
   - Header key format: Only alphanumeric, hyphen, underscore allowed
   - JSON validation in Body field: Warn if invalid JSON (but don't block)
   - Timeout format: Must be valid ISO 8601 duration

2. **Server-side (after submit)**:
   - Backend validates full connector configuration
   - Returns detailed error messages for each field

3. **Error presentation**:
   - Inline field errors with red border + helper text
   - Submit button disabled if validation fails
   - Toast notification for API errors with retry option

**Implementation**: Use React Hook Form + Zod schema for validation

---

## R4: Placeholder/Context Variable Support

**Question**: How should users specify dynamic variables in URL, headers, and body?

**Decision**: Document and implement placeholder syntax support:

- Placeholders use `${variableName}` syntax
- Common variables: `${customerId}`, `${userId}`, `${token}`, etc.
- User provides variable names; runtime substitution happens on backend
- UI should not validate variable existence (backend will handle errors)

**Implementation**:

- Add placeholder examples in field helper text
- Consider adding "Insert variable" UI helper (future enhancement)
- Store exactly as entered; backend handles substitution

---

## R5: Accessibility Requirements for Dynamic Forms

**Question**: What accessibility standards must be met for dynamic form controls?

**Decision**: Target WCAG 2.1 AA compliance:

- All form fields must have associated labels
- Keyboard navigation fully supported (Tab, Enter, Space)
- Dynamic list additions/removals must update ARIA attributes
- Error messages linked to fields via aria-describedby
- Use Radix UI primitives (they have built-in accessibility)
- Announce dynamic changes via aria-live regions for screen readers

**Implementation**: Leverage Radix UI Label, Select, and Tooltip components which include ARIA attributes

---

## R6: State Management for Complex Form Operations

**Question**: How should form state be managed for validation, dirty tracking, and save operations?

**Decision**: Hybrid approach:

- **Form state**: React Hook Form (declarative, built-in validation)
- **Global state**: Zustand store for cross-component connector config
- **Validation**: Zod schema matching OpenAPI contract
- **Persistence**: When user saves, dispatch to store → call API → update journey definition

**Rationale**:

- React Hook Form handles form-level concerns (dirty state, validation errors, touched fields)
- Zustand handles global connector config state that needs to persist across component remounts
- Clear separation of concerns

**Implementation Example**:

```typescript
const useConnectorForm = () => {
  const { form, setFormState } = useConnectorStore();
  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(connectorSchema),
    defaultValues: form,
  });

  const onSubmit = async (data) => {
    await connectorService.updateConnectorConfig(data);
    setFormState(data);
  };

  return { control, handleSubmit, errors, onSubmit };
};
```

---

## Conclusion

All research tasks completed. No blockers identified. Technical approach is clear:

- Use backend OpenAPI contract as source of truth
- Implement client-side validation with React Hook Form + Zod
- Build reusable KeyValueList component for dynamic lists
- Manage state with React Hook Form + Zustand
- Ensure WCAG 2.1 AA accessibility with Radix UI primitives
- Handle errors with inline validation + toast notifications
