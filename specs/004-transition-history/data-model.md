# Data Model: Transition History Tracking

**Created**: 2026-03-28  
**Purpose**: Domain entities and data structures for transition history feature

## Core Domain Entities

### TransitionHistoryEvent

Represents a single state transition record with complete context.

```java
@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class TransitionHistoryEvent {
    private final TransitionHistoryEventId id;
    private final JourneyInstanceId instanceId;
    private final State fromState;
    private final State toState;
    private final Event event;
    private final Instant timestamp;
    private final Map<String, Object> metadata;
    
    // Domain behavior
    public boolean isAfter(TransitionHistoryEvent other) {
        return this.timestamp.isAfter(other.timestamp);
    }
    
    public boolean hasEventType(String eventType) {
        return this.event.getType().equals(eventType);
    }
}
```

**Fields**:
- `id`: Unique identifier for the history event
- `instanceId`: Reference to the journey instance
- `fromState`: Previous state before transition
- `toState`: New state after transition
- `event`: The event that triggered the transition
- `timestamp`: When the transition occurred (nanosecond precision)
- `metadata`: Additional context information (flexible JSON structure)

### TransitionHistoryEventId

Value object for history event identification.

```java
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class TransitionHistoryEventId {
    private final String value;
    
    public static TransitionHistoryEventId generate() {
        return new TransitionHistoryEventId(UUID.randomUUID().toString());
    }
    
    public static TransitionHistoryEventId of(String value) {
        return new TransitionHistoryEventId(value);
    }
}
```

### JourneyInstanceId

Existing value object for journey instance identification.

```java
// Existing entity - enhanced with history relationship
@Getter
@Builder
public class JourneyInstance {
    private final JourneyInstanceId id;
    private final JourneyDefinitionId definitionId;
    private final State currentState;
    private final Instant createdAt;
    private final Instant updatedAt;
    
    // Enhanced behavior
    public List<TransitionHistoryEvent> getTransitionHistory(
            TransitionHistoryRepositoryPort repository) {
        return repository.findByInstanceIdOrderByTimestampAsc(this.id);
    }
}
```

## Repository Interfaces

### TransitionHistoryRepositoryPort

Interface for transition history persistence operations.

```java
public interface TransitionHistoryRepositoryPort {
    void save(TransitionHistoryEvent event);
    List<TransitionHistoryEvent> findByInstanceIdOrderByTimestampAsc(
            JourneyInstanceId instanceId);
    List<TransitionHistoryEvent> findByInstanceIdAndTimestampBetween(
            JourneyInstanceId instanceId, Instant start, Instant end);
    List<TransitionHistoryEvent> findByInstanceIdAndEventType(
            JourneyInstanceId instanceId, String eventType);
    boolean existsByInstanceId(JourneyInstanceId instanceId);
    void deleteByInstanceId(JourneyInstanceId instanceId); // Soft delete
}
```

## MongoDB Documents

### TransitionHistoryDocument

MongoDB document representation for persistence.

```java
@Data
@Builder
@Document(collection = "transition_history")
@CompoundIndex(name = "instance_timestamp_idx", def = "{'instanceId': 1, 'timestamp': 1}")
@CompoundIndex(name = "timestamp_idx", def = "{'timestamp': 1}")
public class TransitionHistoryDocument {
    @Id
    private String id;
    private String instanceId;
    private String fromState;
    private String toState;
    private String eventType;
    private String eventData;
    private Instant timestamp;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant deletedAt; // For soft deletion
    
    @Indexed(expireAfterSeconds = 0) // TTL for cleanup
    private Instant expiresAt;
}
```

## Data Validation Rules

### TransitionHistoryEvent Validation

