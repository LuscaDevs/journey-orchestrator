# Quickstart: HTTP Connector Configuration

**Feature**: 010-http-connector-config  
**Date**: 2026-09-10  
**Purpose**: Validation scenarios proving end-to-end feature functionality

---

## Prerequisites

- **Frontend**: Journey Orchestrator UI running (dev or built)
- **Backend**: Journey Orchestrator backend running with MongoDB
- **Test Data**: Sample journey definition(s) available
- **Browser**: Modern browser with developer console access

---

## Scenario 1: Create HTTP Connector Configuration (P1)

**Goal**: Verify user can create and save HTTP connector configuration for a SERVICE_TASK node

**Setup**:

1. Start Journey Orchestrator UI
2. Open or create a new journey definition
3. Ensure journey has at least one SERVICE_TASK node

**Steps**:

1. **Select SERVICE_TASK Node**
   - In journey designer canvas, click on a SERVICE_TASK node
   - Expected: Properties panel appears on the right side

2. **Open Connector Configuration Section**
   - In properties panel, locate "Connector Configuration" section
   - Expected: Section is visible with "Connector Type" dropdown showing "HTTP" as selected

3. **Fill HTTP Connector Fields**
   - **Method**: Select "POST" from dropdown
   - **URL**: Enter `https://api.example.com/customers/${customerId}`
   - **Headers**: Add two entries:
     - Key: `Authorization`, Value: `Bearer ${token}`
     - Key: `Content-Type`, Value: `application/json`
   - **Query Parameters**: Add one entry:
     - Key: `limit`, Value: `10`
   - **Body**: Enter `{"action": "update", "data": "${data}"}`
   - **Timeout**: Verify default `PT30S` is shown

4. **Save Journey**
   - Click "Save" or "Update" button
   - Expected: Loading state appears briefly
   - Expected: Toast notification shows "Journey definition updated successfully" (or similar)

5. **Verify Configuration Persisted**
   - Reload the page (F5)
   - Navigate back to the same journey
   - Click the SERVICE_TASK node again
   - Expected: All entered values appear exactly as saved:
     - Method: POST
     - URL: `https://api.example.com/customers/${customerId}`
     - Headers: Both entries present with correct values
     - Query Parameters: limit=10
     - Body: `{"action": "update", "data": "${data}"}`
     - Timeout: PT30S

**Acceptance Criteria**: ✅

- [ ] Connector configuration section visible when SERVICE_TASK selected
- [ ] All form fields accept and display entered values
- [ ] Save operation completes without errors
- [ ] Configuration persists across page reload
- [ ] No data loss (100% retrieval accuracy)

---

## Scenario 2: Dynamic List Operations (P2)

**Goal**: Verify user can add and remove headers and query parameters dynamically

**Setup**:

- Complete Scenario 1 OR open an existing journey with SERVICE_TASK

**Steps**:

1. **Add Multiple Headers**
   - Open Connector Configuration for SERVICE_TASK
   - Click "Add Header" button multiple times (total 3 headers)
   - Fill each header:
     - Header 1: `Authorization: Bearer ${token}`
     - Header 2: `Content-Type: application/json`
     - Header 3: `X-Custom-Header: ${customHeader}`
   - Expected: Each new row appears below previous entry
   - Expected: All 3 header rows are visible

2. **Remove a Header**
   - Click "Remove" button on Header 2
   - Expected: Header 2 row disappears
   - Expected: Headers 1 and 3 remain with values intact
   - Expected: Remaining headers are relabeled automatically (no gaps)

3. **Add Multiple Query Parameters**
   - In Query Parameters section, click "Add" multiple times (total 3 params)
   - Fill each parameter:
     - Param 1: `limit: 10`
     - Param 2: `offset: ${offset}`
     - Param 3: `sort: name`
   - Expected: All 3 parameter rows visible

4. **Remove a Query Parameter**
   - Click "Remove" on Param 2
   - Expected: Param 2 row disappears
   - Expected: Params 1 and 3 remain intact

5. **Save and Verify**
   - Click Save
   - Expected: Save completes successfully
   - Reload page and verify final configuration:
     - Headers: 2 entries (Authorization, X-Custom-Header, and Content-Type)
     - Query Parameters: 2 entries (limit, sort)

**Acceptance Criteria**: ✅

