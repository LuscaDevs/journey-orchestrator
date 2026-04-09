# Research Document: End-to-End Journey Testing Framework

**Created**: 2026-04-09  
**Purpose**: Research findings for technical decisions in E2E testing framework implementation

## RestAssured Integration Research

### Decision: Use RestAssured with Spring Boot Test Integration
**Rationale**: 
- RestAssured provides fluent DSL for API testing that integrates seamlessly with Spring Boot Test
- Supports JSON schema validation against OpenAPI specifications
- Excellent Spring Boot ecosystem integration with @SpringBootTest
- Widely adopted in enterprise Java projects with strong community support

**Alternatives Considered**:
- **Raw HTTP clients**: More verbose, less expressive, no built-in validation
- **WebTestClient**: Spring-specific but less suited for true E2E testing (runs in same process)
- **Postman/Newman**: External tool, harder to integrate with Java test suites, less flexible for dynamic test data

## Testcontainers Configuration Research

### Decision: Use MongoDB Testcontainers with Custom Configuration
**Rationale**:
- Provides isolated, reproducible test environments
- Eliminates test pollution between test runs
- Supports version-specific MongoDB testing matching production
- Integrates with Spring Boot Test via @Testcontainers and @DynamicPropertySource

**Configuration Approach**:
```java
@Testcontainers
public abstract class E2ETestBase {
    static final MongoDBContainer<?> mongo = new MongoDBContainer<>("mongo:7.0")
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("testcontainers/mongodb.conf"),
                "/etc/mongod.conf"
            );
    
    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }
}
```

## JUnit 5 Test Structure Research

### Decision: Use JUnit 5 with @Nested and @TestMethodOrder
**Rationale**:
- @Nested allows logical grouping of related test scenarios
- @TestMethodOrder ensures deterministic test execution for state-dependent tests
- Better support for parameterized tests and dynamic test generation
- Enhanced lifecycle management with @BeforeEach and @AfterEach

**Test Organization Strategy**:
```java
@Nested
@DisplayName("Complete Journey Lifecycle Tests")
@TestMethodOrder(OrderAnnotation.class)
class CompleteJourneyFlowTest extends JourneyTestBase {
    
    @Test
    @Order(1)
    @DisplayName("Should create journey definition successfully")
    void shouldCreateJourneyDefinition() { ... }
    
    @Test
    @Order(2)
    @DisplayName("Should start journey instance successfully")
    void shouldStartJourneyInstance() { ... }
}
```

## Performance Testing Approach Research

### Decision: Use JUnit 5 with Custom Performance Assertions
**Rationale**:
- Leverages existing test infrastructure without additional dependencies
- Custom assertions provide clear performance validation
- Integrates with existing test reporting mechanisms
- Avoids complexity of dedicated load testing tools for initial implementation

**Performance Assertion Pattern**:
```java
@Test
void shouldHandleConcurrentJourneyInstances() {
    PerformanceAssertions.assertThat(() -> 
        executeConcurrentJourneys(100)
    ).completesUnder(Duration.ofSeconds(30))
     .withAverageResponseTimeLessThan(Duration.ofSeconds(2))
     .withErrorRateLessThan(0.05);
}
```

## OpenAPI Contract Validation Research

### Decision: Use RestAssured with JsonSchemaValidator
**Rationale**:
- RestAssured provides built-in JSON schema validation
- Can generate schemas from OpenAPI specification
- Integrates seamlessly with existing test structure
- Provides clear validation error messages

**Validation Strategy**:
```java
// Generate JSON schema from OpenAPI
private static final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
private static final JsonSchema journeySchema = schemaFactory.getSchema(
    new File("src/test/resources/schemas/journey-definition.json")
);

@Test
void shouldValidateJourneyDefinitionAgainstSchema() {
    given()
        .spec(requestSpec)
        .body(journeyDefinition)
    .when()
        .post("/journeys")
    .then()
        .statusCode(201)
        .body(matchesJsonSchema(journeySchema));
}
```

## Test Data Management Research

### Decision: Use Builder Pattern with Fixtures
**Rationale**:
- Provides readable, maintainable test data creation
- Supports variation through builder methods
- Centralizes test data definitions
- Reduces duplication across test classes

**Fixture Pattern**:
```java
public class JourneyDefinitionFixtures {
    public static JourneyDefinitionBuilder simpleJourney() {
        return JourneyDefinitionBuilder.builder()
            .journeyCode("SIMPLE_JOURNEY")
            .name("Simple Test Journey")
            .version(1)
            .states(List.of(
                StateBuilder.initialState("START"),
                StateBuilder.finalState("END")
            ))
            .transitions(List.of(
                TransitionBuilder.builder()
                    .source("START")
                    .event("COMPLETE")
                    .target("END")
                    .build()
            ));
    }
    
    public static JourneyDefinitionBuilder conditionalJourney() {
        return simpleJourney()
            .journeyCode("CONDITIONAL_JOURNEY")
            .name("Conditional Test Journey")
            .transitions(List.of(
                TransitionBuilder.builder()
                    .source("START")
                    .event("PROCESS")
                    .target("APPROVED")
                    .condition("#eventData.amount > 1000")
                    .build(),
                TransitionBuilder.builder()
                    .source("START")
                    .event("PROCESS")
                    .target("REJECTED")
                    .condition("#eventData.amount <= 1000")
                    .build()
            ));
    }
}
```

## CI/CD Integration Research

### Decision: Use Maven Surefire/Failsafe with Test Profiles
**Rationale**:
- Integrates with existing Maven build system
- Separate test phases for unit vs E2E tests
- Supports test profile configuration
- Generates standard test reports for CI systems

**Maven Configuration**:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
            <configuration>
                <includes>
                    <include>**/*E2E*Test.java</include>
                </includes>
                <systemPropertyVariables>
                    <spring.profiles.active>e2e-test</spring.profiles.active>
                </systemPropertyVariables>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Test Environment Isolation Research

### Decision: Use Spring Test Profiles with Random Ports
**Rationale**:
- Ensures test isolation from development environment
- Supports parallel test execution
- Prevents port conflicts in CI environments
- Allows configuration of test-specific properties

**Isolation Strategy**:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e-test")
@Testcontainers
abstract class E2ETestBase {
    
    @LocalServerPort
    private int port;
    
    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }
}
```

## Conclusion

All technical decisions align with the project's architecture principles and existing technology stack. The chosen approach provides:

- **Clean Architecture**: Test framework maintains separation from production code
- **Specification-Driven**: Tests validate against OpenAPI contracts
- **Maintainability**: Clear structure with reusable components
- **Performance**: Efficient test execution with parallel support
- **CI/CD Ready**: Integrates with existing Maven build system

The research phase is complete with all technical decisions justified and alternatives evaluated.
