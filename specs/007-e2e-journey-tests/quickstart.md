# Quickstart Guide: E2E Journey Testing Framework

**Created**: 2026-04-09  
**Purpose**: Quick start guide for using the E2E testing framework

## Prerequisites

### Development Environment

- Java 21 (LTS) installed
- Maven 3.8+ installed
- Docker and Docker Compose installed (for Testcontainers)
- IDE with Java support (IntelliJ IDEA recommended)

### Project Dependencies

The framework requires these additional dependencies (to be added to `pom.xml`):

```xml
<dependencies>
    <!-- RestAssured for API testing -->
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <version>5.4.0</version>
        <scope>test</scope>
    </dependency>

    <!-- JSON Schema validation -->
    <dependency>
        <groupId>com.networknt</groupId>
        <artifactId>json-schema-validator</artifactId>
        <version>1.0.87</version>
        <scope>test</scope>
    </dependency>

    <!-- Performance testing support -->
    <dependency>
        <groupId>org.awaitility</groupId>
        <artifactId>awaitility</artifactId>
        <version>4.2.0</version>
        <scope>test</scope>
    </dependency>

    <!-- Testcontainers (already included, ensure version compatibility) -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>mongodb</artifactId>
        <version>1.19.7</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Getting Started

### 1. Create Your First E2E Test

Create a new test class extending `JourneyTestBase`:

```java
package com.luscadevs.journeyorchestrator.e2e.scenarios.lifecycle;

import com.luscadevs.journeyorchestrator.e2e.framework.base.JourneyTestBase;
import com.luscadevs.journeyorchestrator.e2e.framework.fixtures.JourneyDefinitionFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@Nested
@DisplayName("Simple Journey Flow Tests")
@TestMethodOrder(OrderAnnotation.class)
class SimpleJourneyFlowTest extends JourneyTestBase {

    @Test
    @Order(1)
    @DisplayName("Should create and execute simple journey successfully")
    void shouldCreateAndExecuteSimpleJourney() {
        // Arrange: Create journey definition from fixture
        var journeyDefinition = JourneyDefinitionFixtures.simpleJourney().create();

        // Act: Create journey definition via API
        var createdJourney = createJourneyDefinition(journeyDefinition);

        // Assert: Journey was created successfully
        assertThat(createdJourney.getStatusCode()).isEqualTo(201);
        assertThat(createdJourney.getBody().getJourneyCode()).isEqualTo("SIMPLE_JOURNEY");

        // Act: Start journey instance
        var instance = startJourneyInstance(
            createdJourney.getBody().getJourneyCode(),
            createdJourney.getBody().getVersion(),
            Map.of("customerId", "test-customer-123")
        );

        // Assert: Instance started in initial state
        assertThat(instance.getStatusCode()).isEqualTo(201);
        assertThat(instance.getBody().getCurrentState()).isEqualTo("START");

        // Act: Send completion event
        var updatedInstance = sendEvent(
            instance.getBody().getInstanceId(),
            "COMPLETE",
            Map.of("completedBy", "test-user")
        );

        // Assert: Journey completed successfully
        assertThat(updatedInstance.getStatusCode()).isEqualTo(200);
        assertThat(updatedInstance.getBody().getCurrentState()).isEqualTo("END");
        assertThat(updatedInstance.getBody().getStatus()).isEqualTo("COMPLETED");
    }
}
```

### 2. Run Tests Locally

```bash
# Run all E2E tests
mvn failsafe:integration-test -Dspring.profiles.active=e2e-test

# Run specific test class
mvn failsafe:integration-test -Dspring.profiles.active=e2e-test -Dit.test=SimpleJourneyFlowTest

# Run with performance monitoring
mvn failsafe:integration-test -Dspring.profiles.active=e2e-test -Dperformance.monitoring=true
```

### 3. View Test Reports

After test execution, reports are generated in:

- HTML Report: `target/failsafe-reports/e2e-report.html`
- JSON Report: `target/failsafe-reports/e2e-report.json`
- Performance Metrics: `target/failsafe-reports/performance-metrics.json`

## Test Scenarios

### Journey Lifecycle Testing

Test complete journey workflows from creation to completion:

```java
@Test
@DisplayName("Should handle conditional journey transitions")
void shouldHandleConditionalTransitions() {
    // Create conditional journey
    var conditionalJourney = JourneyDefinitionFixtures.conditionalJourney()
        .withVariable("amount", 1500) // High amount
        .create();

    var createdJourney = createJourneyDefinition(conditionalJourney);
    var instance = startJourneyInstance(createdJourney.getBody().getJourneyCode(),
                                   createdJourney.getBody().getVersion(),
                                   Map.of());

    // Send processing event
    var updatedInstance = sendEvent(instance.getBody().getInstanceId(), "PROCESS",
                               Map.of("amount", 1500, "priority", "HIGH"));

    // Should transition to APPROVED due to condition
    assertThat(updatedInstance.getBody().getCurrentState()).isEqualTo("APPROVED");
}
```

### Error Handling Testing

Test error scenarios and validation:

```java
@Test
@DisplayName("Should reject invalid journey definition")
void shouldRejectInvalidJourneyDefinition() {
    var invalidJourney = JourneyDefinitionFixtures.invalidJourney().create();

    var response = createJourneyDefinition(invalidJourney);

    assertThat(response.getStatusCode()).isEqualTo(400);
    assertThat(response.getBody().getError().getCode()).isEqualTo("INVALID_JOURNEY_DEFINITION");
}
```

### Performance Testing

Test performance characteristics:

```java
@Test
@DisplayName("Should handle concurrent journey instances")
void shouldHandleConcurrentInstances() {
    PerformanceAssertions.assertThat(() -> {
        // Execute 100 journey instances concurrently
        executeConcurrentJourneys(100);
    }).completesUnder(Duration.ofSeconds(30))
     .withAverageResponseTimeLessThan(Duration.ofSeconds(2))
     .withErrorRateLessThan(0.05);
}
```

### API Contract Testing

Validate API compliance:

```java
@Test
@DisplayName("Should validate journey definition against OpenAPI schema")
void shouldValidateJourneyDefinitionSchema() {
    var journeyDefinition = JourneyDefinitionFixtures.simpleJourney().create();

    given()
        .spec(getRequestSpecification())
        .body(journeyDefinition)
    .when()
        .post("/journeys")
    .then()
        .statusCode(201)
        .body(matchesJsonSchema(journeyDefinitionSchema));
}
```

## Test Data Management

### Using Fixtures

```java
// Load predefined fixture
var simpleJourney = JourneyDefinitionFixtures.simpleJourney().create();

