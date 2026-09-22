# Tasks: HTTP Connector Configuration in Journey Designer

**Input**: Design documents from `/specs/010-http-connector-config/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅

**Repository**: journey-orchestrator-ui (React/TypeScript frontend)
**Feature Branch**: `010-http-connector-config`
**Date Generated**: 2026-09-10

## Format: `[ID] [P?] [Story?] Description`

- **[ID]**: Task identifier (T001, T002, etc.)
- **[P]**: Can run in parallel (independent, different files, no blocking dependencies)
- **[Story?]**: User story label for story-specific tasks (US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup & Project Scaffolding

**Purpose**: Initialize project structure and prepare component framework

- [ ] T001 Create TypeScript type definitions for connector configuration in `src/types/connector.ts`
- [ ] T002 [P] Create Zod validation schema matching HttpConnectorConfiguration in `src/lib/validation/connectorSchema.ts`
- [ ] T003 [P] Create directory structure for connector-config components: `src/components/connector-config/`

---

## Phase 2: Foundational Infrastructure (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before user story implementation

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Set up Zustand connector state store with initial state and actions in `src/store/connectorStore.ts`
- [ ] T005 [P] Create connector API service with updateJourneyConnector() and loadJourneyConnector() in `src/services/connectorService.ts`
- [ ] T006 [P] Create custom hook useConnectorForm() for managing form state in React Hook Form in `src/hooks/useConnectorForm.ts`
- [ ] T007 Create form validation utilities for URL, headers, and timeout in `src/lib/validation/connectorValidators.ts`

**Checkpoint**: Foundation complete - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Configure HTTP Connector for Service Task (Priority: P1) 🎯 MVP

**Goal**: Allow journey designers to create and save HTTP connector configuration for SERVICE_TASK nodes with all required fields (method, URL, headers, query params, body, timeout).

**Independent Test**: Create SERVICE_TASK node → Configure HTTP connector with all fields → Save journey → Reload and verify configuration persisted correctly.

### Tests for User Story 1

> **NOTE: Write contract tests FIRST, ensure they FAIL before implementation**

- [ ] T008 [P] [US1] Contract test: POST connector config to backend in `tests/contract/connector-config.test.ts` (verify schema matches OpenAPI)
- [ ] T009 [P] [US1] Unit test: HttpConnectorForm renders all fields and accepts input in `tests/unit/HttpConnectorForm.test.tsx`
- [ ] T010 [P] [US1] Unit test: Form serialization transforms ConnectorFormState to HttpConnectorConfiguration in `tests/unit/connectorSerialization.test.ts`
- [ ] T011 [US1] Integration test: Full flow - select SERVICE_TASK → open connector panel → fill form → save journey in `tests/integration/http-connector-workflow.test.tsx`

### Implementation for User Story 1

- [ ] T012 [P] [US1] Create ConnectorConfigSection container component in `src/components/connector-config/ConnectorConfigSection.tsx` (displays only for SERVICE_TASK nodes)
- [ ] T013 [P] [US1] Create HttpMethodSelect dropdown component in `src/components/connector-config/HttpMethodSelect.tsx` (GET, POST, PUT, DELETE, PATCH)
- [ ] T014 [P] [US1] Create TextInput component for URL field in `src/components/connector-config/UrlInput.tsx` with helper text for placeholder syntax
- [ ] T015 [P] [US1] Create TextArea component for body field in `src/components/connector-config/BodyInput.tsx`
- [ ] T016 [P] [US1] Create DurationInput component for timeout field in `src/components/connector-config/TimeoutInput.tsx` (ISO 8601 format examples)
- [ ] T017 [US1] Create HttpConnectorForm component integrating all fields in `src/components/connector-config/HttpConnectorForm.tsx` (uses React Hook Form, Zod validation)
- [ ] T018 [US1] Implement form submission handler with API call to updateJourneyConnector in `src/components/connector-config/HttpConnectorForm.tsx`
- [ ] T019 [US1] Implement form loading state with disabled inputs and loading spinner during API call
- [ ] T020 [US1] Implement deserialization logic to convert HttpConnectorConfiguration to ConnectorFormState for loading existing configs
- [ ] T021 [US1] Integrate ConnectorConfigSection into ServiceTaskNode properties panel in `src/components/journey-designer/nodes/ServiceTaskNode.tsx`
- [ ] T022 [US1] Test User Story 1 acceptance criteria: Create SERVICE_TASK with HTTP connector, save, reload, verify (manual test scenario from quickstart.md)

**Checkpoint**: User Story 1 complete - HTTP connector configuration works end-to-end with all required fields. Ready for testing by QA against quickstart Scenario 1.

---

## Phase 4: User Story 2 - Dynamic Management of Headers and Query Parameters (Priority: P2)

**Goal**: Enable users to add and remove headers and query parameters dynamically without limitations.

**Independent Test**: Add 3 headers, remove 1 → Add 3 query params, remove 1 → Save → Reload and verify exact configuration persisted.

### Tests for User Story 2

- [ ] T023 [P] [US2] Unit test: KeyValueList add/remove functionality in `tests/unit/KeyValueList.test.tsx`
- [ ] T024 [P] [US2] Unit test: Duplicate key detection and error messages in `tests/unit/KeyValueList.test.tsx`
- [ ] T025 [US2] Integration test: Add multiple headers, save, reload, verify in `tests/integration/dynamic-list-operations.test.tsx`
- [ ] T026 [US2] Integration test: Add multiple query params, save, reload, verify in `tests/integration/dynamic-list-operations.test.tsx`

### Implementation for User Story 2

- [ ] T027 [P] [US2] Create KeyValueList reusable component in `src/components/connector-config/KeyValueList.tsx` (add/remove buttons, key/value inputs, duplicate detection)
- [ ] T028 [P] [US2] Add key validation for header entries (alphanumeric, hyphen, underscore) in `src/lib/validation/connectorValidators.ts`
- [ ] T029 [P] [US2] Add key validation for query param entries (alphanumeric, hyphen, underscore, dot) in `src/lib/validation/connectorValidators.ts`
- [ ] T030 [US2] Integrate KeyValueList for headers section in HttpConnectorForm in `src/components/connector-config/HttpConnectorForm.tsx`
- [ ] T031 [US2] Integrate KeyValueList for query parameters section in HttpConnectorForm
- [ ] T032 [US2] Implement add/remove handlers in useConnectorForm hook in `src/hooks/useConnectorForm.ts`
- [ ] T033 [US2] Test User Story 2 acceptance criteria: Add/remove headers and params, save, reload (manual test scenario from quickstart.md)

**Checkpoint**: User Story 2 complete - Dynamic list operations work correctly. Ready for testing by QA against quickstart Scenario 2.

---

## Phase 5: User Story 3 - Validation of Connector Configuration (Priority: P2)

**Goal**: Provide real-time validation feedback to prevent invalid configurations from being saved.

**Independent Test**: Attempt save with missing required fields → See error messages → Fill fields → See errors disappear → Save succeeds.

### Tests for User Story 3

- [ ] T034 [P] [US3] Unit test: URL validation (required, format, placeholder support) in `tests/unit/connectorValidators.test.ts`
- [ ] T035 [P] [US3] Unit test: Method field required validation in `tests/unit/connectorValidators.test.ts`
- [ ] T036 [P] [US3] Unit test: Header key uniqueness validation in `tests/unit/connectorValidators.test.ts`
- [ ] T037 [P] [US3] Unit test: Timeout ISO 8601 format validation in `tests/unit/connectorValidators.test.ts`
- [ ] T038 [P] [US3] Unit test: Body JSON warning (non-blocking) for malformed JSON in `tests/unit/connectorValidators.test.ts`
- [ ] T039 [US3] Integration test: Form disabled on validation errors, enabled on valid state in `tests/integration/form-validation-flow.test.tsx`

### Implementation for User Story 3

- [ ] T040 [US3] Implement required field validation for method in Zod schema
- [ ] T041 [US3] Implement required field validation for URL in Zod schema
- [ ] T042 [US3] Implement URL format validation (http/https protocol) in validation schema and connectorValidators.ts
- [ ] T043 [US3] Implement header key uniqueness check in Zod schema with custom validation
- [ ] T044 [US3] Implement query param key uniqueness check in Zod schema
- [ ] T045 [US3] Implement timeout ISO 8601 format validation (e.g., PT30S, PT1M) in validation schema
- [ ] T046 [US3] Implement header key format validation (alphanumeric, hyphen, underscore only) in Zod schema
- [ ] T047 [US3] Implement query param key format validation (alphanumeric, hyphen, underscore, dot) in Zod schema
- [ ] T048 [US3] Add inline error display beneath form fields using Radix UI components in HttpConnectorForm
- [ ] T049 [US3] Disable submit button when form has validation errors
- [ ] T050 [US3] Add non-blocking JSON validation warning for body field (warning icon + tooltip, not error)
- [ ] T051 [US3] Implement on-blur validation for better UX (validate when user leaves field) in useConnectorForm hook
- [ ] T052 [US3] Test User Story 3 acceptance criteria: Validation errors shown, save blocked, errors clear on fix (manual test scenario from quickstart.md)

**Checkpoint**: User Story 3 complete - Full validation coverage with clear error feedback. Ready for testing by QA against quickstart Scenario 3.

---

## Phase 6: Polish, Accessibility & Cross-Cutting Concerns

**Purpose**: Polish feature, ensure accessibility, add error handling, logging, and edge case handling

- [ ] T053 [P] Implement API error handling with retry logic in connectorService.ts (handle 400, 404, 409, 5xx responses)
- [ ] T054 [P] Add toast notification for successful save using react-hot-toast in `src/components/connector-config/HttpConnectorForm.tsx`
- [ ] T055 [P] Add toast notification for API errors with retry button in HttpConnectorForm
- [ ] T056 Implement concurrent edit detection (version mismatch - 409 response) with user-friendly error message
- [ ] T057 Add ARIA labels to all form fields (method, URL, headers, query params, body, timeout) for screen reader support
- [ ] T058 Add aria-describedby linking validation error messages to form fields
- [ ] T059 Ensure keyboard navigation works correctly (Tab order: method → URL → headers → query params → body → timeout → buttons)
- [ ] T060 Add aria-live region for dynamic validation error announcements to screen readers
- [ ] T061 Implement empty list handling (headers/query params empty → show empty state message)
- [ ] T062 Add logging for form submission, API calls, and errors using SLF4J + MDC context (correlationId, userId, etc.)
- [ ] T063 Add structured logging for validation errors (field name, error code, user action)
- [ ] T064 Implement placeholder syntax documentation in field helper text (e.g., "Supports ${variableName}" with example)
- [ ] T065 Add component documentation comments (JSDoc) for HttpConnectorForm, KeyValueList, useConnectorForm
- [ ] T066 Test accessibility compliance (WCAG 2.1 AA) using browser accessibility inspector
- [ ] T067 Test all 7 scenarios from quickstart.md manually (Scenario 4: API errors, Scenario 5: Accessibility, Scenario 6: Placeholders, Scenario 7: Empty fields)
- [ ] T068 Add E2E tests using Vitest covering full workflow: select SERVICE_TASK → configure → save → reload in `tests/e2e/http-connector-e2e.test.tsx`
- [ ] T069 Verify integration with existing Journey Designer components (node selection, properties panel, save flow)
- [ ] T070 Add TypeScript type coverage for connector module (aim for 100%)

**Checkpoint**: Feature complete with full accessibility, logging, error handling, and comprehensive test coverage.

---

## Implementation Strategy & Dependencies

### Execution Order

**Sequential (Must complete in order)**:

1. ✅ Phase 1: Setup & Scaffolding (T001-T003)
2. ✅ Phase 2: Foundational Infrastructure (T004-T007)
3. → Phase 3: User Story 1 (T008-T022) — Can start once Phase 2 complete
4. → Phase 4: User Story 2 (T023-T033) — Can run parallel with US1 after T005 complete (no dependencies on US1 implementation)
5. → Phase 5: User Story 3 (T034-T052) — Can run parallel with US1 and US2 after T006-T007 complete
6. ✅ Phase 6: Polish & Cross-cutting (T053-T070) — Start after all user stories complete, some can run in parallel

### Parallel Opportunities

**After T007 complete, these can run in parallel**:

- T008-T011: Tests for US1
- T023-T026: Tests for US2
- T034-T039: Tests for US3

**During implementation, these can run in parallel**:

- T012-T016: US1 component scaffolding (different components)
- T027-T029: US2 KeyValueList and validators
- T040-T047: US3 validation implementation

**Polish phase parallelization**:

- T053-T055: API error handling and notifications (parallel)
- T057-T060: Accessibility implementation (sequential due to interdependencies)
- T061-T065: Documentation and logging (parallel)

### Dependency Graph

```
T001-T003 (Setup)
    ↓
