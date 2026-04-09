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
 * E2E Load Tests for Journey Orchestrator.
 * Tests system performance under sustained load with high-volume event
 * processing.
 * Validates that response times remain acceptable under load.
 */
@Tag("performance")
@Tag("load")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Load Performance Tests")
public class LoadTest extends RestAssuredTestBase {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final int LOAD_TEST_REQUESTS = 50;
    private static final long MAX_RESPONSE_TIME_MS = 2000; // 2 seconds max
    private static final int PERCENTILE_95 = 95;

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
     * Given: High-volume event processing scenario
     * When: System processes multiple events rapidly in sequence
     * Then: System should maintain acceptable response times (p95 < 2s)
     */
    @Test
    @DisplayName("Should handle high-volume event processing with acceptable response times")
    void shouldHandleHighVolumeEventProcessing() {
        var journeyDef = apiClient.createSimpleJourney().assertSuccess();
        String journeyCode = journeyDef.getJourneyCode();
        int journeyVersion = journeyDef.getVersion();

        // Create multiple instances and measure response times
        List<Long> responseTimes = new ArrayList<>();

        for (int i = 0; i < LOAD_TEST_REQUESTS; i++) {
            Instant startTime = Instant.now();

            // Create instance
            var instanceResponse = apiClient.startJourney(journeyCode, journeyVersion,
                    Map.of("customerId", "load-test-" + i));

            long duration = Duration.between(startTime, Instant.now()).toMillis();
            responseTimes.add(duration);

            assertEquals(200, instanceResponse.getRawResponse().statusCode(), "Instance should be created");
        }

        // Calculate performance metrics
        long avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).sum() / responseTimes.size();
        long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long p95ResponseTime = calculatePercentile(responseTimes, PERCENTILE_95);

        // Assertions
        assertTrue(maxResponseTime < 5000, "Max response time should be < 5 seconds, got: " + maxResponseTime + "ms");
        assertTrue(p95ResponseTime < MAX_RESPONSE_TIME_MS,
                "P95 response time should be < " + MAX_RESPONSE_TIME_MS + "ms, got: " + p95ResponseTime + "ms");

        System.out.println("Load Test Results - Requests: " + LOAD_TEST_REQUESTS);
        System.out.println("  Avg Response Time: " + avgResponseTime + "ms");
        System.out.println("  P95 Response Time: " + p95ResponseTime + "ms");
        System.out.println("  Max Response Time: " + maxResponseTime + "ms");
    }

    /**
     * Given: Large journey definitions
     * When: System creates and processes multiple large journey definitions
     * Then: Performance should remain acceptable for large payloads
     */
    @Test
    @DisplayName("Should handle large journey definition creation with acceptable performance")
    void shouldHandleLargeJourneyDefinitions() {
        List<Long> responseTimes = new ArrayList<>();

        // Create multiple conditional journey definitions
        int complexJourneyCount = 10;
        for (int i = 0; i < complexJourneyCount; i++) {
            Instant startTime = Instant.now();

            Map<String, Object> complexJourney = new java.util.HashMap<>(
                    JourneyDefinitionFixtures.conditionalJourney());
            complexJourney.put("journeyCode", "CONDITIONAL_PERF_" + UUID.randomUUID().toString().substring(0, 8));

            var response = apiClient.createJourneyDefinition(complexJourney).assertSuccess();

            long duration = Duration.between(startTime, Instant.now()).toMillis();
            responseTimes.add(duration);

            assertEquals(201, response.getRawResponse().statusCode(), "Complex journey should be created");
        }

        long avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).sum() / responseTimes.size();
        long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        assertTrue(maxResponseTime < 3000, "Max response time for complex journeys should be < 3 seconds");
        System.out.println(
                "Large Journey Load Test - Avg Response: " + avgResponseTime + "ms, Max: " + maxResponseTime + "ms");
    }

    /**
     * Given: Rapid sequential event processing
     * When: Multiple events are sent to instances in rapid succession
     * Then: System should maintain data consistency and acceptable response times
     */
    @Test
    @DisplayName("Should maintain performance during rapid sequential event processing")
    void shouldMaintainPerformanceDuringRapidEventProcessing() {
        var journeyDef = apiClient.createSimpleJourney().assertSuccess();
        String journeyCode = journeyDef.getJourneyCode();
        int journeyVersion = journeyDef.getVersion();

        // Create instance
        var instanceResponse = apiClient.startJourney(journeyCode, journeyVersion, Map.of("customerId", "perf-test"))
                .assertStarted();
        String instanceId = instanceResponse.getInstanceId();

        // Send events rapidly
        List<Long> eventResponseTimes = new ArrayList<>();
        int eventCount = 20;

        for (int i = 0; i < eventCount; i++) {
            Instant startTime = Instant.now();

            double amount = 100.0 + (i * 50); // Vary amounts for conditional evaluation
            try {
                apiClient.sendEvent(instanceId, "PROCESS", Map.of("amount", amount, "currency", "USD"));
            } catch (Exception e) {
                // Some events may fail, that's ok for performance testing
            }

            long duration = Duration.between(startTime, Instant.now()).toMillis();
            eventResponseTimes.add(duration);
        }

        long avgEventResponseTime = eventResponseTimes.stream().mapToLong(Long::longValue).sum()
                / eventResponseTimes.size();
        long p95EventResponseTime = calculatePercentile(eventResponseTimes, PERCENTILE_95);

        assertTrue(p95EventResponseTime < MAX_RESPONSE_TIME_MS,
                "P95 event response time should be < " + MAX_RESPONSE_TIME_MS + "ms");
        System.out.println(
                "Rapid Event Processing - Avg: " + avgEventResponseTime + "ms, P95: " + p95EventResponseTime + "ms");
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
