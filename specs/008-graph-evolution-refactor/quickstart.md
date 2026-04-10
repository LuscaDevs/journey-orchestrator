# Quickstart Guide: Incremental Graph Evolution Refactor

**Feature**: 008-graph-evolution-refactor  
**Date**: 2025-04-10  
**Status**: Draft

## Overview

This guide provides step-by-step instructions for implementing the incremental graph evolution refactor. Follow this guide to add unique identifiers (UUID) to States, dual reference support for Transitions, and optional position data for visual editor integration.

## Prerequisites

- Java 21 installed
- Maven 3.8+ installed
- MongoDB running (or Testcontainers for testing)
- Git branch `008-graph-evolution-refactor` checked out
- Access to the project repository

## Implementation Steps

### Step 1: Update OpenAPI Specification

**File**: `api-spec/openapi.yaml`

**Action**: Add optional fields to State and Transition schemas

1. Open `api-spec/openapi.yaml`
2. Locate the `State` schema definition
3. Add the `id` field (optional, UUID format)
4. Add the `position` field (optional, object with x/y coordinates)
5. Locate the `TransitionRequest` schema definition
6. Add the `sourceStateId` field (optional, UUID format)
7. Add the `targetStateId` field (optional, UUID format)
8. Locate the `TransitionResponse` schema definition
9. Add the same `sourceStateId` and `targetStateId` fields
10. Save the file

**Reference**: See `contracts/openapi-evolution.md` for detailed schema changes

**Validation**:
```bash
# Validate OpenAPI specification
mvn validate
```

### Step 2: Regenerate Code from OpenAPI

**Action**: Run Maven code generation to update generated classes

```bash
mvn generate-sources
```

**Expected Output**: Generated classes in `src/main/java/com/luscadevs/journeyorchestrator/api/model/` should include:
- `State.java` with `id` and `position` fields
- `TransitionRequest.java` with `sourceStateId` and `targetStateId` fields
- `TransitionResponse.java` with `sourceStateId` and `targetStateId` fields
- `Position.java` (new class) with `x` and `y` fields

**Verification**:
```bash
# Check generated State class
cat src/main/java/com/luscadevs/journeyorchestrator/api/model/State.java

# Check generated TransitionRequest class
cat src/main/java/com/luscadevs/journeyorchestrator/api/model/TransitionRequest.java
```

### Step 3: Update Domain Layer

#### 3.1 Update State Entity

**File**: `src/main/java/com/luscadevs/journeyorchestrator/domain/journey/State.java`

**Action**: Add `id` and `position` fields

```java
@Getter
@Builder
public class State {
    private UUID id;              // NEW - unique identifier
    private String name;          // EXISTING
    private StateType type;       // EXISTING
    private Position position;    // NEW - optional position data
    
    // Validation logic
    public State {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("State name cannot be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("State type cannot be null");
        }
        // ID is auto-generated if not provided
        if (id == null) {
            this.id = UUID.randomUUID();
        }
        // Position is optional, can be null
    }
    
    // Immutability
    public State withId(UUID id) {
        if (this.id != null) {
            throw new IllegalStateException("State ID is immutable");
        }
        return State.builder()
            .id(id)
            .name(this.name)
            .type(this.type)
            .position(this.position)
            .build();
    }
}
```

#### 3.2 Create Position Value Object

**File**: `src/main/java/com/luscadevs/journeyorchestrator/domain/journey/Position.java`

**Action**: Create new value object for position data

```java
@Getter
@Builder
public class Position {
    private final double x;
    private final double y;
    
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
```

#### 3.3 Update Transition Entity

**File**: `src/main/java/com/luscadevs/journeyorchestrator/domain/journey/Transition.java`

**Action**: Add ID-based reference support

```java
@Getter
@Builder
public class Transition {
    private String source;           // EXISTING - name-based
    private String target;           // EXISTING - name-based
    private UUID sourceStateId;      // NEW - ID-based
    private UUID targetStateId;      // NEW - ID-based
    private String event;            // EXISTING
    private String condition;        // EXISTING - optional
    
    // Validation logic
    public Transition {
        if (event == null || event.isBlank()) {
            throw new IllegalArgumentException("Event cannot be blank");
        }
        // At least one reference type must be provided for source and target
        if ((source == null || source.isBlank()) && sourceStateId == null) {
            throw new IllegalArgumentException("Source reference must be provided");
        }
        if ((target == null || target.isBlank()) && targetStateId == null) {
            throw new IllegalArgumentException("Target reference must be provided");
        }
    }
}
```

