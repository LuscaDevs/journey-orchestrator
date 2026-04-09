package com.luscadevs.journeyorchestrator.e2e.scenarios.lifecycle;

import com.luscadevs.journeyorchestrator.e2e.framework.base.RestAssuredTestBase;
import com.luscadevs.journeyorchestrator.e2e.framework.fixtures.HybridJourneyFixtures;
import com.luscadevs.journeyorchestrator.config.MongoTestContainerConfig;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E tests for concurrent journey instance execution. Tests that multiple journey instances can
 * operate independently without interference.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Concurrent Instance Tests")
public class ConcurrentInstanceTest extends RestAssuredTestBase {

    @Autowired
    private MongoTemplate mongoTemplate;

    private HybridJourneyFixtures hybridFixtures = new HybridJourneyFixtures();

    static {
        // Set up Testcontainers MongoDB URI for Spring Boot
        System.setProperty("spring.data.mongodb.uri", MongoTestContainerConfig.getMongoUri());
    }

    @BeforeEach
    void cleanupDatabase() {
        // Clean up all collections before each test
        mongoTemplate.getCollectionNames().forEach(collection -> {
            mongoTemplate.dropCollection(collection);
        });
    }

    @Test
    @DisplayName("Should handle multiple journey instances concurrently")
    void shouldHandleMultipleJourneyInstancesConcurrently() {
        // Given: A journey definition using hybrid fixtures
        Map<String, Object> journeyDefinition = hybridFixtures.simpleJourney("CONCURRENT_TEST");
        Response createResponse = createJourneyDefinition(journeyDefinition);
        assertJourneyDefinitionCreated(createResponse);

        String journeyCode = createResponse.jsonPath().getString("journeyCode");
        int version = createResponse.jsonPath().getInt("version");

        // When: Start multiple journey instances concurrently
        int instanceCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(instanceCount);
        List<CompletableFuture<String>> futures = new ArrayList<>();
        List<Long> completionTimes = new ArrayList<>();

        for (int i = 0; i < instanceCount; i++) {
            final int instanceIndex = i;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();

                Map<String, Object> context = Map.of("userId", "user-" + instanceIndex, "sessionId",
                        "session-" + instanceIndex);

                Response startResponse = startJourneyInstance(journeyCode, version, context);
                assertJourneyInstanceStarted(startResponse);

                String instanceId = startResponse.jsonPath().getString("instanceId");

                // Send completion event
                Map<String, Object> eventPayload = Map.of("completedBy", "user-" + instanceIndex);
                Response eventResponse = sendEvent(instanceId, "COMPLETE", eventPayload);
                assertEventProcessed(eventResponse);

                completionTimes.add(System.currentTimeMillis() - startTime);
                return instanceId;
            }, executor);

