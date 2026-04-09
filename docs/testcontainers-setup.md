# Testcontainers MongoDB Setup Guide

## Overview

This guide explains how to set up and use Testcontainers MongoDB for E2E testing in the Journey Orchestrator project. Testcontainers provides isolated, containerized database instances for each test run, ensuring proper test isolation and reliability.

## Architecture

### Components

1. **MongoTestContainerConfig** - Manages MongoDB container lifecycle
2. **Hybrid Fixtures** - JSON + Java approach for test data
3. **Database Cleanup** - Automatic cleanup between tests
4. **Test Structure** - Consistent Spring Boot test configuration

## Setup Instructions

### 1. Testcontainers Configuration

```java
// MongoTestContainerConfig.java
public class MongoTestContainerConfig {
    private static final MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7.0");
    
    static {
        mongoContainer.start();
    }
    
    public static String getMongoUri() {
        return mongoContainer.getReplicaSetUrl();
    }
}
```

### 2. Test Class Structure

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(Lifecycle.PER_CLASS)
public class YourE2ETest extends RestAssuredTestBase {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    static {
        System.setProperty("spring.data.mongodb.uri", 
            MongoTestContainerConfig.getMongoUri());
    }
    
    @BeforeEach
    void cleanupDatabase() {
        mongoTemplate.getCollectionNames().forEach(collection -> {
            mongoTemplate.dropCollection(collection);
        });
    }
}
```

## Hybrid Fixtures

### JSON Files

Place JSON fixture files in `src/test/resources/fixtures/journey-definitions/`:

```json
// simple-journey.json
{
  "journeyCode": "SIMPLE_JOURNEY",
  "name": "Simple Test Journey",
  "version": 1,
  "active": true,
  "states": [
    {
      "name": "START",
      "type": "INITIAL",
      "description": "Initial state of the journey"
    },
    {
      "name": "END",
      "type": "FINAL",
      "description": "Final state of the journey"
    }
  ],
  "transitions": [
    {
      "source": "START",
      "event": "COMPLETE",
      "target": "END",
      "description": "Transition to end state"
    }
  ]
}
```

### Java API

```java
// HybridJourneyFixtures.java
public class HybridJourneyFixtures {
    private final JsonFixtureLoader jsonLoader = new JsonFixtureLoader();
    
    public Map<String, Object> simpleJourney() {
        return jsonLoader.loadValidJourneyDefinition("simple-journey");
    }
    
    public Map<String, Object> simpleJourney(String journeyCode) {
        return jsonLoader.createJourneyVariation("simple-journey", 
            Map.of("journeyCode", journeyCode));
    }
}
```

## Best Practices

### 1. Test Isolation

- Always use `@DirtiesContext` to ensure clean context between tests
- Implement `@BeforeEach` database cleanup
- Use unique journey codes for each test

### 2. Fixture Management

- Keep JSON fixtures simple and readable
- Use Java API for dynamic variations
- Validate fixture structure in JsonFixtureLoader

### 3. Performance Considerations

- Container startup happens once per test class
- Database cleanup is fast and efficient
- Use appropriate timeouts for async operations

### 4. Error Handling

- Use proper assertions for API responses
- Handle container startup failures gracefully
- Log container information for debugging

## Troubleshooting

### Port Conflicts

If you encounter port conflicts, ensure you're using `WebEnvironment.RANDOM_PORT`:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
```

### Container Issues

If MongoDB container fails to start:

1. Check Docker is running
2. Verify MongoDB image is available: `docker pull mongo:7.0`
3. Check system resources

### Test Failures

Common issues and solutions:

1. **422 Errors**: Check event types match journey transitions
2. **500 Errors**: Verify journey definition structure
3. **Timeout Issues**: Increase wait times for async operations

## Migration Guide

### From In-Memory Tests

1. Add Testcontainers dependency
2. Create MongoTestContainerConfig
3. Update test classes with proper annotations
4. Implement database cleanup
5. Replace in-memory fixtures with hybrid fixtures

### From Local MongoDB

1. Remove local MongoDB dependencies
2. Add Testcontainers configuration
3. Update connection strings to use container URI
4. Add database cleanup logic
5. Update test profiles

## Examples

### Basic E2E Test

```java
@Test
@DisplayName("Should create and complete journey")
void shouldCreateAndCompleteJourney() {
    // Given: Journey definition using hybrid fixtures
    Map<String, Object> journeyDefinition = hybridFixtures.simpleJourney("TEST_JOURNEY");
    Response createResponse = createJourneyDefinition(journeyDefinition);
    assertJourneyDefinitionCreated(createResponse);
    
    // When: Start journey instance
    String journeyCode = createResponse.jsonPath().getString("journeyCode");
    int version = createResponse.jsonPath().getInt("version");
    
    Response startResponse = startJourneyInstance(journeyCode, version, Map.of());
    assertJourneyInstanceStarted(startResponse);
    
    String instanceId = startResponse.jsonPath().getString("instanceId");
    
    // When: Send completion event
    Map<String, Object> eventPayload = Map.of("completedBy", "test-user");
    Response eventResponse = sendEvent(instanceId, "COMPLETE", eventPayload);
    assertEventProcessed(eventResponse);
    
    // Then: Journey should be completed
    waitForJourneyState(instanceId, "END", 10);
    assertJourneyInstanceState(instanceId, "END");
}
```

## Conclusion

Testcontainers MongoDB provides a robust, isolated testing environment that ensures reliable E2E tests. The hybrid fixture approach combines the best of JSON readability with Java type safety, making tests maintainable and easy to understand.

Following this guide will help you create consistent, reliable E2E tests that properly validate the Journey Orchestrator functionality.