#### 3.4 Create StateReference Value Object

**File**: `src/main/java/com/luscadevs/journeyorchestrator/domain/journey/StateReference.java`

**Action**: Create new value object for dual reference resolution

```java
@Getter
@Builder
public class StateReference {
    private final UUID id;
    private final String name;
    
    public StateReference(UUID id, String name) {
        if (id == null && (name == null || name.isBlank())) {
            throw new IllegalArgumentException("At least one of id or name must be provided");
        }
        this.id = id;
        this.name = name;
    }
    
    public boolean isIdBased() {
        return id != null;
    }
    
    public boolean isNameBased() {
        return name != null && !name.isBlank();
    }
}
```

### Step 4: Update Application Layer

#### 4.1 Update JourneyDefinitionService

**File**: `src/main/java/com/luscadevs/journeyorchestrator/application/service/JourneyDefinitionService.java`

**Action**: Add dual reference resolution logic

```java
@Service
public class JourneyDefinitionService {
    
    private final JourneyDefinitionRepositoryPort repository;
    
    public JourneyDefinition create(JourneyDefinition definition) {
        // Auto-generate IDs for states that don't have them
        List<State> statesWithIds = definition.getStates().stream()
            .map(state -> state.getId() == null ? 
                State.builder()
                    .id(UUID.randomUUID())
                    .name(state.getName())
                    .type(state.getType())
                    .position(state.getPosition())
                    .build() : state)
            .toList();
        
        // Build state lookup maps
        Map<UUID, State> stateById = statesWithIds.stream()
            .collect(Collectors.toMap(State::getId, Function.identity()));
        Map<String, State> stateByName = statesWithIds.stream()
            .collect(Collectors.toMap(State::getName, Function.identity()));
        
        // Resolve and validate transitions
        List<Transition> resolvedTransitions = definition.getTransitions().stream()
            .map(transition -> resolveTransition(transition, stateById, stateByName))
            .toList();
        
        // Create definition with resolved transitions
        JourneyDefinition resolvedDefinition = JourneyDefinition.builder()
            .id(definition.getId())
            .journeyCode(definition.getJourneyCode())
            .version(definition.getVersion())
            .status(definition.getStatus())
            .states(statesWithIds)
            .transitions(resolvedTransitions)
            .build();
        
        return repository.save(resolvedDefinition);
    }
    
    private Transition resolveTransition(Transition transition, 
                                        Map<UUID, State> stateById, 
                                        Map<String, State> stateByName) {
        // Resolve source
        State sourceState = resolveStateReference(
            transition.getSource(), 
            transition.getSourceStateId(), 
            stateById, 
            stateByName,
            "source"
        );
        
        // Resolve target
        State targetState = resolveStateReference(
            transition.getTarget(), 
            transition.getTargetStateId(), 
            stateById, 
            stateByName,
            "target"
        );
        
        // Return normalized transition with resolved IDs
        return Transition.builder()
            .source(transition.getSource())
            .target(transition.getTarget())
            .sourceStateId(sourceState.getId())
            .targetStateId(targetState.getId())
            .event(transition.getEvent())
            .condition(transition.getCondition())
            .build();
    }
    
    private State resolveStateReference(String name, UUID id, 
                                       Map<UUID, State> stateById, 
                                       Map<String, State> stateByName,
                                       String fieldName) {
        // If both provided, validate they refer to the same state
        if (id != null && name != null && !name.isBlank()) {
            State byId = stateById.get(id);
            State byName = stateByName.get(name);
            if (byId == null) {
                throw new IllegalArgumentException(
                    String.format("Transition %s references non-existent state by ID: %s", fieldName, id));
            }
            if (byName == null) {
                throw new IllegalArgumentException(
                    String.format("Transition %s references non-existent state by name: %s", fieldName, name));
            }
            if (!byId.getId().equals(byName.getId())) {
                throw new IllegalArgumentException(
                    String.format("Transition %s references conflict: ID refers to '%s' but name refers to '%s'", 
                                 fieldName, byName.getName(), byId.getName()));
            }
            return byId;
        }
        
        // ID-based reference
        if (id != null) {
            State state = stateById.get(id);
            if (state == null) {
                throw new IllegalArgumentException(
                    String.format("Transition %s references non-existent state by ID: %s", fieldName, id));
            }
            return state;
        }
        
        // Name-based reference
        if (name != null && !name.isBlank()) {
            State state = stateByName.get(name);
            if (state == null) {
                throw new IllegalArgumentException(
                    String.format("Transition %s references non-existent state by name: %s", fieldName, name));
            }
            return state;
        }
        
        throw new IllegalArgumentException(
            String.format("Transition %s has no valid reference", fieldName));
    }
}
```

