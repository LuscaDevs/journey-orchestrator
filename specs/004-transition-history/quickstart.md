# Quick Start Guide: Transition History Tracking

**Created**: 2026-03-28  
**Purpose**: Quick start guide for implementing and using transition history feature

## Overview

The Transition History Tracking feature provides comprehensive audit trail functionality for journey instances. Every state transition is automatically recorded with complete context, and the history can be retrieved via REST API endpoints.

## Key Features

- **Automatic History Creation**: Every state transition creates a persistent history event
- **Complete Context**: Captures fromState, toState, event, timestamp, and metadata
- **Chronological Ordering**: Events are returned in exact chronological order
- **Flexible Querying**: Support for date range and event type filtering
- **High Performance**: Optimized for large-scale journey instances

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Layer     │    │ Application     │    │   Domain Layer  │
│                 │    │     Layer       │    │                 │
│ Controllers     │───▶│ Services        │───▶│ Entities        │
│ DTOs            │    │ Ports           │    │ Business Logic  │
│ Mappers         │    │ Use Cases       │    │ Validation      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │ Infrastructure  │
                       │     Layer       │
                       │                 │
                       │ Repositories    │
                       │ MongoDB         │
                       │ External APIs   │
                       └─────────────────┘
```

## Implementation Steps

### 1. Update OpenAPI Specification

Add the new history endpoint to your existing OpenAPI spec:

```yaml
/journey-instances/{id}/history:
  get:
    summary: Get transition history for a journey instance
    # ... (see contracts/openapi.yaml for full specification)
```

### 2. Generate Code

Run Maven to generate API stubs:

```bash
mvn generate-sources
```

### 3. Implement Domain Layer

Create the core domain entities:

```java
// TransitionHistoryEvent.java
@Getter
@Builder
public class TransitionHistoryEvent {
    private final TransitionHistoryEventId id;
    private final JourneyInstanceId instanceId;
    private final State fromState;
    private final State toState;
    private final Event event;
    private final Instant timestamp;
    private final Map<String, Object> metadata;
}
```

### 4. Implement Repository Port

Define the repository interface:

```java
public interface TransitionHistoryRepositoryPort {
    void save(TransitionHistoryEvent event);
    List<TransitionHistoryEvent> findByInstanceIdOrderByTimestampAsc(
            JourneyInstanceId instanceId);
    // ... other query methods
}
```

### 5. Implement Application Service

Create the service layer:

```java
@Service
public class TransitionHistoryService {
    private final TransitionHistoryRepositoryPort repository;
    
