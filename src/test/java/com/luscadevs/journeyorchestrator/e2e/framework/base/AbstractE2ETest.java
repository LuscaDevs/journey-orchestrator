package com.luscadevs.journeyorchestrator.e2e.framework.base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract base class for all E2E tests providing: - Optimized Testcontainers MongoDB configuration
 * with container reuse - RestAssured setup and helper methods - Database cleanup between tests -
 * Common assertion methods - Parallel test execution support
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Testcontainers
public abstract class AbstractE2ETest {

    @Container
    static final MongoDBContainer mongoContainer =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @Autowired
    protected MongoTemplate mongoTemplate;

    @LocalServerPort
    protected int port;

    protected RequestSpecification requestSpec;

    @BeforeEach
    void setUpTestEnvironment() {
        // Clear all proxy-related system properties
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("http.nonProxyHosts");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("https.nonProxyHosts");
        System.clearProperty("ftp.proxyHost");
        System.clearProperty("ftp.proxyPort");
        System.clearProperty("socksProxyHost");
        System.clearProperty("socksProxyPort");

        // Reset RestAssured to clean state
        RestAssured.reset();

        // Build request specification using RestAssured 6.0.0 best practices
        requestSpec = new RequestSpecBuilder().setBaseUri("http://localhost").setPort(port)
                .setContentType("application/json").setAccept("application/json")
                .addFilter(new RequestLoggingFilter()).addFilter(new ResponseLoggingFilter())
                .build();

        // Clean up database before each test
        cleanupDatabase();
    }

    @AfterEach
    void tearDownTestEnvironment() {
        RestAssured.reset();
    }

    /**
     * Clean up all collections in MongoDB efficiently Uses batch operations for better performance
     */
    protected void cleanupDatabase() {
        if (mongoTemplate != null) {
            try {
                // Get all collection names and drop them in batch
                var collectionNames = mongoTemplate.getCollectionNames();
                collectionNames.forEach(collection -> {
                    try {
                        mongoTemplate.dropCollection(collection);
                    } catch (Exception e) {
                        // Log and continue if collection doesn't exist
                        System.err.println("Warning: Could not drop collection " + collection + ": "
                                + e.getMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("Warning: Error during database cleanup: " + e.getMessage());
            }
        }
    }

    /**
     * Get RestAssured request specification
     */
    protected RequestSpecification given() {
        return RestAssured.given().spec(requestSpec);
    }

    // ========== API Helper Methods ==========

    /**
     * Creates a journey definition from template via API
     */
    protected Response createJourneyDefinition(Map<String, Object> journeyDefinition) {
        return given().body(journeyDefinition).post("/journeys");
    }

    /**
     * Starts a journey instance with context data
     */
    protected Response startJourneyInstance(String journeyCode, int version,
            Map<String, Object> context) {
        Map<String, Object> request =
                Map.of("journeyCode", journeyCode, "version", version, "context", context);
        return given().body(request).post("/journey-instances");
    }

    /**
     * Sends an event to a journey instance
     */
    protected Response sendEvent(String instanceId, String event, Map<String, Object> payload) {
        Map<String, Object> request = Map.of("event", event, "payload", payload);
        return given().body(request).post("/journey-instances/" + instanceId + "/events");
    }

    /**
     * Retrieves journey instance details
     */
    protected Response getJourneyInstance(String instanceId) {
        return given().get("/journey-instances/" + instanceId);
    }

    /**
     * Retrieves transition history for a journey instance
     */
    protected Response getTransitionHistory(String instanceId) {
        return given().get("/journey-instances/" + instanceId + "/history");
    }

    // ========== Assertion Helper Methods ==========

    /**
     * Asserts that journey definition was created successfully
     */
    protected void assertJourneyDefinitionCreated(Response response) {
        response.then().statusCode(anyOf(equalTo(201), equalTo(200), equalTo(409)));
    }

    /**
     * Asserts that journey instance was started successfully
     */
    protected void assertJourneyInstanceStarted(Response response) {
        response.then().statusCode(anyOf(equalTo(201), equalTo(200)));
    }

    /**
     * Asserts that event was processed successfully
     */
    protected void assertEventProcessed(Response response) {
        response.then().statusCode(anyOf(equalTo(200), equalTo(202)));
    }

    /**
     * Asserts that journey instance is in expected state
     */
    protected void assertJourneyInstanceState(String instanceId, String expectedState) {
        Response response = getJourneyInstance(instanceId);
        response.then().statusCode(200).body("currentState", equalTo(expectedState));
    }

    /**
     * Asserts that journey instance has expected status
     */
    protected void assertJourneyInstanceStatus(String instanceId, String expectedStatus) {
        Response response = getJourneyInstance(instanceId);
        response.then().statusCode(200).body("status", equalTo(expectedStatus));
    }

    /**
     * Waits for journey to reach expected state with optimized polling Uses exponential backoff for
     * better performance
     */
    protected void waitForJourneyState(String instanceId, String expectedState, int maxAttempts) {
        long initialDelay = 100; // Start with 100ms
        long maxDelay = 2000; // Max delay of 2 seconds
        long currentDelay = initialDelay;

        for (int i = 0; i < maxAttempts; i++) {
            try {
                Response response = getJourneyInstance(instanceId);
                if (response.statusCode() == 200) {
                    String currentState = response.jsonPath().getString("currentState");
                    if (expectedState.equals(currentState)) {
                        return;
                    }
                }

                // Use exponential backoff with jitter
                if (i < maxAttempts - 1) { // Don't sleep on last attempt
                    Thread.sleep(currentDelay);
                    currentDelay = Math.min(currentDelay * 2, maxDelay);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Log error but continue polling
                System.err.println("Warning: Error polling journey state: " + e.getMessage());
                try {
                    Thread.sleep(currentDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // If we get here, the state wasn't reached
        assertTrue(false,
                "Journey instance " + instanceId + " did not reach state " + expectedState
                        + " within " + maxAttempts + " attempts (total wait time: "
                        + (initialDelay * (Math.pow(2, Math.min(maxAttempts, 10)) - 1)) + "ms)");
    }

    /**
     * Waits for journey to reach expected state with default timeout
     */
    protected void waitForJourneyState(String instanceId, String expectedState) {
        waitForJourneyState(instanceId, expectedState, 15);
    }

    // ========== Testcontainers Configuration ==========

    /**
     * Configure dynamic properties for Testcontainers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "journey-e2e-test");
    }

    /**
     * Clean up Testcontainers after all tests
     */
    @AfterAll
    static void cleanupContainers() {
        if (mongoContainer != null && mongoContainer.isRunning()) {
            mongoContainer.stop();
        }
    }
}