### Step 5: Update Persistence Layer

#### 5.1 Update StateDocument

**File**: `src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/StateDocument.java`

**Action**: Add `id` and `position` fields

```java
@Document(collection = "states")
@Getter
@Builder
public class StateDocument {
    @Id
    private ObjectId mongoId;
    
    @Field("id")
    private UUID id;
    
    @Field("name")
    private String name;
    
    @Field("type")
    private String type;
    
    @Field("position")
    private Position position;
    
    @Field("journeyDefinitionId")
    private UUID journeyDefinitionId;
}
```

#### 5.2 Update TransitionDocument

**File**: `src/main/java/com/luscadevs/journeyorchestrator/adapters/out/persistence/mongo/TransitionDocument.java`

**Action**: Add `sourceStateId` and `targetStateId` fields

```java
@Document(collection = "transitions")
@Getter
@Builder
public class TransitionDocument {
    @Id
    private ObjectId mongoId;
    
    @Field("source")
    private String source;
    
    @Field("target")
    private String target;
    
    @Field("sourceStateId")
    private UUID sourceStateId;
    
    @Field("targetStateId")
    private UUID targetStateId;
    
    @Field("event")
    private String event;
    
    @Field("condition")
    private String condition;
    
    @Field("journeyDefinitionId")
    private UUID journeyDefinitionId;
}
```

#### 5.3 Update MongoDB Indexes

**Action**: Add new indexes for ID-based lookups

```java
@CompoundIndex(name = "journey_definition_id_name_idx", def = "{'journeyDefinitionId': 1, 'name': 1}")
@CompoundIndex(name = "journey_definition_id_id_idx", def = "{'journeyDefinitionId': 1, 'id': 1}")
@Indexed(name = "id_idx")
public class StateDocument {
    // ... fields
}

@Indexed(name = "source_state_id_idx")
@Indexed(name = "target_state_id_idx")
public class TransitionDocument {
    // ... fields
}
```

### Step 6: Update Mappers

#### 6.1 Update JourneyDefinitionMapper

**File**: `src/main/java/com/luscadevs/journeyorchestrator/api/mapper/JourneyDefinitionMapper.java`

**Action**: Add mapping for new fields

```java
@Mapper(componentModel = "spring")
public interface JourneyDefinitionMapper {
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "position", source = "position")
    State toDomainState(api.model.State apiState);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "position", source = "position")
    api.model.State toApiState(State domainState);
    
    @Mapping(target = "sourceStateId", source = "sourceStateId")
    @Mapping(target = "targetStateId", source = "targetStateId")
    Transition toDomainTransition(api.model.TransitionRequest apiTransition);
    
    @Mapping(target = "sourceStateId", source = "sourceStateId")
    @Mapping(target = "targetStateId", source = "targetStateId")
    api.model.TransitionResponse toApiTransition(Transition domainTransition);
}
```

### Step 7: Update Tests

#### 7.1 Update Unit Tests

**File**: `src/test/java/com/luscadevs/journeyorchestrator/unit/journey/StateTest.java`

**Action**: Add tests for ID generation and immutability

```java
@Test
void shouldAutoGenerateIdWhenNotProvided() {
    State state = State.builder()
        .name("TestState")
        .type(StateType.INTERMEDIATE)
        .build();
    
    assertNotNull(state.getId());
}

@Test
void shouldUseProvidedId() {
    UUID id = UUID.randomUUID();
    State state = State.builder()
        .id(id)
        .name("TestState")
        .type(StateType.INTERMEDIATE)
        .build();
    
    assertEquals(id, state.getId());
}

@Test
void shouldRejectIdChange() {
    State state = State.builder()
        .id(UUID.randomUUID())
        .name("TestState")
        .type(StateType.INTERMEDIATE)
        .build();
    
    assertThrows(IllegalStateException.class, () -> state.withId(UUID.randomUUID()));
}
```