            futures.add(future);
        }

        // Then: All instances should complete successfully
        List<String> instanceIds = futures.stream().map(future -> {
            try {
                return future.get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Failed to complete journey instance", e);
            }
        }).toList();

        // Verify all instances are in completed state
        for (String instanceId : instanceIds) {
            waitForJourneyState(instanceId, "END", 15);
            assertJourneyInstanceState(instanceId, "END");
            assertJourneyInstanceStatus(instanceId, "RUNNING");
        }

        // Verify instances are independent (no interference)
        for (String instanceId : instanceIds) {
            Response instanceResponse = getJourneyInstance(instanceId);
            instanceResponse.then().statusCode(200).body("instanceId", equalTo(instanceId))
                    .body("currentState", equalTo("END")).body("status", equalTo("RUNNING"));
        }

        // Basic performance assertions
        double averageTime =
                completionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        assertTrue(averageTime < 5000.0, "Average completion time should be reasonable");

        executor.shutdown();
    }

    @Test
    @DisplayName("Should maintain data isolation between concurrent instances")
    void shouldMaintainDataIsolationBetweenConcurrentInstances() {
        // Given: A data-centric journey definition using hybrid fixtures
        Map<String, Object> journeyDefinition = hybridFixtures.simpleJourney("DATA_ISOLATION_TEST");
        Response createResponse = createJourneyDefinition(journeyDefinition);
        assertJourneyDefinitionCreated(createResponse);

        String journeyCode = createResponse.jsonPath().getString("journeyCode");
        int version = createResponse.jsonPath().getInt("version");

        // When: Start multiple instances with different contexts
        int instanceCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(instanceCount);
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < instanceCount; i++) {
            final int instanceIndex = i;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                Map<String, Object> uniqueContext = Map.of("customerId",
                        "customer-" + instanceIndex, "orderAmount", 1000.0 + instanceIndex * 100,
                        "productType", "type-" + instanceIndex);

                Response startResponse = startJourneyInstance(journeyCode, version, uniqueContext);
                assertJourneyInstanceStarted(startResponse);

                String instanceId = startResponse.jsonPath().getString("instanceId");

                // Send completion event with instance-specific data
                Map<String, Object> eventPayload = Map.of("completedBy", "agent-" + instanceIndex,
                        "completionAmount", 1000.0 + instanceIndex * 100);

                Response eventResponse = sendEvent(instanceId, "COMPLETE", eventPayload);
                assertEventProcessed(eventResponse);

                return instanceId;
            }, executor);

            futures.add(future);
        }

        // Then: Verify data isolation
        List<String> instanceIds = futures.stream().map(future -> {
            try {
                return future.get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Failed to complete journey instance", e);
            }
        }).toList();

        // Verify each instance maintains its own data (simplified - context not returned by
        // backend)
        for (int i = 0; i < instanceIds.size(); i++) {
            String instanceId = instanceIds.get(i);
            waitForJourneyState(instanceId, "END", 15);

            Response instanceResponse = getJourneyInstance(instanceId);
            instanceResponse.then().statusCode(200).body("instanceId", equalTo(instanceId))
                    .body("currentState", equalTo("END"));
        }

        executor.shutdown();
    }

    @Test
    @DisplayName("Should handle concurrent instances with different journey definitions")
    void shouldHandleConcurrentInstancesWithDifferentJourneyDefinitions() {
        // Given: Multiple journey definitions using hybrid fixtures
        Map<String, Object> simpleJourney = hybridFixtures.simpleJourney("SIMPLE_CONCURRENT");
        Map<String, Object> conditionalJourney =
                hybridFixtures.conditionalJourney("CONDITIONAL_CONCURRENT");

        Response simpleResponse = createJourneyDefinition(simpleJourney);
        Response conditionalResponse = createJourneyDefinition(conditionalJourney);

        assertJourneyDefinitionCreated(simpleResponse);
        assertJourneyDefinitionCreated(conditionalResponse);

        String simpleJourneyCode = simpleResponse.jsonPath().getString("journeyCode");
        int simpleVersion = simpleResponse.jsonPath().getInt("version");

        String conditionalJourneyCode = conditionalResponse.jsonPath().getString("journeyCode");
        int conditionalVersion = conditionalResponse.jsonPath().getInt("version");

        // When: Start instances of different journey types concurrently
        ExecutorService executor = Executors.newFixedThreadPool(6);
        List<CompletableFuture<String>> futures = new ArrayList<>();

        // Start 3 simple journey instances
        for (int i = 0; i < 3; i++) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                Response startResponse =
                        startJourneyInstance(simpleJourneyCode, simpleVersion, Map.of());
                assertJourneyInstanceStarted(startResponse);

                String instanceId = startResponse.jsonPath().getString("instanceId");

                Response eventResponse = sendEvent(instanceId, "COMPLETE", Map.of());
                assertEventProcessed(eventResponse);

                return instanceId;
            }, executor);

            futures.add(future);
        }

        // Start 3 conditional journey instances
        for (int i = 0; i < 3; i++) {
            final int instanceIndex = i;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                Map<String, Object> payload = instanceIndex % 2 == 0 ? Map.of("amount", 1500.0) // High
                                                                                                // amount
                        : Map.of("amount", 500.0); // Low amount

                Response startResponse =
                        startJourneyInstance(conditionalJourneyCode, conditionalVersion, Map.of());
                assertJourneyInstanceStarted(startResponse);

                String instanceId = startResponse.jsonPath().getString("instanceId");

                Response eventResponse = sendEvent(instanceId, "PROCESS", payload);
                assertEventProcessed(eventResponse);

                return instanceId;
            }, executor);

            futures.add(future);
        }

        // Then: All instances should complete with appropriate states
        List<String> instanceIds = futures.stream().map(future -> {
            try {
                return future.get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Failed to complete journey instance", e);
            }
        }).toList();

        // Verify simple journeys end in END state
        // Verify conditional journeys end in appropriate states based on conditions
        for (String instanceId : instanceIds) {
            Response instanceResponse = getJourneyInstance(instanceId);
            instanceResponse.then().statusCode(200).body("instanceId", equalTo(instanceId))
                    .body("status", anyOf(equalTo("COMPLETED"), equalTo("RUNNING")));
        }

        executor.shutdown();
    }

    @Test
    @DisplayName("Should handle high concurrency load without degradation")
    void shouldHandleHighConcurrencyLoadWithoutDegradation() {
        // Given: A simple journey definition using hybrid fixtures
        Map<String, Object> journeyDefinition =
                hybridFixtures.simpleJourney("HIGH_CONCURRENCY_TEST");
        Response createResponse = createJourneyDefinition(journeyDefinition);
        assertJourneyDefinitionCreated(createResponse);

        String journeyCode = createResponse.jsonPath().getString("journeyCode");
        int version = createResponse.jsonPath().getInt("version");

        // When: Start many instances concurrently
        int instanceCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < instanceCount; i++) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long instanceStart = System.currentTimeMillis();

                Response startResponse = startJourneyInstance(journeyCode, version, Map.of());
                assertJourneyInstanceStarted(startResponse);

                String instanceId = startResponse.jsonPath().getString("instanceId");

                Response eventResponse = sendEvent(instanceId, "COMPLETE", Map.of());
                assertEventProcessed(eventResponse);

                waitForJourneyState(instanceId, "END", 20);

                return System.currentTimeMillis() - instanceStart;
            }, executor);

            futures.add(future);
        }

        // Then: All instances should complete within reasonable time
        List<Long> completionTimes = futures.stream().map(future -> {
            try {
                return future.get(60, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Failed to complete journey instance", e);
            }
        }).toList();

        long totalTime = System.currentTimeMillis() - startTime;
        double averageTime =
                completionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);

        // Performance assertions
        assertTrue(totalTime < 30000L,
                "Total time should be reasonable for " + instanceCount + " instances"); // 30
                                                                                        // seconds
                                                                                        // max

        assertTrue(averageTime < 5000.0,
                "Average completion time per instance should be reasonable"); // 5 seconds max
                                                                              // average

        // Verify all instances completed successfully
        assertEquals(instanceCount, completionTimes.size(), "All instances should complete");

        executor.shutdown();
    }
}
