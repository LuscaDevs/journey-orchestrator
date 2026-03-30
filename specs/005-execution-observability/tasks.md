---

description: "Task list for execution observability feature implementation"
---

# Tasks: Execution Observability

**Input**: Design documents from `/specs/005-execution-observability/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: The examples below include test tasks. Tests are OPTIONAL - only include them if explicitly requested in feature specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/`, `tests/` at repository root
- **Web app**: `backend/src/`, `frontend/src/`
- **Mobile**: `api/src/`, `ios/src/` or `android/src/`
- Paths shown below assume single project - adjust based on plan.md structure

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Add Spring AOP dependency to pom.xml
- [ ] T002 Add JSON logging dependency to pom.xml
- [ ] T003 [P] Create observability package structure in src/main/java/com/luscadevs/journeyorchestrator/adapters/observability/
- [ ] T004 [P] Create aspect subdirectory in observability package
- [ ] T005 [P] Create filter subdirectory in observability package
- [ ] T006 [P] Create enhancer subdirectory in observability package
- [ ] T007 [P] Create config subdirectory in observability package

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T008 Create ObservabilityProperties configuration class in src/main/java/com/luscadevs/journeyorchestrator/observability/config/ObservabilityProperties.java
- [ ] T009 [P] Update logback-spring.xml with JSON encoder configuration
- [ ] T010 [P] Update application.yml with observability configuration
- [ ] T011 Create MDCErrorEnhancer class in src/main/java/com/luscadevs/journeyorchestrator/observability/enhancer/MDCErrorEnhancer.java
- [ ] T012 Create ExecutionLogEntry data model in src/main/java/com/luscadevs/journeyorchestrator/observability/model/ExecutionLogEntry.java
- [ ] T013 Create execution phase enums in src/main/java/com/luscadevs/journeyorchestrator/observability/model/ExecutionPhase.java
- [ ] T014 Create component type enums in src/main/java/com/luscadevs/journeyorchestrator/observability/model/ComponentType.java
- [ ] T015 Create execution status enums in src/main/java/com/luscadevs/journeyorchestrator/observability/model/ExecutionStatus.java

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Automatic Controller Lifecycle Logging (Priority: P1) 🎯 MVP

**Goal**: Every HTTP request automatically generates comprehensive execution logs with start time, completion status, duration, and MDC context without exposing request parameters.

**Independent Test**: Make HTTP requests to any controller endpoint and verify that structured logs contain start time, completion status, duration, and MDC context without exposing request parameters.

### Tests for User Story 1 (OPTIONAL - only if tests requested) ⚠️

> **NOTE**: Write these tests FIRST, ensure they FAIL before implementation

- [ ] T016 [P] [US1] Unit test for ExecutionLoggingAspect controller interception in src/test/java/com/luscadevs/journeyorchestrator/observability/aspect/ExecutionLoggingAspectTest.java
- [ ] T017 [P] [US1] Integration test for controller logging in src/test/java/com/luscadevs/journeyorchestrator/observability/integration/ControllerLoggingIntegrationTest.java

### Implementation for User Story 1

- [ ] T018 [P] [US1] Create ExecutionLoggingAspect base class in src/main/java/com/luscadevs/journeyorchestrator/observability/aspect/ExecutionLoggingAspect.java
- [ ] T019 [US1] Implement controller pointcut in ExecutionLoggingAspect for @RestController methods
- [ ] T020 [US1] Implement logExecutionStart method in ExecutionLoggingAspect
- [ ] T021 [US1] Implement logExecutionCompletion method in ExecutionLoggingAspect
- [ ] T022 [US1] Implement createLogData method in ExecutionLoggingAspect
- [ ] T023 [US1] Implement getComponentIdentifier method in ExecutionLoggingAspect
- [ ] T024 [US1] Add controller-specific field handling (httpMethod, requestPath) in ExecutionLoggingAspect
- [ ] T025 [US1] Implement sensitive data exclusion logic in ExecutionLoggingAspect
- [ ] T026 [US1] Add exception handling and error logging in ExecutionLoggingAspect
- [ ] T027 [US1] Update GlobalExceptionHandler to use MDCErrorEnhancer in src/main/java/com/luscadevs/journeyorchestrator/adapters/in/web/GlobalExceptionHandler.java

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Application Service Execution Logging (Priority: P1)

**Goal**: All application service method executions are automatically logged with method start/completion, duration, and MDC context propagation.

**Independent Test**: Invoke any application service method and verify that structured logs contain method start/completion, duration, and MDC context propagation.

### Tests for User Story 2 (OPTIONAL - only if tests requested) ⚠️