- [ ] Add button creates new empty row
- [ ] Remove button deletes row (not just clears it)
- [ ] Multiple add/remove operations work correctly
- [ ] Final configuration saved correctly
- [ ] No orphaned or duplicate entries after reload

---

## Scenario 3: Validation Feedback (P2)

**Goal**: Verify form validation prevents invalid submissions and shows helpful errors

**Setup**:

- Open Connector Configuration for SERVICE_TASK

**Steps**:

1. **Test Required Field Validation**
   - Clear the **Method** field (if not dropdown, leave blank)
   - Expected: Required field indicator visible or error message shown
   - Clear the **URL** field completely
   - Click outside URL field (blur event)
   - Expected: Validation error appears: "URL is required" or similar
   - Expected: URL field has visual error state (red border)

2. **Test URL Format Validation**
   - Enter URL: `not-a-valid-url` (without protocol)
   - Click outside URL field
   - Expected: Validation error appears: "Invalid URL format" or similar
   - Expected: Save button is disabled (cannot submit invalid form)

3. **Test Valid URL**
   - Enter URL: `https://api.example.com/endpoint`
   - Click outside URL field
   - Expected: Validation error disappears
   - Expected: Field returns to normal state

4. **Test Duplicate Header Keys**
   - Add two headers with same key: `X-Header: value1` and `X-Header: value2`
   - Expected: Error appears on second entry: "Duplicate header key" or similar
   - Expected: Cannot save while duplicate exists

5. **Test Invalid JSON in Body (Warning)**
   - Enter body: `{invalid json`
   - Expected: Warning icon or message appears (non-blocking)
   - Expected: User can still save (JSON is validated at runtime)

6. **Test Timeout Format**
   - Enter timeout: `30` (without ISO 8601 format)
   - Click outside timeout field
   - Expected: Validation error: "Use ISO 8601 format (e.g., PT30S)" or similar

7. **Valid Timeout**
   - Enter timeout: `PT1M30S`
   - Click outside timeout field
   - Expected: No validation error
   - Expected: Value accepted

**Acceptance Criteria**: ✅

- [ ] Required fields show errors when empty
- [ ] URL format validation rejects invalid URLs
- [ ] Duplicate header keys are detected
- [ ] Invalid JSON in body shows warning (not error)
- [ ] Timeout must be valid ISO 8601 format
- [ ] Save button disabled when validation errors exist
- [ ] Save button enabled when form valid

---

## Scenario 4: API Error Handling

**Goal**: Verify graceful handling of API errors during save

**Setup**:

- Configure a valid HTTP connector
- Have ability to simulate backend unavailability (mock or stop backend)

**Steps**:

1. **Simulate Backend Unavailability**
   - Temporarily stop Journey Orchestrator backend service
   - OR use browser network throttling to simulate timeout

2. **Attempt to Save**
   - Make a configuration change (e.g., change method)
   - Click Save
   - Expected: Loading state appears
   - Expected: After timeout, error toast notification appears
   - Expected: Error message is user-friendly (not raw stack trace)

3. **Retry After Recovery**
   - Backend service comes back online
   - Click "Retry" button (if available) or save again
   - Expected: Save operation succeeds
   - Expected: Configuration is persisted

**Acceptance Criteria**: ✅

- [ ] API errors show user-friendly error messages
- [ ] No raw error codes or stack traces displayed
- [ ] User can retry operation after error
- [ ] Form state is preserved during error (no data loss)

---

## Scenario 5: Accessibility (Browser DevTools)

**Goal**: Verify basic accessibility requirements are met

**Setup**:

- Open browser Developer Tools (F12)
- Open Accessibility inspector/tree

**Steps**:

1. **Keyboard Navigation**
   - Close browser DevTools
   - With keyboard only (no mouse), navigate through form:
     - Press Tab to move to first field (Method dropdown)
     - Tab through all form fields
     - Verify logical tab order (left to right, top to bottom)
   - Expected: All fields are keyboard accessible
   - Expected: No fields skipped when tabbing

2. **Form Labels**
   - Open DevTools → Inspector
   - Click on each input field
   - Expected: Each input has associated `<label>` element
   - Expected: Label text is clear and descriptive (e.g., "Request Method", "Request URL")

3. **Error Announcements**
   - Trigger a validation error (e.g., leave URL empty)
   - With screen reader simulation or ARIA inspection, verify:
   - Expected: Error message is announced/discoverable
   - Expected: Error message is linked to form field via `aria-describedby`

