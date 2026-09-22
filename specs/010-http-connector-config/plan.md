# Implementation Plan: HTTP Connector Configuration

**Branch**: `010-http-connector-config` | **Date**: 2026-09-10 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/010-http-connector-config/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Enable journey designers to configure HTTP connectors for SERVICE_TASK nodes in the Journey Designer UI. The feature consumes the existing backend HTTP Connector API to allow users to specify request method, URL, headers, query parameters, body content, and timeout values with validation feedback and dynamic parameter management.

## Technical Context

**Language/Version**: TypeScript 5.x / React 19  
**Primary Dependencies**: React, Vite, Zustand, Tailwind CSS, Radix UI, ReactFlow  
**Storage**: MongoDB (via Journey Orchestrator backend API)  
**Testing**: Vitest + React Testing Library  
**Target Platform**: Browser (Modern web, ES2020+)  
**Project Type**: Web UI feature for Journey Designer  
**Performance Goals**: Form load/save < 1s, dynamic list operations < 500ms  
**Constraints**: Must integrate seamlessly with existing Journey Designer UI, reuse Radix UI + Tailwind components, support accessibility (WCAG 2.1 AA)  
**Scale/Scope**: Single UI feature - HTTP connector configuration panel, extending SERVICE_TASK properties

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

✅ **Specification-Driven Development**: Feature spec is complete with user stories, requirements, and acceptance criteria  
✅ **API Contract First**: Backend API contracts defined in OpenAPI 3.0.3 (HttpConnectorConfiguration schema with method, url, headers, queryParams, body, timeout)  
✅ **Layered Architecture**: Will follow Page → Component → Service → API Client pattern  
✅ **State Machine as Core Domain**: N/A - this is a UI configuration feature; state machine is backend concern

**Gate Status**: ✅ **PASS** - No constitution violations. Feature aligns with all project principles.

---

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (journey-orchestrator-ui repository)

```text
src/
├── components/
│   ├── journey-designer/
│   │   ├── nodes/
│   │   │   └── ServiceTaskNode.tsx        # SERVICE_TASK node component
│   │   └── properties-panel/
│   │       └── ConnectorConfigPanel.tsx   # Main connector config UI
│   ├── connector-config/                  # Extracted connector components
│   │   ├── HttpConnectorForm.tsx          # HTTP connector form component
│   │   ├── HttpMethodSelect.tsx           # Method dropdown
│   │   ├── KeyValueList.tsx               # Dynamic headers/query params list
│   │   └── ConnectorConfigSection.tsx     # Container component
│   └── ui/
│       └── [existing Radix + Tailwind components]
├── services/
│   └── connectorService.ts                # API client for connector operations
├── types/
│   └── connector.ts                       # TypeScript types for connector config
├── hooks/
│   └── useConnectorConfig.ts              # Custom hook for connector form state
└── store/
    └── connectorStore.ts                  # Zustand store for connector state

tests/
├── unit/
│   ├── HttpConnectorForm.test.tsx
│   ├── KeyValueList.test.tsx
│   └── connectorService.test.ts
├── integration/
│   └── connector-configuration.test.tsx   # E2E flow: select task → configure → save
```

**Structure Decision**: Feature integrates into existing Journey Designer architecture. Connector configuration is a new properties panel section within ServiceTaskNode component. Uses existing Radix UI component library and Tailwind CSS for styling. Follows the layered architecture pattern: UI Components → Custom Hooks → Services → API Client (generated from OpenAPI).

---

## Phase 0: Research & Clarification

✅ **Status**: COMPLETE | **Date**: 2026-09-10

**Research Tasks Completed**:

- R1: Backend API response format for connector retrieval → RESOLVED ✅
- R2: UI pattern for dynamic list management → RESOLVED ✅
- R3: Form validation and error handling strategy → RESOLVED ✅
- R4: Placeholder/context variable support → RESOLVED ✅
- R5: Accessibility requirements for dynamic forms → RESOLVED ✅
- R6: State management for complex form operations → RESOLVED ✅

**Key Findings**:

- Backend API contract fully defined in OpenAPI 3.0.3
- Recommend React Hook Form + Zod for client-side validation
- Use Zustand for global connector state management
- Implement KeyValueList component pattern for dynamic headers/query params
- Target WCAG 2.1 AA accessibility using Radix UI primitives
- Support placeholder syntax: `${variableName}` (no client-side validation needed)

**Deliverable**: [research.md](research.md)

---

## Phase 1: Design & Contracts

✅ **Status**: COMPLETE | **Date**: 2026-09-10

**Design Artifacts Generated**:

1. **Data Model** ([data-model.md](data-model.md))
   - Entity definitions: HttpConnectorConfiguration, HttpMethod, ConnectorConfigEntry, FormState
   - Validation rules for all fields (required, URL format, header keys, timeout format)
   - State serialization (form ↔ backend API format)
   - TypeScript type definitions
   - Edge cases and constraints (max URL length, header limits, etc.)

2. **API Contract** ([contracts/API-CONTRACT.md](contracts/API-CONTRACT.md))
   - PUT /journeys/{id} endpoint for saving configuration
   - GET /journeys/{id} endpoint for loading configuration
   - Complete schema definitions from OpenAPI 3.0.3
   - Error handling contracts (400, 404, 409, 5xx responses)
   - UI implementation notes for request/response handling

3. **Quickstart Validation Guide** ([quickstart.md](quickstart.md))
   - 7 comprehensive scenarios covering all feature requirements
   - **Scenario 1**: Create and persist HTTP connector configuration (P1)
   - **Scenario 2**: Dynamic add/remove headers and query parameters (P2)
   - **Scenario 3**: Form validation and error feedback (P2)
   - **Scenario 4**: API error handling and retry
   - **Scenario 5**: Accessibility compliance (WCAG 2.1 AA)
   - **Scenario 6**: Placeholder variable support
   - **Scenario 7**: Empty optional fields handling
   - Test data reference with example connector configurations

**Re-evaluation**: Constitution Check PASSES after Phase 1 design ✅

- All design decisions align with specification-driven development
- API contract usage follows Contract First principle
- Layered architecture maintained (Components → Hooks → Services → API)
- No new violations introduced

---

## Next Steps

**Phase 2 (Ready for Execution)**:
Run `/speckit.tasks` command to generate actionable, dependency-ordered implementation tasks.

**Estimated Task Count**: 8-12 tasks covering:

- Component scaffolding (forms, dropdowns, key-value lists)
- API service integration
- State management (Zustand store setup)
- Validation implementation (React Hook Form + Zod)
- Error handling and user feedback
- Accessibility implementation
- Unit and integration testing
- Journey Designer integration

---

## Assumptions Validated

✅ Backend API for connector configuration is already implemented and stable  
✅ Journey Designer has existing properties panel system that can be extended  
✅ Existing UI components (dropdowns, text inputs, key/value lists) are available for reuse  
✅ Backend API contract matches the domain model defined in the spec

---

## Dependencies & Blockers

**External Dependencies**:

- Backend API running with HTTP connector endpoints accessible
- OpenAPI spec up-to-date at `journey-orchestrator/api-spec/openapi.yaml`

**No Blockers Identified** ✅
