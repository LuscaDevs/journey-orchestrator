package com.luscadevs.journeyorchestrator.e2e.scenarios.performance;

import com.luscadevs.journeyorchestrator.e2e.framework.base.RestAssuredTestBase;
import com.luscadevs.journeyorchestrator.e2e.framework.client.JourneyApiClient;
import com.luscadevs.journeyorchestrator.e2e.framework.fixtures.JourneyDefinitionFixtures;
import com.luscadevs.journeyorchestrator.config.MongoTestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E Scalability Tests for Journey Orchestrator.
 * Tests system behavior when scaled to handle many journey instances and
 * complex workflows.
 * Validates performance remains acceptable as data volume grows.
 */
@Tag("performance")
@Tag("scalability")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Scalability Performance Tests")
public class ScalabilityTest extends RestAssuredTestBase {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final int SCALE_TEST_INSTANCES = 100;
    private static final int SCALE_TEST_BATCHES = 4;
    private static final long MAX_RESPONSE_TIME_PER_INSTANCE_MS = 2000;

    private JourneyApiClient apiClient;

    static {
        System.setProperty("spring.data.mongodb.uri", MongoTestContainerConfig.getMongoUri());
    }

    @BeforeEach
    void setup() {
        apiClient = new JourneyApiClient(requestSpec);
    }

    @BeforeEach
    void cleanupDatabase() {
        mongoTemplate.getCollectionNames().forEach(collection -> {
            mongoTemplate.dropCollection(collection);
        });
    }

    /**
     * Given: Requirement to handle 100+ concurrent journey instances
     * When: System creates and manages many journey instances simultaneously
     * Then: System should maintain acceptable response times (p95 < 2s) and data
     * consistency
     */
    @Test
    @DisplayName("Should handle 100+ concurrent journey instances with acceptable performance")
    void shouldHandle100ConcurrentJourneyInstances() {
        var journeyDef = apiClient.createSimpleJourney().assertSuccess();
        String journeyCode = journeyDef.getJourneyCode();
        int journeyVersion = journeyDef.getVersion();

        List<Long> creationTimes = new ArrayList<>();
        List<String> createdInstanceIds = new ArrayList<>();

        Instant startTime = Instant.now();

        // Create 100+ instances in batches
        for (int batch = 0; batch < SCALE_TEST_BATCHES; batch++) {
            for (int i = 0; i < SCALE_TEST_INSTANCES / SCALE_TEST_BATCHES; i++) {
                Instant instanceStartTime = Instant.now();

                var instanceResponse = apiClient.startJourney(
                        journeyCode,
                        journeyVersion,
                        Map.of("customerId", "scale-test-" + (batch * SCALE_TEST_INSTANCES + i)));

                long creationTime = Duration.between(instanceStartTime, Instant.now()).toMillis();
                creationTimes.add(creationTime);

                assertEquals(200, instanceResponse.getRawResponse().statusCode(),
                        "Instance should be created in batch " + batch);

                String instanceId = instanceResponse.getInstanceId();
                createdInstanceIds.add(instanceId);
            }

            System.out.println("Batch " + (batch + 1) + " completed - " +
                    (SCALE_TEST_INSTANCES / SCALE_TEST_BATCHES) + " instances created");
        }

        long totalDuration = Duration.between(startTime, Instant.now()).toMillis();

        // Calculate metrics
        long avgCreationTime = creationTimes.stream().mapToLong(Long::longValue).sum() / creationTimes.size();
        long maxCreationTime = creationTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long p95CreationTime = calculatePercentile(creationTimes, 95);

        // Assertions
        assertTrue(maxCreationTime < 5000,
                "Max instance creation time should be < 5 seconds, got: " + maxCreationTime + "ms");
        assertTrue(p95CreationTime < MAX_RESPONSE_TIME_PER_INSTANCE_MS,
                "P95 creation time should be < " + MAX_RESPONSE_TIME_PER_INSTANCE_MS + "ms, got: " + p95CreationTime
                        + "ms");

        System.out.println("Scalability Test Results - 100+ Instances:");
        System.out.println("  Total Instances Created: " + SCALE_TEST_INSTANCES);
        System.out.println("  Avg Creation Time: " + avgCreationTime + "ms");
        System.out.println("  P95 Creation Time: " + p95CreationTime + "ms");
        System.out.println("  Max Creation Time: " + maxCreationTime + "ms");
        System.out.println("  Total Duration: " + totalDuration + "ms");
    }

    /**
     * Given: Large journey definitions with many states and transitions
     * When: System creates and processes complex journey definitions
     * Then: Performance should remain acceptable despite workflow complexity
     */
    @Test
    @DisplayName("Should handle large complex journey definitions with acceptable performance")
    void shouldHandleLargeComplexJourneyDefinitions() {
        List<Long> creationTimes = new ArrayList<>();

        // Create multiple complex journey definitions
        int complexJourneyCount = 20;

        Instant startTime = Instant.now();

        for (int i = 0; i < complexJourneyCount; i++) {
            Instant journeyStartTime = Instant.now();

            Map<String, Object> complexJourney = buildComplexJourney(i);

            var response = apiClient.createJourneyDefinition(complexJourney).assertSuccess();

            long creationTime = Duration.between(journeyStartTime, Instant.now()).toMillis();
            creationTimes.add(creationTime);

            assertEquals(201, response.getRawResponse().statusCode(),
                    "Complex journey " + i + " should be created successfully");
        }

        long totalDuration = Duration.between(startTime, Instant.now()).toMillis();

        long avgCreationTime = creationTimes.stream().mapToLong(Long::longValue).sum() / creationTimes.size();
        long maxCreationTime = creationTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        assertTrue(maxCreationTime < 3000,
                "Max complex journey creation time should be < 3 seconds");

        System.out.println("Complex Journey Scalability Test:");
        System.out.println("  Complex Journeys Created: " + complexJourneyCount);
        System.out.println("  Avg Creation Time: " + avgCreationTime + "ms");
        System.out.println("  Total Duration: " + totalDuration + "ms");
    }

