# Tasks: Connector Framework (HTTP Connector)

**Input**: Design documents from `/specs/009-http-connector/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: E2E tests are REQUIRED per constitution for flow-affecting features. Unit and integration tests included for quality.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/main/java/com/luscadevs/journeyorchestrator/`
- **Tests**: `src/test/java/com/luscadevs/journeyorchestrator/`
- **API spec**: `api-spec/openapi.yaml`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Update OpenAPI specification and prepare project structure

- [x] T001 Update api-spec/openapi.yaml with connector-related changes per contracts/openapi.yaml
- [x] T002 Run Maven generate-sources to regenerate API stubs from updated OpenAPI specification
- [x] T003 Create domain/connector package structure in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/
- [x] T004 Create domain/connector/http package structure in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/http/
- [x] T005 Create adapters/out/connector package structure in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/connector/
- [x] T006 Create adapters/out/connector/http package structure in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/connector/http/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain entities and interfaces that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T007 Create ConnectorType enum in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/ConnectorType.java
- [x] T008 [P] Create HttpMethod enum in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/http/HttpMethod.java
- [x] T009 [P] Create Connector interface (Strategy pattern) in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/Connector.java
- [x] T010 Create ConnectorConfiguration abstract entity in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/ConnectorConfiguration.java
- [x] T011 Create ConnectorResult value object in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/ConnectorResult.java
- [x] T012 [P] Create HttpConnectorResult value object in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/http/HttpConnectorResult.java
- [x] T013 Create ConnectorExecutionRecord entity in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/ConnectorExecutionRecord.java
- [x] T014 Extend StateType enum with SERVICE_TASK in src/main/java/com/luscadevs/journeyorchestrator/domain/journey/StateType.java
- [x] T015 Add connectorConfiguration field to State entity in src/main/java/com/luscadevs/journeyorchestrator/domain/journey/State.java
- [x] T016 Add mergeData method to ExecutionContext in src/main/java/com/luscadevs/journeyorchestrator/domain/journeyinstance/ExecutionContext.java
- [x] T017 Add getVariable and hasVariable methods to ExecutionContext in src/main/java/com/luscadevs/journeyorchestrator/domain/journeyinstance/ExecutionContext.java
- [x] T018 Create custom exception hierarchy in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/exception/ConnectorException.java
- [x] T019 [P] Create ConnectorTimeoutException in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/exception/ConnectorTimeoutException.java
- [x] T020 [P] Create ConnectorConfigurationException in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/exception/ConnectorConfigurationException.java
- [x] T021 [P] Create ConnectorExecutionException in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/exception/ConnectorExecutionException.java
- [x] T022 [P] Create ConnectorNetworkException in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/exception/ConnectorNetworkException.java
- [x] T023 Create ConnectorRepositoryPort interface in src/main/java/com/luscadevs/journeyorchestrator/application/port/out/ConnectorRepositoryPort.java
- [x] T024 Create ConnectorResolverPort interface in src/main/java/com/luscadevs/journeyorchestrator/application/port/out/ConnectorResolverPort.java
- [x] T025 Create ConnectorPort interface in src/main/java/com/luscadevs/journeyorchestrator/application/port/in/ConnectorPort.java

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Configure HTTP Connector in Journey Designer (Priority: P1) 🎯 MVP

**Goal**: Enable journey designers to configure SERVICE_TASK states with HTTP Connector parameters

**Independent Test**: Create a journey definition with a SERVICE_TASK state, configure it with HTTP Connector parameters (method, URL, headers, body), and verify the configuration is persisted correctly via API

### Tests for User Story 1

- [ ] T026 [P] [US1] Unit test for HttpConnectorConfiguration validation in src/test/java/com/luscadevs/journeyorchestrator/domain/connector/http/HttpConnectorConfigurationTest.java
- [ ] T027 [P] [US1] Unit test for ConnectorConfiguration base validation in src/test/java/com/luscadevs/journeyorchestrator/domain/connector/ConnectorConfigurationTest.java

### Implementation for User Story 1

