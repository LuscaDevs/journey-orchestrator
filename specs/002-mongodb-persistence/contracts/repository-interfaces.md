# Repository Interface Contracts

**Feature**: 002-mongodb-persistence  
**Date**: 2026-03-26  

## Repository Port Interfaces

These interfaces define the contracts that MongoDB adapters must implement. They remain unchanged from the existing memory adapters to maintain architectural consistency.

### JourneyDefinitionRepositoryPort

```java
package com.luscadevs.journeyorchestrator.application.port;

import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import java.util.List;
import java.util.Optional;

public interface JourneyDefinitionRepositoryPort {
    
    /**
     * Save a journey definition
     * @param journeyDefinition the journey definition to save
     * @return the saved journey definition
     */
    JourneyDefinition save(JourneyDefinition journeyDefinition);
    
    /**
     * Find a journey definition by ID
     * @param id the journey definition ID
     * @return the journey definition if found
     */
    Optional<JourneyDefinition> findById(String id);
    
    /**
     * Find a journey definition by name and version
     * @param name the journey definition name
     * @param version the journey definition version
     * @return the journey definition if found
     */
    Optional<JourneyDefinition> findByNameAndVersion(String name, String version);
    
    /**
     * Find all journey definitions
     * @return list of all journey definitions
     */
    List<JourneyDefinition> findAll();
    
    /**
     * Delete a journey definition by ID
     * @param id the journey definition ID
     */
    void deleteById(String id);
    
    /**
     * Check if a journey definition exists by ID
     * @param id the journey definition ID
     * @return true if exists, false otherwise
     */
    boolean existsById(String id);
}
```

### JourneyInstanceRepositoryPort

```java
package com.luscadevs.journeyorchestrator.application.port;

import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;
import java.util.List;
import java.util.Optional;

public interface JourneyInstanceRepositoryPort {
    
    /**
     * Save a journey instance
     * @param journeyInstance the journey instance to save
     * @return the saved journey instance
     */
    JourneyInstance save(JourneyInstance journeyInstance);
    
    /**
     * Find a journey instance by ID
     * @param id the journey instance ID
     * @return the journey instance if found
     */
    Optional<JourneyInstance> findById(String id);
    
    /**
     * Find all journey instances for a journey definition
     * @param journeyDefinitionId the journey definition ID
     * @return list of journey instances
     */
    List<JourneyInstance> findByJourneyDefinitionId(String journeyDefinitionId);
    
    /**
     * Find journey instances by current state
     * @param state the current state
     * @return list of journey instances
     */
    List<JourneyInstance> findByCurrentState(String state);
    
    /**
     * Find journey instances by status
     * @param status the journey instance status
     * @return list of journey instances
     */
    List<JourneyInstance> findByStatus(String status);
    
    /**
     * Find all journey instances
     * @return list of all journey instances
     */
    List<JourneyInstance> findAll();
    
    /**
     * Delete a journey instance by ID
     * @param id the journey instance ID
     */
    void deleteById(String id);
    
    /**
     * Check if a journey instance exists by ID
     * @param id the journey instance ID
     * @return true if exists, false otherwise
     */
    boolean existsById(String id);
}
```

### EventRepositoryPort

```java
package com.luscadevs.journeyorchestrator.application.port;

import com.luscadevs.journeyorchestrator.domain.engine.Event;
import java.util.List;

public interface EventRepositoryPort {
    
    /**
     * Save an event
     * @param event the event to save
     * @return the saved event
     */
    Event save(Event event);
    
    /**
     * Find events by journey instance ID
     * @param journeyInstanceId the journey instance ID
     * @return list of events for the journey instance
     */
    List<Event> findByJourneyInstanceId(String journeyInstanceId);
    
    /**
     * Find events by event type
     * @param eventType the event type
     * @return list of events of the specified type
     */
    List<Event> findByEventType(String eventType);
    
    /**
     * Find events within a time range
     * @param startTime the start time
     * @param endTime the end time
     * @return list of events within the time range
     */
    List<Event> findByTimestampBetween(java.time.LocalDateTime startTime, 
                                       java.time.LocalDateTime endTime);
    
    /**
     * Find all events
     * @return list of all events
     */
    List<Event> findAll();
}
```

## Configuration Interface

### MongoConfigurationPort

```java
package com.luscadevs.journeyorchestrator.application.port;

public interface MongoConfigurationPort {
    
    /**
     * Get MongoDB connection URI
     * @return the MongoDB URI
     */
    String getMongoUri();
    
    /**
     * Get MongoDB database name
     * @return the database name
     */
    String getDatabaseName();
    
    /**
     * Check if auto-index creation is enabled
     * @return true if auto-index creation is enabled
     */
    boolean isAutoIndexCreation();
    
    /**
     * Get connection timeout in milliseconds
     * @return connection timeout
     */
    int getConnectionTimeout();
    
    /**
     * Get maximum connection pool size
     * @return maximum pool size
     */
    int getMaxPoolSize();
}
```

## Contract Guarantees

### Performance Guarantees
- Repository operations complete within 100ms for standard queries
- Connection establishment completes within 5 seconds
- Support for 1000 concurrent operations

### Reliability Guarantees
- All write operations are atomic
- Read operations return consistent data
- Automatic retry for transient failures
- Comprehensive error reporting

### Security Guarantees
- Connection strings are securely managed
- No sensitive data in logs
- Access control through MongoDB roles
- Audit trail for all data operations

## Implementation Requirements

### MongoDB Adapter Requirements
- Implement all repository port interfaces
- Use Spring Data MongoDB for persistence
- Provide proper exception handling
- Support connection pooling and retry
- Include comprehensive logging

### Configuration Requirements
- Support Spring Boot configuration properties
- Environment-specific configuration profiles
- Validation of configuration values
- Graceful handling of missing configuration

### Testing Requirements
- Unit tests for all repository methods
- Integration tests with Testcontainers
- Performance tests for query optimization
- Error handling and recovery tests