```java
public class TransitionHistoryEventValidator {
    public void validate(TransitionHistoryEvent event) {
        Objects.requireNonNull(event.getId(), "Event ID is required");
        Objects.requireNonNull(event.getInstanceId(), "Instance ID is required");
        Objects.requireNonNull(event.getFromState(), "From state is required");
        Objects.requireNonNull(event.getToState(), "To state is required");
        Objects.requireNonNull(event.getEvent(), "Event is required");
        Objects.requireNonNull(event.getTimestamp(), "Timestamp is required");
        
        if (event.getMetadata() == null) {
            event = event.toBuilder().metadata(Map.of()).build();
        }
        
        validateMetadataSize(event.getMetadata());
        validateStateTransition(event.getFromState(), event.getToState());
    }
    
    private void validateMetadataSize(Map<String, Object> metadata) {
        // Implement size validation (e.g., < 1MB)
    }
    
    private void validateStateTransition(State from, State to) {
        // Validate that transition is valid according to journey definition
    }
}
```

## Data Mappers

### TransitionHistoryMapper

Maps between domain entities and MongoDB documents.

```java
@Component
public class TransitionHistoryMapper {
    
    public TransitionHistoryDocument toDocument(TransitionHistoryEvent event) {
        return TransitionHistoryDocument.builder()
                .id(event.getId().getValue())
                .instanceId(event.getInstanceId().getValue())
                .fromState(event.getFromState().getName())
                .toState(event.getToState().getName())
                .eventType(event.getEvent().getType())
                .eventData(event.getEvent().getData())
                .timestamp(event.getTimestamp())
                .metadata(event.getMetadata())
                .createdAt(Instant.now())
                .build();
    }
    
    public TransitionHistoryEvent toDomain(TransitionHistoryDocument document) {
        return TransitionHistoryEvent.builder()
                .id(TransitionHistoryEventId.of(document.getId()))
                .instanceId(JourneyInstanceId.of(document.getInstanceId()))
                .fromState(State.of(document.getFromState()))
                .toState(State.of(document.getToState()))
                .event(Event.of(document.getEventType(), document.getEventData()))
                .timestamp(document.getTimestamp())
                .metadata(document.getMetadata())
                .build();
    }
}
```

## Performance Considerations

### Indexing Strategy

1. **Primary Query Index**: `{ instanceId: 1, timestamp: 1 }`
   - Supports main use case: retrieve history by instance in chronological order
   - Covers filtering and sorting in single index

2. **Time-based Index**: `{ timestamp: 1 }`
   - Supports time-based cleanup and monitoring queries
   - Useful for TTL operations

3. **Event Type Index**: `{ instanceId: 1, eventType: 1, timestamp: 1 }`
   - Supports filtered queries by event type
   - Optional, add based on query patterns

### Storage Optimization

1. **Document Size**: Target < 1KB per history event
2. **Metadata Limits**: Enforce application-level limits
3. **Compression**: Consider MongoDB compression for large collections

### Query Optimization

1. **Projection**: Return only required fields
2. **Pagination**: Use cursor-based pagination for large histories
3. **Caching**: Cache recent history events in memory

## Security Considerations

### Data Access Control

1. **Field-level Security**: Sensitive metadata protection
2. **Audit Logging**: Track access to history data
3. **Rate Limiting**: Prevent abuse of history endpoints

### Data Privacy

1. **PII Handling**: Special handling for personal data in metadata
2. **Retention Policies**: Automatic cleanup of old data
3. **Data Anonymization**: Options for long-term storage

## Relationships

### Entity Relationship Diagram

```
JourneyInstance (1) -----> (N) TransitionHistoryEvent
    |                                    |
    |                                    |
    v                                    v
JourneyDefinition                 Event
```

### Key Relationships

1. **JourneyInstance → TransitionHistoryEvent**: One-to-many
2. **TransitionHistoryEvent → Event**: Many-to-one (event type reference)
3. **TransitionHistoryEvent → State**: References for from/to states

## Evolution Considerations

### Versioning

1. **Schema Version**: Include version in documents for migration
2. **Backward Compatibility**: Maintain compatibility with older data
3. **Migration Strategy**: Plan for schema evolution

### Extensibility

1. **Metadata Schema**: Flexible structure for future enhancements
2. **Event Types**: Support for new event types without schema changes
3. **Query Patterns**: Prepare for additional query patterns