- [ ] T028 [US1] Create HttpConnectorConfiguration entity in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/http/HttpConnectorConfiguration.java (depends on T007, T008, T010)
- [ ] T029 [US1] Create ConnectorConfigurationDto in src/main/java/com/luscadevs/journeyorchestrator/api/dto/connector/ConnectorConfigurationDto.java
- [ ] T030 [US1] Create HttpConnectorConfigurationDto in src/main/java/com/luscadevs/journeyorchestrator/api/dto/connector/HttpConnectorConfigurationDto.java
- [ ] T031 [US1] Create ConnectorMapper in src/main/java/com/luscadevs/journeyorchestrator/api/mapper/ConnectorMapper.java
- [ ] T032 [US1] Update State entity validation to require connectorConfiguration when type is SERVICE_TASK in src/main/java/com/luscadevs/journeyorchestrator/domain/journey/State.java
- [ ] T033 [US1] Update journey definition creation/update logic to validate SERVICE_TASK configuration in src/main/java/com/luscadevs/journeyorchestrator/application/service/JourneyDefinitionService.java

**Checkpoint**: At this point, User Story 1 should be fully functional - journey definitions can be created with SERVICE_TASK states and HTTP Connector configuration

---

## Phase 4: User Story 2 - Runtime Executes HTTP Connector Automatically (Priority: P1)

**Goal**: Runtime automatically executes HTTP Connector when journey instance enters SERVICE_TASK state

**Independent Test**: Start a journey instance that reaches a SERVICE_TASK state with HTTP Connector configuration, and verify the HTTP request is executed automatically

### Tests for User Story 2

- [ ] T034 [P] [US2] Unit test for ConnectorResolver in src/test/java/com/luscadevs/journeyorchestrator/adapters/out/connector/ConnectorResolverTest.java
- [ ] T035 [P] [US2] Unit test for ContextVariableResolver in src/test/java/com/luscadevs/journeyorchestrator/adapters/out/connector/http/ContextVariableResolverTest.java
- [ ] T036 [P] [US2] Integration test for ConnectorExecutionService in src/test/java/com/luscadevs/journeyorchestrator/application/service/ConnectorExecutionServiceTest.java

### Implementation for User Story 2

- [ ] T037 [US2] Create ContextVariableResolver utility in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/connector/http/ContextVariableResolver.java
- [ ] T038 [US2] Create HttpClientAdapter in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/connector/http/HttpClientAdapter.java
- [ ] T039 [US2] Create HttpConnector implementation in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/connector/http/HttpConnector.java (depends on T037, T038)
- [ ] T040 [US2] Create ConnectorResolver implementation in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/connector/ConnectorResolver.java
- [ ] T041 [US2] Create ConnectorExecutionService in src/main/java/com/luscadevs/journeyorchestrator/application/service/ConnectorExecutionService.java (depends on T023, T024, T025, T040)
- [ ] T042 [US2] Create ConnectorExecutionDocument in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/ConnectorExecutionDocument.java
- [ ] T043 [US2] Extend JourneyInstanceDocument with connectorExecutions field in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/JourneyInstanceDocument.java
- [ ] T044 [US2] Create ConnectorExecutionMongoRepository in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/ConnectorExecutionMongoRepository.java
- [ ] T045 [US2] Create ConnectorExecutionRepositoryImpl in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/ConnectorExecutionRepositoryImpl.java (depends on T023, T044)
- [ ] T046 [US2] Extend StateTransitionHandler to execute connector on SERVICE_TASK entry in src/main/java/com/luscadevs/journeyorchestrator/application/engine/StateTransitionHandler.java (depends on T041)
- [ ] T047 [US2] Configure WebClient bean with timeout settings in src/main/java/com/luscadevs/journeyorchestrator/adapters/config/WebClientConfig.java
- [ ] T048 [US2] Add connector execution logging with correlationId in src/main/java/com/luscadevs/journeyorchestrator/adapters/observability/aspect/ExecutionLoggingAspect.java