// Customize fixture
var customJourney = JourneyDefinitionFixtures.simpleJourney()
    .withName("Custom Test Journey")
    .withJourneyCode("CUSTOM_JOURNEY")
    .withAdditionalState("REVIEW", StateType.INTERMEDIATE)
    .create();

// Create multiple instances
var journeys = JourneyDefinitionFixtures.simpleJourney().createMultiple(10);
```

### Event Payload Templates

```java
// Load event payload template
var approvalEvent = EventPayloadFixtures.approvalEvent()
    .withAmount(5000)
    .withApprover("manager-123")
    .withReason("High value approval")
    .create();

// Send custom event
var response = sendEvent(instanceId, "APPROVE", approvalEvent);
```

## Configuration

### Test Profiles

Create `src/test/resources/application-e2e.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/test}
      database: journey-e2e-test

  test:
    database:
      replace: none # Use Testcontainers instead

e2e:
  framework:
    performance:
      enabled: true
      thresholds:
        max-response-time: 2s
        max-error-rate: 0.05
        min-throughput: 10

    reporting:
      enabled: true
      formats: [HTML, JSON]
      output-dir: target/e2e-reports

    cleanup:
      auto-cleanup: true
      isolation-level: METHOD
```

### Testcontainers Configuration

Create `src/test/resources/testcontainers/mongodb.conf`:

```yaml
net:
  port: 27017
storage:
  dbPath: /data/db
  journal:
    enabled: true
systemLog:
  destination: file
  path: /var/log/mongodb/mongod.log
  logAppend: true
```

## Best Practices

## Test Coverage with JaCoCo

This project uses the **JaCoCo** Maven plugin for code coverage. There is no need to implement or maintain a custom CoverageAnalyzer, as JaCoCo already covers all requirements for E2E, integration, and unit test coverage.

**How to generate the coverage report:**

```bash
mvn verify
# or
mvn test
# The report will be available at target/site/jacoco/index.html
```

**Why JaCoCo?**

- Industry standard, auditable, and fully integrated with Maven.
- Generates detailed reports (line, branch, method coverage).
- Compatible with CI/CD pipelines and IDEs.
- Avoids duplicated logic and extra maintenance.

**When to consider something custom?**

- Only if you have coverage requirements not supported by JaCoCo (e.g., mutation testing, dynamic contract coverage, etc.), which is not the case for this project.

### Test Organization

- Group related tests in nested classes
- Use descriptive display names
- Order tests logically when dependencies exist
- Keep tests independent and isolated

### Data Management

- Use fixtures for reusable test data
- Generate unique IDs to avoid conflicts
- Clean up test data after each test
- Use appropriate isolation levels

### Performance Testing

- Set realistic performance thresholds
- Test with realistic data volumes
- Monitor resource usage
- Run performance tests in dedicated environments

### Error Handling

- Test both happy path and error scenarios
- Validate error responses and messages
- Test edge cases and boundary conditions
- Ensure graceful degradation

### CI/CD Integration

- Run E2E tests in dedicated stage
- Use parallel execution for faster feedback
- Generate and archive test reports
- Fail build on performance regressions

## Troubleshooting

### Common Issues

**Testcontainers Fails to Start**

```bash
# Ensure Docker is running
docker --version
docker ps

# Check Docker daemon
sudo systemctl status docker
```

**Port Conflicts**

```bash
# Kill processes using test ports
lsof -ti:8080 | xargs kill -9

# Use random ports in configuration
spring.profiles.active=e2e-test,random-ports
```

**Memory Issues**

```bash
# Increase JVM memory for tests
export MAVEN_OPTS="-Xmx2g -Xms1g"

# Run tests with reduced parallelism
mvn failsafe:integration-test -Dparallel.tests=2
```

### Debug Mode

Enable debug logging:

```bash
mvn failsafe:integration-test -Dspring.profiles.active=e2e-test,debug
```

### Test Isolation Issues

Check test data cleanup:

```java
@AfterEach
void verifyCleanup() {
    // Verify no test data leakage
    var remainingData = testDataManager.getRemainingTestData();
    assertThat(remainingData).isEmpty();
}
```

## Next Steps

1. **Create Test Scenarios**: Define test cases for your specific journeys
2. **Add Fixtures**: Create reusable test data for your domain
3. **Configure Performance**: Set appropriate performance thresholds
4. **Integrate with CI**: Add E2E tests to your CI/CD pipeline
5. **Monitor Results**: Review test reports and performance metrics

For more detailed information, refer to:

- [Data Model Documentation](data-model.md)
- [API Contracts](contracts/e2e-framework-api.md)
- [Implementation Tasks](tasks.md) (generated by `/speckit.tasks`)
