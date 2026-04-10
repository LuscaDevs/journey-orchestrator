# Tasks: Incremental Graph Evolution Refactor

**Input**: Design documents from `/specs/008-graph-evolution-refactor/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Test tasks are included based on FR-018, FR-019, SC-002, and constitution E2E test compliance requirements.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/`, `tests/` at repository root
- Paths follow the hexagonal architecture: `src/main/java/com/luscadevs/journeyorchestrator/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Verify environment and prepare for refactor

- [ ] T001 Verify Java 21 and Maven are installed in development environment
- [ ] T002 Verify MongoDB is running or Testcontainers is configured for testing
- [ ] T003 Verify OpenAPI code generation workflow is functional by running `mvn generate-sources`
- [ ] T004 Create feature branch `008-graph-evolution-refactor` if not already created

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T005 Update OpenAPI specification in api-spec/openapi.yaml to add optional `id` field to State schema
- [ ] T006 Update OpenAPI specification in api-spec/openapi.yaml to add optional `position` field to State schema
- [ ] T007 Update OpenAPI specification in api-spec/openapi.yaml to add optional `sourceStateId` field to TransitionRequest schema
- [ ] T008 Update OpenAPI specification in api-spec/openapi.yaml to add optional `targetStateId` field to TransitionRequest schema
- [ ] T009 Update OpenAPI specification in api-spec/openapi.yaml to add optional `sourceStateId` field to TransitionResponse schema
- [ ] T010 Update OpenAPI specification in api-spec/openapi.yaml to add optional `targetStateId` field to TransitionResponse schema
- [ ] T011 Run `mvn generate-sources` to regenerate code from updated OpenAPI specification
- [ ] T012 Verify generated State.java includes new `id` and `position` fields in src/main/java/com/luscadevs/journeyorchestrator/api/model/
- [ ] T013 Verify generated TransitionRequest.java includes new `sourceStateId` and `targetStateId` fields in src/main/java/com/luscadevs/journeyorchestrator/api/model/
- [ ] T014 Verify generated TransitionResponse.java includes new `sourceStateId` and `targetStateId` fields in src/main/java/com/luscadevs/journeyorchestrator/api/model/
- [ ] T015 Verify generated Position.java class exists in src/main/java/com/luscadevs/journeyorchestrator/api/model/
- [ ] T016 Add MongoDB compound index on `{ journeyDefinitionId: 1, id: 1 }` for StateDocument in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/StateDocument.java
- [ ] T017 Add MongoDB index on `{ id: 1 }` for StateDocument in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/StateDocument.java
- [ ] T018 Add MongoDB index on `{ sourceStateId: 1 }` for TransitionDocument in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/TransitionDocument.java
- [ ] T019 Add MongoDB index on `{ targetStateId: 1 }` for TransitionDocument in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/TransitionDocument.java

**Checkpoint**: Foundation ready - OpenAPI updated, code regenerated, indexes added - user story implementation can now begin

---

## Phase 3: User Story 1 - Backward-Compatible State Identity (Priority: P1) 🎯 MVP

**Goal**: States automatically receive unique identifiers while maintaining name-based functionality

**Independent Test**: Create journey definition via API with states using only names, verify states receive unique IDs while name-based transitions continue to function

### Tests for User Story 1

- [ ] T020 [P] [US1] Create unit test for UUID auto-generation in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/StateTest.java
- [ ] T021 [P] [US1] Create unit test for ID immutability in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/StateTest.java
- [ ] T022 [P] [US1] Create unit test for duplicate state name rejection in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/StateTest.java
- [ ] T023 [P] [US1] Create integration test for state creation with auto-generated IDs in src/test/java/com/luscadevs/journeyorchestrator/integration/JourneyDefinitionServiceTest.java

### Implementation for User Story 1

- [ ] T024 [US1] Add `id` field (UUID) to State domain entity in src/main/java/com/luscadevs/journeyorchestrator/domain/journey/State.java
- [ ] T025 [US1] Add `position` field (Position) to State domain entity in src/main/java/com/luscadevs/journeyorchestrator/domain/journey/State.java
- [ ] T026 [US1] Implement UUID auto-generation in State constructor in src/main/java/com/luscadevs/journeyorchestrator/domain/journey/State.java
- [ ] T027 [US1] Implement ID immutability check in State in src/main/java/com/luscadevs/journeyorchestrator/domain/journey/State.java
- [ ] T028 [US1] Create Position value object in src/main/java/com/luscadevs/journeyorchestrator/domain/journey/Position.java
- [ ] T029 [US1] Add `id` field to StateDocument in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/StateDocument.java
- [ ] T030 [US1] Add `position` field to StateDocument in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/StateDocument.java
- [ ] T031 [US1] Update StateDocument mapper to handle new fields in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/StateDocumentMapper.java
- [ ] T032 [US1] Update JourneyDefinitionMapper to map State.id in src/main/java/com/luscadevs/journeyorchestrator/api/mapper/JourneyDefinitionMapper.java
- [ ] T033 [US1] Update JourneyDefinitionMapper to map State.position in src/main/java/com/luscadevs/journeyorchestrator/api/mapper/JourneyDefinitionMapper.java
- [ ] T034 [US1] Update JourneyDefinitionService to auto-generate IDs for states without IDs in src/main/java/com/luscadevs/journeyorchestrator/application/service/JourneyDefinitionService.java
- [ ] T035 [US1] Add validation for duplicate state names in JourneyDefinitionService in src/main/java/com/luscadevs/journeyorchestrator/application/service/JourneyDefinitionService.java

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently - states have IDs and position data

---

## Phase 4: User Story 2 - Dual Reference Transition Support (Priority: P1)

**Goal**: Transitions can reference states by name, ID, or both with proper resolution

**Independent Test**: Create journey definitions with transitions using name references, ID references, and mixed references, verify all resolve correctly

### Tests for User Story 2

- [ ] T036 [P] [US2] Create unit test for state reference resolution by ID in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/TransitionTest.java
- [ ] T037 [P] [US2] Create unit test for state reference resolution by name in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/TransitionTest.java
- [ ] T038 [P] [US2] Create unit test for mixed reference patterns in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/TransitionTest.java
- [ ] T039 [P] [US2] Create unit test for conflict detection (ID vs name mismatch) in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/TransitionTest.java
- [ ] T040 [P] [US2] Create integration test for dual reference resolution in src/test/java/com/luscadevs/journeyorchestrator/integration/JourneyDefinitionServiceTest.java
- [ ] T041 [P] [US2] Create E2E test for ID-based transitions in src/test/java/com/luscadevs/journeyorchestrator/e2e/DualReferenceE2ETest.java

### Implementation for User Story 2

- [ ] T042 [US2] Add `sourceStateId` field to Transition domain entity in src/main/java/com/luscadevs/journeyorchestrator/domain/journey/Transition.java
- [ ] T043 [US2] Add `targetStateId` field to Transition domain entity in src/main/java/com/luscadevs/journeyorchestrator/domain/journey/Transition.java
- [ ] T044 [US2] Add validation for at least one reference type in Transition constructor in src/main/java/com/luscadevs/journeyorchestrator/domain/journey/Transition.java
- [ ] T045 [US2] Create StateReference value object in src/main/java/com/luscadevs/journeyorchestrator/domain/journey/StateReference.java
- [ ] T046 [US2] Add `sourceStateId` field to TransitionDocument in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/TransitionDocument.java
- [ ] T047 [US2] Add `targetStateId` field to TransitionDocument in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/TransitionDocument.java
- [ ] T048 [US2] Update TransitionDocument mapper to handle new fields in src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/TransitionDocumentMapper.java
- [ ] T049 [US2] Update JourneyDefinitionMapper to map Transition.sourceStateId in src/main/java/com/luscadevs/journeyorchestrator/api/mapper/JourneyDefinitionMapper.java
- [ ] T050 [US2] Update JourneyDefinitionMapper to map Transition.targetStateId in src/main/java/com/luscadevs/journeyorchestrator/api/mapper/JourneyDefinitionMapper.java
- [ ] T051 [US2] Implement state reference resolution logic in JourneyDefinitionService in src/main/java/com/luscadevs/journeyorchestrator/application/service/JourneyDefinitionService.java
- [ ] T052 [US2] Implement conflict detection for ID vs name mismatches in JourneyDefinitionService in src/main/java/com/luscadevs/journeyorchestrator/application/service/JourneyDefinitionService.java
- [ ] T053 [US2] Implement ID precedence when both ID and name provided in JourneyDefinitionService in src/main/java/com/luscadevs/journeyorchestrator/application/service/JourneyDefinitionService.java
- [ ] T054 [US2] Normalize transitions internally to use state IDs in JourneyDefinitionService in src/main/java/com/luscadevs/journeyorchestrator/application/service/JourneyDefinitionService.java

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - dual reference resolution functional

---

## Phase 5: User Story 3 - Consistency Validation (Priority: P2)

**Goal**: System validates state name and ID consistency throughout lifecycle

**Independent Test**: Attempt to create journey definitions with inconsistent state references, verify validation errors prevent invalid data

### Tests for User Story 3

- [ ] T055 [P] [US3] Create unit test for invalid ID reference rejection in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/StateReferenceTest.java
- [ ] T056 [P] [US3] Create unit test for circular reference validation in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/JourneyDefinitionTest.java
- [ ] T057 [P] [US3] Create integration test for consistency validation on state updates in src/test/java/com/luscadevs/journeyorchestrator/integration/JourneyDefinitionServiceTest.java

### Implementation for User Story 3

- [ ] T058 [US3] Implement UUID format validation in State constructor in src/main/java/com/luscadevs/journeyorchestrator/domain/journey/State.java
- [ ] T059 [US3] Add validation for UUID format when client-provided in JourneyDefinitionService in src/main/java/com/luscadevs/journeyorchestrator/application/service/JourneyDefinitionService.java
- [ ] T060 [US3] Implement orphan transition detection in JourneyDefinitionService in src/main/java/com/luscadevs/journeyorchestrator/application/service/JourneyDefinitionService.java
- [ ] T061 [US3] Add circular reference validation in JourneyDefinitionService in src/main/java/com/luscadevs/journeyorchestrator/application/service/JourneyDefinitionService.java

**Checkpoint**: All user stories should now be independently functional - consistency validation in place

---

## Phase 6: User Story 4 - Legacy API Compatibility (Priority: P1)

**Goal**: Existing API clients continue to function without any changes

**Independent Test**: Run existing E2E test suite without modification, verify all tests pass

### Tests for User Story 4

- [ ] T062 [P] [US4] Create E2E test for legacy name-based references in src/test/java/com/luscadevs/journeyorchestrator/e2e/LegacyCompatibilityE2ETest.java
- [ ] T063 [P] [US4] Create E2E test for position data persistence in src/test/java/com/luscadevs/journeyorchestrator/e2e/PositionDataE2ETest.java
- [ ] T064 [US4] Run all existing E2E tests to verify backward compatibility

### Implementation for User Story 4

- [ ] T065 [US4] Verify OpenAPI specification changes maintain backward compatibility by comparing api-spec/openapi.yaml before and after changes
- [ ] T066 [US4] Update JourneyDefinitionController to handle new optional fields without breaking existing clients in src/main/java/com/luscadevs/journeyorchestrator/adapters/in/web/JourneyDefinitionController.java
- [ ] T067 [US4] Add logging for backward compatibility operations in JourneyDefinitionController in src/main/java/com/luscadevs/journeyorchestrator/adapters/in/web/JourneyDefinitionController.java
- [ ] T068 [US4] Update existing unit tests to cover both legacy and new reference patterns in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/StateTest.java
- [ ] T069 [US4] Update existing unit tests to cover both legacy and new reference patterns in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/TransitionTest.java
- [ ] T070 [US4] Update existing integration tests to cover both legacy and new reference patterns in src/test/java/com/luscadevs/journeyorchestrator/integration/JourneyDefinitionServiceTest.java

**Checkpoint**: Legacy compatibility verified - all existing tests pass without modification

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T071 [P] Run all unit tests and verify 90%+ coverage for dual reference resolution logic
- [ ] T072 [P] Run all integration tests and verify dual reference patterns work correctly
- [ ] T073 [P] Run all E2E tests including new DualReferenceE2ETest, LegacyCompatibilityE2ETest, and PositionDataE2ETest
- [ ] T074 Performance test journey definition creation to verify <10% performance impact
- [ ] T075 Performance test state reference resolution to verify sub-millisecond performance
- [ ] T076 Performance test validation error responses to verify <100ms response time
- [ ] T077 Update documentation in docs/domain-model.md to reflect new State and Transition structure
- [ ] T078 Update documentation in docs/journey-dsl.md to reflect dual reference support
- [ ] T079 Verify code generation produces classes reflecting new model by reviewing generated classes in src/main/java/com/luscadevs/journeyorchestrator/api/model/
- [ ] T080 Run quickstart.md validation steps to verify implementation completeness
- [ ] T081 Generate JaCoCo coverage report with `mvn jacoco:report`
- [ ] T082 Verify coverage report shows 90%+ coverage for resolution logic

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-6)**: All depend on Foundational phase completion
  - User Story 1 (P1): Can start after Foundational - No dependencies on other stories
  - User Story 2 (P1): Can start after Foundational and User Story 1 - depends on State entity changes
  - User Story 3 (P2): Can start after Foundational and User Story 2 - depends on Transition and StateReference
  - User Story 4 (P1): Can start after Foundational and User Story 2 - depends on dual reference implementation
- **Polish (Phase 7)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories - BLOCKS User Story 2
- **User Story 2 (P1)**: Depends on User Story 1 completion (needs State entity with ID field)
- **User Story 3 (P2)**: Depends on User Story 2 completion (needs Transition with ID fields and StateReference)
- **User Story 4 (P1)**: Depends on User Story 2 completion (needs dual reference resolution implemented)

### Within Each User Story

- Tests MUST be written and FAIL before implementation (TDD approach)
- Domain entities before application services
- Application services before controllers
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- **Setup Phase**: T001, T002, T003 can run in parallel
- **Foundational Phase**: T005-T010 (OpenAPI updates) can run in parallel as separate schema updates
- **User Story 1 Tests**: T020, T021, T022 can run in parallel
- **User Story 2 Tests**: T036, T037, T038, T039 can run in parallel
- **User Story 3 Tests**: T055, T056 can run in parallel
- **User Story 4 Tests**: T062, T063 can run in parallel
- **Polish Phase**: T071, T072, T073 can run in parallel

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Create unit test for UUID auto-generation in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/StateTest.java"
Task: "Create unit test for ID immutability in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/StateTest.java"
Task: "Create unit test for duplicate state name rejection in src/test/java/com/luscadevs/journeyorchestrator/unit/journey/StateTest.java"

# Launch OpenAPI schema updates together:
Task: "Update OpenAPI specification in api-spec/openapi.yaml to add optional id field to State schema"
Task: "Update OpenAPI specification in api-spec/openapi.yaml to add optional position field to State schema"
Task: "Update OpenAPI specification in api-spec/openapi.yaml to add optional sourceStateId field to TransitionRequest schema"
Task: "Update OpenAPI specification in api-spec/openapi.yaml to add optional targetStateId field to TransitionRequest schema"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently - states have IDs and position data
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP - states have identity)
3. Add User Story 2 → Test independently → Deploy/Demo (dual reference support)
4. Add User Story 3 → Test independently → Deploy/Demo (consistency validation)
5. Add User Story 4 → Test independently → Deploy/Demo (legacy compatibility verified)
6. Complete Polish → Final validation and performance testing

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (State identity)
   - Developer B: User Story 2 (Dual reference - waits for US1)
   - Developer C: User Story 4 (Legacy compatibility - waits for US2)
3. After US1 and US2 complete:
   - Developer B continues with User Story 3 (Consistency validation)
4. All stories integrate and validate together in Polish phase

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Tests are written following TDD approach - write failing tests first, then implement
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- OpenAPI changes must happen first (specification-driven development principle)
- All new fields are optional to maintain backward compatibility
- Performance targets: <10% impact, sub-millisecond resolution, <100ms validation
- Coverage target: 90%+ for dual reference resolution logic