**Checkpoint**: At this point, User Stories 1 AND 2 should both work - journey definitions can be configured AND runtime executes connectors automatically

---

## Phase 5: User Story 3 - Context Enrichment from Connector Results (Priority: P2)

**Goal**: Connector results enrich execution context for use in subsequent journey steps

**Independent Test**: Configure an HTTP Connector that returns data, and verify that the data is available in the context for subsequent states

### Tests for User Story 3

- [ ] T049 [P] [US3] Unit test for ExecutionContext mergeData in src/test/java/com/luscadevs/journeyorchestrator/domain/journeyinstance/ExecutionContextTest.java
- [ ] T050 [P] [US3] Unit test for ExecutionContext getVariable with dot notation in src/test/java/com/luscadevs/journeyorchestrator/domain/journeyinstance/ExecutionContextTest.java

### Implementation for User Story 3

- [ ] T051 [US3] Implement deep merge algorithm in ExecutionContext.mergeData in src/main/java/com/luscadevs/journeyorchestrator/domain/journeyinstance/ExecutionContext.java
- [ ] T052 [US3] Implement dot notation parsing in ExecutionContext.getVariable in src/main/java/com/luscadevs/journeyorchestrator/domain/journeyinstance/ExecutionContext.java
- [ ] T053 [US3] Update HttpConnector to parse JSON response and convert to Map in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/connector/http/HttpConnector.java
- [ ] T054 [US3] Update ConnectorExecutionService to merge connector result into context in src/main/java/com/luscadevs/journeyorchestrator/application/service/ConnectorExecutionService.java

**Checkpoint**: All user stories should now be independently functional - context enrichment working

---

## Phase 6: User Story 4 - Audit Trail for Connector Executions (Priority: P2)

**Goal**: System administrators can view detailed audit information about connector executions

**Independent Test**: Execute a journey with a connector, then view the execution details and verify the audit information is present and accurate

### Tests for User Story 4

- [ ] T055 [P] [US4] Integration test for connector execution persistence in src/test/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/ConnectorExecutionRepositoryImplTest.java
- [ ] T056 [P] [US4] Contract test for connector execution history endpoint in src/test/java/com/luscadevs/journeyorchestrator/e2e/ConnectorExecutionHistoryE2ETest.java

### Implementation for User Story 4

- [ ] T057 [US4] Create ConnectorExecutionResponse DTO in src/main/java/com/luscadevs/journeyorchestrator/adapters/in/web/dto/ConnectorExecutionResponse.java
- [ ] T058 [US4] Create ConnectorExecutionController in src/main/java/com/luscadevs/journeyorchestrator/adapters/in/web/ConnectorExecutionController.java
- [ ] T059 [US4] Update ConnectorExecutionService to retrieve execution history in src/main/java/com/luscadevs/journeyorchestrator/application/service/ConnectorExecutionService.java
- [ ] T060 [US4] Add MongoDB indexes for connector execution queries in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/config/MongoIndexConfig.java
- [ ] T061 [US4] Update ConnectorExecutionRepositoryImpl to support filtering and pagination in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/ConnectorExecutionRepositoryImpl.java

**Checkpoint**: All user stories should now be independently functional - audit trail working

---

## Phase 7: E2E Tests (Constitution Requirement)

**Purpose**: Comprehensive E2E tests for complete connector workflows (REQUIRED per constitution)