**Acceptance Criteria**: ✅

- [ ] All form fields keyboard navigable (no tabindex traps)
- [ ] Logical tab order maintained
- [ ] All inputs have associated labels
- [ ] Validation error messages discoverable/announced
- [ ] ARIA attributes present (aria-required, aria-invalid, aria-describedby)

---

## Scenario 6: Placeholder Variables

**Goal**: Verify placeholder syntax is supported and stored correctly

**Setup**:

- Open Connector Configuration form

**Steps**:

1. **Enter Placeholder in URL**
   - URL: `https://api.example.com/orders/${orderId}/status`
   - Expected: Text accepted as-is
   - Expected: No validation error (validation happens at runtime)

2. **Enter Multiple Placeholders**
   - Headers: `Authorization: Bearer ${token}`
   - Headers: `X-User-Id: ${userId}`
   - Body: `{"orderId": "${orderId}", "action": "${action}"}`
   - Expected: All placeholders accepted

3. **Save and Retrieve**
   - Save configuration
   - Reload page
   - Expected: All placeholders preserved exactly as entered:
     - URL: `.../${orderId}/status`
     - Headers: `Bearer ${token}`, `${userId}`
     - Body: `...${orderId}...${action}...`

**Acceptance Criteria**: ✅

- [ ] Placeholder syntax `${variableName}` accepted without validation
- [ ] Multiple placeholders in single field supported
- [ ] Placeholders preserved in storage (not altered/escaped)
- [ ] Retrieved placeholders match exactly what was entered

---

## Scenario 7: Edge Case - Empty Optional Fields

**Goal**: Verify optional fields can be empty without blocking save

**Setup**:

- Open Connector Configuration form

**Steps**:

1. **Fill Only Required Fields**
   - Method: POST
   - URL: `https://api.example.com/endpoint`
   - Leave headers empty (no entries)
   - Leave query parameters empty
   - Leave body empty
   - Timeout: PT30S (default)

2. **Save**
   - Click Save
   - Expected: No validation error
   - Expected: Save succeeds

3. **Verify Empty Fields Handled**
   - Reload and check configuration
   - Expected: Method and URL are present
   - Expected: Headers, query params, body are empty (or show as empty collections)
   - Expected: Configuration is valid and can be executed

**Acceptance Criteria**: ✅

- [ ] Optional fields can be left empty
- [ ] Form saves successfully with minimal required fields
- [ ] Empty optional fields retrieved correctly from backend

---

## Test Data Reference

### Example HTTP Connectors

**Simple GET Request**:

```
Method: GET
URL: https://api.example.com/customers
Headers: (none)
Query Params: limit=10
Body: (empty)
Timeout: PT30S
```

**POST with Authentication**:

```
Method: POST
URL: https://api.example.com/customers
Headers:
  - Authorization: Bearer ${token}
  - Content-Type: application/json
Query Params: (none)
Body: {"name": "John", "email": "${email}"}
Timeout: PT30S
```

**Complex Request with Placeholders**:

```
Method: PUT
URL: https://api.example.com/customers/${customerId}/orders/${orderId}
Headers:
  - Authorization: Bearer ${token}
  - X-Request-Id: ${requestId}
  - Content-Type: application/json
Query Params:
  - priority: high
  - timestamp: ${timestamp}
Body: {"status": "${status}", "notes": "${notes}"}
Timeout: PT1M
```

---

## Completion Checklist

- [ ] Scenario 1: Create and persist configuration ✅
- [ ] Scenario 2: Dynamic add/remove operations ✅
- [ ] Scenario 3: Validation feedback ✅
- [ ] Scenario 4: API error handling ✅
- [ ] Scenario 5: Accessibility basics ✅
- [ ] Scenario 6: Placeholder variables ✅
- [ ] Scenario 7: Empty optional fields ✅
- [ ] No console errors during any scenario
- [ ] No network errors during API calls
- [ ] All user stories from spec satisfied
- [ ] Ready for implementation task generation

---

## Next Steps

Once all scenarios pass:

1. Run `/speckit.tasks` to generate implementation tasks
2. Follow task list to implement feature
3. Use scenarios as acceptance test criteria
4. Consider recording these scenarios as automated E2E tests