#### 7.2 Update Integration Tests

**File**: `src/test/java/com/luscadevs/journeyorchestrator/integration/JourneyDefinitionServiceTest.java`

**Action**: Add tests for dual reference resolution

```java
@Test
void shouldResolveTransitionsById() {
    // Test ID-based transition resolution
}

@Test
void shouldResolveTransitionsByName() {
    // Test name-based transition resolution
}

@Test
void shouldResolveMixedTransitions() {
    // Test mixed reference patterns
}

@Test
void shouldRejectConflictingReferences() {
    // Test conflict detection
}
```

#### 7.3 Create E2E Tests

**File**: `src/test/java/com/luscadevs/journeyorchestrator/e2e/DualReferenceE2ETest.java`

**Action**: Create E2E test for dual reference patterns

```java
@SpringBootTest
@Testcontainers
class DualReferenceE2ETest {
    
    @Container
    static MongoDBContainer mongoDB = new MongoDBContainer("mongo:6.0");
    
    @Test
    void shouldCreateJourneyDefinitionWithIdBasedTransitions() {
        // E2E test for ID-based references
    }
    
    @Test
    void shouldCreateJourneyDefinitionWithNameBasedTransitions() {
        // E2E test for name-based references (legacy)
    }
    
    @Test
    void shouldCreateJourneyDefinitionWithMixedTransitions() {
        // E2E test for mixed references
    }
}
```

### Step 8: Run Tests

**Action**: Run all tests to verify implementation

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run E2E tests
mvn test -Dtest="**/e2e/**"

# Run all tests with coverage
mvn test jacoco:report
```

**Expected Results**:
- All unit tests pass
- All integration tests pass
- All E2E tests pass
- Coverage >= 90% for dual reference resolution logic (SC-003)

### Step 9: Performance Validation

**Action**: Validate performance targets

```bash
# Run E2E tests with performance metrics
mvn test -Dtest="LoadTest"
```

**Expected Results**:
- Journey definition creation time increase < 10% (SC-004)
- State reference resolution < 1ms (SC-005)
- Validation response time < 100ms (SC-010)

### Step 10: Backward Compatibility Verification

**Action**: Verify existing E2E tests pass without modification

```bash
# Run existing E2E test suite
mvn test -Dtest="**/e2e/**"
```

**Expected Results**:
- All existing E2E tests pass (SC-002)
- No breaking changes detected in OpenAPI comparison (SC-006)

## Verification Checklist

- [ ] OpenAPI specification updated with optional fields
- [ ] Code generation produces classes with new fields
- [ ] State entity has id and position fields
- [ ] Transition entity has sourceStateId and targetStateId fields
- [ ] StateReference value object created
- [ ] Position value object created
- [ ] JourneyDefinitionService resolves dual references
- [ ] MongoDB documents updated with new fields
- [ ] MongoDB indexes added for ID-based lookups
- [ ] Mappers updated for new fields
- [ ] Unit tests updated for new functionality
- [ ] Integration tests updated for dual reference resolution
- [ ] E2E tests created for dual reference patterns
- [ ] All tests pass
- [ ] Performance targets met
- [ ] Backward compatibility verified
- [ ] Coverage >= 90% for resolution logic

## Troubleshooting

### Issue: Generated classes don't include new fields

**Solution**: Ensure OpenAPI specification is correctly updated and run `mvn clean generate-sources`

### Issue: Validation errors for valid UUIDs

**Solution**: Verify UUID format validation logic and ensure UUID v4 format is used

### Issue: Performance targets not met

**Solution**: Profile reference resolution logic and optimize if needed. Consider caching state lookups.

### Issue: Existing E2E tests fail

**Solution**: Verify that new fields are optional in OpenAPI spec and that no breaking changes were introduced

## Next Steps

After completing this quickstart:
1. Review the implementation against success criteria in `spec.md`
2. Run full test suite including E2E tests
3. Create pull request for review
4. Update documentation if needed
5. Merge to main branch after approval

## Resources

- [Feature Specification](./spec.md)
- [Data Model](./data-model.md)
- [OpenAPI Evolution](./contracts/openapi-evolution.md)
- [Research Findings](./research.md)
- [Project Constitution](../../.specify/memory/constitution.md)