- [ ] T028 [P] [US2] Unit test for service logging in ExecutionLoggingAspectTest.java
- [ ] T029 [P] [US2] Integration test for service logging in src/test/java/com/luscadevs/journeyorchestrator/observability/integration/ServiceLoggingIntegrationTest.java

### Implementation for User Story 2

- [ ] T030 [US2] Implement service pointcut in ExecutionLoggingAspect for @Service methods
- [ ] T031 [US2] Add service-specific field handling (service name, method name) in ExecutionLoggingAspect
- [ ] T032 [US2] Implement MDC context propagation for service layer in ExecutionLoggingAspect
- [ ] T033 [US2] Add service method parameter exclusion logic in ExecutionLoggingAspect
- [ ] T034 [US2] Test service logging with existing application services

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - MDC Context Integration (Priority: P2)

**Goal**: All execution logs include consistent MDC context information with correlationId, httpMethod, requestPath, and errorCode across different layers.

**Independent Test**: Make a request and verify that all generated log entries across controller and service layers contain the same correlationId and appropriate context values.

### Tests for User Story 3 (OPTIONAL - only if tests requested) ⚠️

- [ ] T035 [P] [US3] Unit test for MDC context propagation in src/test/java/com/luscadevs/journeyorchestrator/observability/integration/MDCContextPropagationTest.java
- [ ] T036 [P] [US3] Integration test for end-to-end MDC context in MDCContextPropagationTest.java

### Implementation for User Story 3

- [ ] T037 [US3] Enhance CorrelationIdFilter to ensure proper MDC initialization in src/main/java/com/luscadevs/journeyorchestrator/observability/filter/CorrelationIdFilter.java
- [ ] T038 [US3] Implement MDC context validation in ExecutionLoggingAspect
- [ ] T039 [US3] Add MDC context cleanup logic in ExecutionLoggingAspect
- [ ] T040 [US3] Implement error code handling in MDCErrorEnhancer
- [ ] T041 [US3] Add MDC context fallback handling for missing context in ExecutionLoggingAspect
- [ ] T042 [US3] Test MDC context consistency across all layers

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T043 [P] Add performance monitoring and metrics collection in src/main/java/com/luscadevs/journeyorchestrator/observability/metrics/LoggingMetrics.java
- [ ] T044 [P] Create log validation utilities in src/main/java/com/luscadevs/journeyorchestrator/observability/validation/LogValidator.java
- [ ] T045 Implement configuration validation in ObservabilityProperties
- [ ] T046 Add comprehensive error handling for logging failures in ExecutionLoggingAspect
- [ ] T047 [P] Create unit tests for all utility classes in src/test/java/com/luscadevs/journeyorchestrator/observability/
- [ ] T048 [P] Create integration tests for complete request flows in src/test/java/com/luscadevs/journeyorchestrator/observability/integration/
- [ ] T049 Add performance benchmarks in src/test/java/com/luscadevs/journeyorchestrator/observability/performance/
- [ ] T050 Update documentation in README.md and docs/observability.md
- [ ] T051 Run quickstart.md validation and update if needed
- [ ] T052 Configure CI/CD pipeline for observability feature testing

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-5)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P1 → P2)
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable

### Within Each User Story

- Tests (if included) MUST be written and FAIL before implementation
- Models before services
- Services before endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together (if tests requested):
Task: "Unit test for ExecutionLoggingAspect controller interception in src/test/java/com/luscadevs/journeyorchestrator/observability/aspect/ExecutionLoggingAspectTest.java"
Task: "Integration test for controller logging in src/test/java/com/luscadevs/journeyorchestrator/observability/integration/ControllerLoggingIntegrationTest.java"

# Launch all implementation tasks for User Story 1 together:
Task: "Create ExecutionLoggingAspect base class in src/main/java/com/luscadevs/journeyorchestrator/observability/aspect/ExecutionLoggingAspect.java"
Task: "Create ExecutionLogEntry data model in src/main/java/com/luscadevs/journeyorchestrator/observability/model/ExecutionLogEntry.java"
Task: "Create execution phase enums in src/main/java/com/luscadevs/journeyorchestrator/observability/model/ExecutionPhase.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (Controller Logging)
   - Developer B: User Story 2 (Service Logging)
   - Developer C: User Story 3 (MDC Integration)
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence

---

## Summary

- **Total Tasks**: 52
- **Tasks per User Story**: 
  - User Story 1: 12 tasks (including tests)
  - User Story 2: 6 tasks (including tests)
  - User Story 3: 8 tasks (including tests)
- **Parallel Opportunities**: 25 tasks marked as parallelizable
- **Independent Test Criteria**: Each user story has clear independent test criteria
- **Suggested MVP Scope**: User Story 1 (Controller Logging) provides immediate value