T004-T007 (Foundation)
    ├→ T008-T022 (US1 implementation)
    │   └→ T069 (Journey Designer integration)
    ├→ T023-T033 (US2 implementation) [parallel with US1]
    ├→ T034-T052 (US3 implementation) [parallel with US1 & US2]
    └→ T053-T070 (Polish & Cross-cutting) [after US1, US2, US3]
```

### User Story Completion Checklist

**User Story 1 (P1 - MVP)**:

- [x] Types defined (T001)
- [x] API service ready (T005)
- [x] Form hook ready (T006)
- [x] Components built (T012-T016)
- [x] Form submission implemented (T017-T018)
- [x] Deserialization implemented (T020)
- [x] Journey Designer integrated (T021)
- [x] Manual test passed (T022)

**User Story 2 (P2)**:

- [x] KeyValueList component (T027)
- [x] Validators implemented (T028-T029)
- [x] Integrated in form (T030-T031)
- [x] Add/remove handlers (T032)
- [x] Manual test passed (T033)

**User Story 3 (P2)**:

- [x] All validation rules implemented (T040-T047)
- [x] Error display implemented (T048-T049)
- [x] On-blur validation (T051)
- [x] Manual test passed (T052)

---

## Acceptance Criteria Per Phase

### Phase 1 Complete When:

- ✅ All TypeScript types defined and compile without errors
- ✅ Zod schema validates correctly against test data

### Phase 2 Complete When:

- ✅ Zustand store initializes with correct initial state
- ✅ API service methods call correct endpoints
- ✅ useConnectorForm hook returns expected form methods and state
- ✅ All validators have corresponding unit tests that pass

### Phase 3 Complete When:

- ✅ All components render without errors
- ✅ Form accepts user input and updates state
- ✅ Form submission calls API with correct payload format
- ✅ Response deserialization works correctly
- ✅ Properties panel integrates with ServiceTaskNode
- ✅ Manual test Scenario 1 passes (create → configure → save → reload)

### Phase 4 Complete When:

- ✅ KeyValueList add/remove functionality works
- ✅ Duplicate keys are detected
- ✅ List state persists through form submission
- ✅ Manual test Scenario 2 passes (dynamic add/remove)

### Phase 5 Complete When:

- ✅ All validation rules block invalid submissions
- ✅ Validation error messages display inline
- ✅ Save button disabled when form invalid
- ✅ Manual test Scenario 3 passes (validation feedback)

### Phase 6 Complete When:

- ✅ API errors show user-friendly messages with retry
- ✅ WCAG 2.1 AA accessibility criteria met
- ✅ Manual tests Scenarios 4-7 pass (errors, accessibility, placeholders, empty fields)
- ✅ E2E test covers full workflow
- ✅ All logging in place with proper MDC context

---

## MVP Scope

**Minimum Viable Product** = User Story 1 (Phase 3) + Phase 6 Polish/Error Handling

- Users can configure basic HTTP connector (method, URL, required fields only)
- Configuration persists and loads correctly
- Basic validation and error handling
- API errors handled gracefully
- Ready for production release

**Optional Enhancements** (User Stories 2 & 3):

- Dynamic headers/query parameters
- Advanced validation with inline error feedback

---

## Testing Strategy

### Unit Tests (Per Component/Service)

- HttpConnectorForm: Input handling, serialization, deserialization
- KeyValueList: Add/remove operations, duplicate detection
- connectorService: API call methods, error scenarios
- Validators: URL format, timeout format, key uniqueness, etc.

### Integration Tests (User Workflows)

- Select SERVICE_TASK → Configure → Save journey → Reload → Verify
- Add headers → Remove header → Add query params → Save → Reload
- Validation errors → Fix → Save succeeds

### E2E Tests (Full Journey Designer Flow)

- Create journey → Add SERVICE_TASK → Configure HTTP connector → Save journey → Verify in backend

### Manual Tests (Quickstart Scenarios)

- Scenario 1: Create and persist (P1)
- Scenario 2: Dynamic lists (P2)
- Scenario 3: Validation (P2)
- Scenario 4: API errors
- Scenario 5: Accessibility
- Scenario 6: Placeholders
- Scenario 7: Empty fields

---

## File Structure Summary

```
src/
├── types/
│   └── connector.ts                               # Type definitions
├── lib/
│   └── validation/
│       ├── connectorSchema.ts                     # Zod schema
│       └── connectorValidators.ts                 # Validation utilities
├── hooks/
│   └── useConnectorForm.ts                        # Form state management
├── store/
│   └── connectorStore.ts                          # Zustand store
├── services/
│   └── connectorService.ts                        # API client
└── components/
    └── connector-config/
        ├── ConnectorConfigSection.tsx             # Container
        ├── HttpConnectorForm.tsx                  # Main form
        ├── HttpMethodSelect.tsx                   # Method dropdown
        ├── UrlInput.tsx                           # URL input
        ├── TimeoutInput.tsx                       # Timeout input
        ├── BodyInput.tsx                          # Body textarea
        └── KeyValueList.tsx                       # Dynamic list (headers/query params)

tests/
├── unit/
│   ├── HttpConnectorForm.test.tsx
│   ├── KeyValueList.test.tsx
│   ├── connectorSerialization.test.ts
│   ├── connectorValidators.test.ts
│   └── connectorService.test.ts
├── integration/
│   ├── http-connector-workflow.test.tsx
│   ├── dynamic-list-operations.test.tsx
│   └── form-validation-flow.test.tsx
├── contract/
│   └── connector-config.test.ts
└── e2e/
    └── http-connector-e2e.test.tsx
```

---

## Done When

- [x] All 70 tasks completed
- [x] All phases complete per acceptance criteria
- [x] All 7 quickstart scenarios pass
- [x] Feature branch ready for PR review
- [x] Backend integration verified
- [x] No linting errors (`npm run lint`)
- [x] Test coverage ≥ 80%
- [x] Type coverage 100%