- [ ] T062 Create ConnectorWorkflowE2ETest in src/test/java/com/luscadevs/journeyorchestrator/e2e/ConnectorWorkflowE2ETest.java
- [ ] T063 Implement E2E test for successful HTTP connector execution in src/test/java/com/luscadevs/journeyorchestrator/e2e/ConnectorWorkflowE2ETest.java
- [ ] T064 Implement E2E test for connector execution with context variables in src/test/java/com/luscadevs/journeyorchestrator/e2e/ConnectorWorkflowE2ETest.java
- [ ] T065 Implement E2E test for connector execution failure handling in src/test/java/com/luscadevs/journeyorchestrator/e2e/ConnectorWorkflowE2ETest.java
- [ ] T066 Implement E2E test for connector execution timeout in src/test/java/com/luscadevs/journeyorchestrator/e2e/ConnectorWorkflowE2ETest.java
- [ ] T067 Implement E2E test for context enrichment verification in src/test/java/com/luscadevs/journeyorchestrator/e2e/ConnectorWorkflowE2ETest.java
- [ ] T068 Implement E2E test for audit trail verification in src/test/java/com/luscadevs/journeyorchestrator/e2e/ConnectorWorkflowE2ETest.java

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T069 [P] Add sensitive data sanitization for auth tokens in logs in src/main/java/com/luscadevs/journeyorchestrator/adapters/observability/enhancer/SensitiveDataSanitizer.java
- [ ] T070 [P] Update OpenAPI documentation with connector examples in api-spec/openapi.yaml
- [ ] T071 Update ADR documentation for connector framework in docs/adr/009-connector-framework.md
- [ ] T072 Update domain model documentation in docs/domain-model.md
- [ ] T073 Run quickstart.md validation examples
- [ ] T074 Performance test for connector execution threshold (<500ms) in src/test/java/com/luscadevs/journeyorchestrator/e2e/ConnectorPerformanceTest.java
- [ ] T075 Verify all existing E2E tests still pass (regression prevention)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-6)**: All depend on Foundational phase completion
  - User Story 1 (P1): Can start after Foundational - No dependencies on other stories
  - User Story 2 (P1): Can start after Foundational - Depends on US1 for configuration structure
  - User Story 3 (P2): Can start after Foundational - Depends on US2 for connector execution
  - User Story 4 (P2): Can start after Foundational - Depends on US2 for execution records
- **E2E Tests (Phase 7)**: Depends on all user stories being complete
- **Polish (Phase 8)**: Depends on all user stories and E2E tests being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational (Phase 2) - Depends on US1 configuration entities
- **User Story 3 (P2)**: Can start after US2 complete - Requires connector execution to be working
- **User Story 4 (P2)**: Can start after US2 complete - Requires execution records to be generated

### Within Each User Story

- Tests MUST be written and FAIL before implementation (TDD approach)
- Domain entities before application services
- Application services before adapters
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Tests within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members (after dependencies satisfied)
- Polish tasks marked [P] can run in parallel

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Unit test for HttpConnectorConfiguration validation in src/test/java/com/luscadevs/journeyorchestrator/domain/connector/http/HttpConnectorConfigurationTest.java"
Task: "Unit test for ConnectorConfiguration base validation in src/test/java/com/luscadevs/journeyorchestrator/domain/connector/ConnectorConfigurationTest.java"
```

---

## Parallel Example: Foundational Phase

```bash
# Launch all enum and interface tasks together:
Task: "Create HttpMethod enum in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/http/HttpMethod.java"
Task: "Create Connector interface (Strategy pattern) in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/Connector.java"
Task: "Create HttpConnectorResult value object in src/main/java/com/luscadevs/journeyorchestrator/domain/connector/http/HttpConnectorResult.java"
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. Complete Phase 4: User Story 2
5. **STOP and VALIDATE**: Test connector configuration and automatic execution independently
6. Deploy/demo if ready

### Full Feature Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Configuration working
3. Add User Story 2 → Test independently → Automatic execution working (MVP!)
4. Add User Story 3 → Test independently → Context enrichment working
5. Add User Story 4 → Test independently → Audit trail working
6. Complete E2E tests → Full coverage
7. Complete Polish → Production ready

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (configuration)
   - Developer B: User Story 2 (execution) - waits for US1 entities
3. After US1 + US2 complete:
   - Developer A: User Story 3 (context enrichment)
   - Developer B: User Story 4 (audit trail)
4. Team completes E2E tests together
5. Team completes polish tasks in parallel

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing (TDD)
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Constitution requires E2E tests for flow-affecting features - Phase 7 is mandatory
- OpenAPI must be updated first (T001) per constitution's specification-driven development
- All domain entities must have no framework dependencies (constitution requirement)