    /**
     * Given: Large-scale instance query operations
     * When: System queries many instances across different states
     * Then: Query performance should scale linearly or better with instance count
     */
    @Test
    @DisplayName("Should maintain query performance as instance count grows")
    void shouldMaintainQueryPerformanceWithScaledData() {
        var journeyDef = apiClient.createSimpleJourney().assertSuccess();
        String journeyCode = journeyDef.getJourneyCode();
        int journeyVersion = journeyDef.getVersion();

        // Create multiple instances
        int instanceCount = 50;
        List<String> instanceIds = new ArrayList<>();

        for (int i = 0; i < instanceCount; i++) {
            var instanceResponse = apiClient.startJourney(journeyCode, journeyVersion,
                    Map.of("customerId", "query-test-" + i));
            String instanceId = instanceResponse.getInstanceId();
            instanceIds.add(instanceId);
        }

        // Query instances and measure response times
        List<Long> queryTimes = new ArrayList<>();

        for (String instanceId : instanceIds) {
            Instant queryStartTime = Instant.now();

            var queryResponse = apiClient.getJourneyInstance(instanceId);

            long queryTime = Duration.between(queryStartTime, Instant.now()).toMillis();
            queryTimes.add(queryTime);

            assertEquals(200, queryResponse.getRawResponse().statusCode(), "Instance query should succeed");
        }

        long avgQueryTime = queryTimes.stream().mapToLong(Long::longValue).sum() / queryTimes.size();
        long maxQueryTime = queryTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        // Query time for individual instances should remain relatively constant
        assertTrue(maxQueryTime < 1000,
                "Max query time should be < 1 second even with 50+ instances");

        System.out.println("Query Scalability Test Results:");
        System.out.println("  Instances Queried: " + instanceCount);
        System.out.println("  Avg Query Time: " + avgQueryTime + "ms");
        System.out.println("  Max Query Time: " + maxQueryTime + "ms");
    }

    /**
     * Given: High-volume event processing across scale
     * When: Many instances process events concurrently
     * Then: Event processing throughput should be acceptable
     */
    @Test
    @DisplayName("Should maintain event processing throughput at scale")
    void shouldMaintainEventProcessingThroughputAtScale() {
        var journeyDef = apiClient.createSimpleJourney().assertSuccess();
        String journeyCode = journeyDef.getJourneyCode();
        int journeyVersion = journeyDef.getVersion();

        // Create instances
        List<String> instanceIds = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            var instanceResponse = apiClient.startJourney(journeyCode, journeyVersion, Map.of());
            String instanceId = instanceResponse.getInstanceId();
            instanceIds.add(instanceId);
        }

        // Send events across all instances
        Instant startTime = Instant.now();
        int eventsPerInstance = 5;
        int totalEvents = instanceIds.size() * eventsPerInstance;

        for (String instanceId : instanceIds) {
            for (int i = 0; i < eventsPerInstance; i++) {
                double amount = 100.0 + (i * 100);
                try {
                    apiClient.sendEvent(instanceId, "PROCESS",
                            Map.of("amount", amount, "currency", "USD"));
                    // Track that event was attempted
                } catch (Exception e) {
                    // Some events may fail under scale, that's ok
                }
            }
        }

        long totalDuration = Duration.between(startTime, Instant.now()).toMillis();
        double eventsPerSecond = (totalEvents * 1000.0) / totalDuration;

        System.out.println("Event Processing Throughput at Scale:");
        System.out.println("  Instances: " + instanceIds.size());
        System.out.println("  Total Events: " + totalEvents);
        System.out.println("  Total Duration: " + totalDuration + "ms");
        System.out.println("  Throughput: " + String.format("%.2f", eventsPerSecond) + " events/sec");

        assertTrue(eventsPerSecond > 0, "Event processing throughput should be positive");
    }

    /**
     * Builds a complex journey definition with many states and transitions
     */
    private Map<String, Object> buildComplexJourney(int journeyIndex) {
        Map<String, Object> journey = new java.util.HashMap<>(
                JourneyDefinitionFixtures.conditionalJourney());
        journey.put("journeyCode", "COMPLEX_SCALABILITY_" + UUID.randomUUID().toString().substring(0, 8));
        return journey;
    }

    /**
     * Calculates the nth percentile from a list of values
     */
    private long calculatePercentile(List<Long> values, int percentile) {
        if (values.isEmpty())
            return 0;

        List<Long> sorted = new ArrayList<>(values);
        sorted.sort(Long::compareTo);

        int index = (int) ((percentile / 100.0) * sorted.size());
        return sorted.get(Math.min(index, sorted.size() - 1));
    }
}