    public void recordTransition(JourneyInstanceId instanceId, 
                                State fromState, 
                                State toState, 
                                Event event) {
        TransitionHistoryEvent historyEvent = TransitionHistoryEvent.builder()
                .id(TransitionHistoryEventId.generate())
                .instanceId(instanceId)
                .fromState(fromState)
                .toState(toState)
                .event(event)
                .timestamp(Instant.now())
                .metadata(extractMetadata(event))
                .build();
                
        repository.save(historyEvent);
    }
}
```

### 6. Implement MongoDB Adapter

Create the MongoDB repository implementation:

```java
@Repository
public class TransitionHistoryMongoRepository 
        implements TransitionHistoryRepositoryPort {
    
    private final MongoTemplate mongoTemplate;
    private final TransitionHistoryMapper mapper;
    
    @Override
    public void save(TransitionHistoryEvent event) {
        TransitionHistoryDocument document = mapper.toDocument(event);
        mongoTemplate.save(document);
    }
    
    @Override
    public List<TransitionHistoryEvent> findByInstanceIdOrderByTimestampAsc(
            JourneyInstanceId instanceId) {
        Query query = Query.query(
            Criteria.where("instanceId").is(instanceId.getValue())
        ).with(Sort.by(Sort.Direction.ASC, "timestamp"));
        
        List<TransitionHistoryDocument> documents = 
            mongoTemplate.find(query, TransitionHistoryDocument.class);
            
        return documents.stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
```

### 7. Implement Web Controller

Create the REST endpoint:

```java
@RestController
@RequestMapping("/journey-instances")
public class JourneyInstanceController {
    
    private final TransitionHistoryService historyService;
    private final TransitionHistoryMapper mapper;
    
    @GetMapping("/{id}/history")
    public ResponseEntity<TransitionHistoryListResponse> getHistory(
            @PathVariable String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String eventType,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        JourneyInstanceId instanceId = JourneyInstanceId.of(id);
        
        List<TransitionHistoryEvent> events = historyService.getHistory(
            instanceId, from, to, eventType, limit, offset);
            
        TransitionHistoryListResponse response = TransitionHistoryListResponse.builder()
                .instanceId(id)
                .events(events.stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList()))
                .pagination(createPaginationInfo(limit, offset, events.size()))
                .totalCount(historyService.getHistoryCount(instanceId))
                .build();
                
        return ResponseEntity.ok(response);
    }
}
```

## Usage Examples

### Recording History Events

```java
// In your existing journey state transition logic
@Service
public class JourneyInstanceService {
    private final TransitionHistoryService historyService;
    
    public void applyEvent(JourneyInstanceId instanceId, Event event) {
        JourneyInstance instance = findById(instanceId);
        State fromState = instance.getCurrentState();
        State toState = calculateNewState(instance, event);
        
        // Update journey instance state
        instance = instance.toBuilder()
            .currentState(toState)
            .updatedAt(Instant.now())
            .build();
            
        save(instance);
        
        // Record transition history
        historyService.recordTransition(instanceId, fromState, toState, event);
    }
}
```

### Retrieving History

```bash
# Get complete history for a journey instance
curl -H "Authorization: Bearer <token>" \
     "http://localhost:8080/journey-instances/123e4567-e89b-12d3-a456-426614174000/history"

# Get history with date filtering
curl -H "Authorization: Bearer <token>" \
     "http://localhost:8080/journey-instances/123e4567-e89b-12d3-a456-426614174000/history?from=2024-01-01T00:00:00Z&to=2024-01-31T23:59:59Z"

# Get history filtered by event type
curl -H "Authorization: Bearer <token>" \
     "http://localhost:8080/journey-instances/123e4567-e89b-12d3-a456-426614174000/history?eventType=USER_ACTION"

# Get paginated history
curl -H "Authorization: Bearer <token>" \
     "http://localhost:8080/journey-instances/123e4567-e89b-12d3-a456-426614174000/history?limit=50&offset=100"
```

## Configuration

### MongoDB Indexes

Create the necessary indexes for optimal performance:

```javascript
// Create composite index for primary queries
db.transition_history.createIndex(
    { "instanceId": 1, "timestamp": 1 },
    { name: "instance_timestamp_idx" }
);

// Create timestamp index for TTL operations
db.transition_history.createIndex(
    { "timestamp": 1 },
    { name: "timestamp_idx", expireAfterSeconds: 31536000 } // 1 year TTL
);

// Create event type index for filtered queries
db.transition_history.createIndex(
    { "instanceId": 1, "eventType": 1, "timestamp": 1 },
    { name: "instance_event_timestamp_idx" }
);
```

### Application Properties

```yaml
# application.yml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/journey-orchestrator
      
# History-specific configuration
journey:
  history:
    max-metadata-size: 1048576  # 1MB
    default-limit: 100
    max-limit: 1000
    retention-days: 365
```

## Testing

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class TransitionHistoryServiceTest {
    
    @Mock
    private TransitionHistoryRepositoryPort repository;
    
    @InjectMocks
    private TransitionHistoryService service;
    
    @Test
    void shouldRecordTransition() {
        // Given
        JourneyInstanceId instanceId = JourneyInstanceId.of("test-id");
        State fromState = State.of("initial");
        State toState = State.of("processing");
        Event event = Event.of("START", "{}");
        
        // When
        service.recordTransition(instanceId, fromState, toState, event);
        
        // Then
        verify(repository).save(any(TransitionHistoryEvent.class));
    }
}
```

### Integration Tests

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.database=test_journey_orchestrator"
})
class TransitionHistoryIntegrationTest {
    
    @Autowired
    private TransitionHistoryService service;
    
    @Test
    void shouldPersistAndRetrieveHistory() {
        // Given
        JourneyInstanceId instanceId = JourneyInstanceId.of("test-id");
        
        // When
        service.recordTransition(instanceId, 
            State.of("initial"), 
            State.of("processing"), 
            Event.of("START", "{}"));
            
        // Then
        List<TransitionHistoryEvent> history = 
            service.getHistory(instanceId, null, null, null, 100, 0);
            
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getFromState().getName()).isEqualTo("initial");
        assertThat(history.get(0).getToState().getName()).isEqualTo("processing");
    }
}
```

## Performance Considerations

### Monitoring

Monitor these key metrics:

- History creation latency (target: <100ms)
- History query response time (target: <500ms)
- Database index usage
- Storage growth rate

### Optimization Tips

1. **Use Projections**: Return only required fields in queries
2. **Implement Caching**: Cache recent history events in memory
3. **Batch Operations**: Consider batch inserts for high-volume scenarios
4. **Regular Cleanup**: Use TTL indexes for automatic cleanup

## Troubleshooting

### Common Issues

1. **Slow Queries**: Check that indexes are being used
2. **Missing Events**: Verify that history recording is called in all transition paths
3. **Large Documents**: Monitor metadata size to avoid document size limits
4. **Memory Issues**: Implement pagination for large histories

### Debug Logging

Enable debug logging for troubleshooting:

```yaml
logging:
  level:
    com.luscadevs.journeyorchestrator.domain.journeyinstance: DEBUG
    com.luscadevs.journeyorchestrator.application.service: DEBUG
    org.springframework.data.mongodb: DEBUG
```

## Next Steps

1. **Implement the feature** following the steps above
2. **Add comprehensive tests** for all scenarios
3. **Set up monitoring** for performance metrics
4. **Configure retention policies** based on your requirements
5. **Document your specific use cases** and integration points

For additional information, refer to:
- [Data Model Documentation](data-model.md)
- [API Contract](contracts/openapi.yaml)
- [Research Findings](research.md)
