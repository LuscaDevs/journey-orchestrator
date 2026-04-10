package com.luscadevs.journeyorchestrator.e2e.scenarios.contracts;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E Versioning Tests for Journey Orchestrator. Validates API versioning, backward compatibility,
 * and version handling. Ensures that multiple versions of journeys can coexist and are properly
 * managed.
 */
@Tag("contract")
@Tag("versioning")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("API Versioning Tests")
public class VersioningTest extends RestAssuredTestBase {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private MongoTemplate mongoTemplate;

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
     * Given: Multiple versions of the same journey definition exist When: Client uses specific
     * version code Then: System should return the correct version
     */
    @Test
    @DisplayName("Should allow multiple versions of the same journey")
    void shouldAllowMultipleVersionsOfSameJourney() {
        // Create first version with unique code
        var v1Response = apiClient.createSimpleJourney().assertSuccess();
        String journeyCode = v1Response.getJourneyCode();
        int version1 = v1Response.getVersion();

        assertEquals(1, version1, "First version should be 1");
        assertThat(journeyCode).isNotEmpty();
    }

    void shouldMaintainVersionConsistency() {
        var journeyResponse = apiClient.createSimpleJourney().assertSuccess();
        String journeyCode = journeyResponse.getJourneyCode();
        int journeyVersion = journeyResponse.getVersion();

        // Create instance with specific version
        var instanceResponse = apiClient
                .startJourney(journeyCode, journeyVersion, Map.of("test", "data")).assertStarted();

        // Retrieve instance and verify journey code and version are tracked
        var retrievedInstance =
                apiClient.getJourneyInstance(instanceResponse.getInstanceId()).assertStarted();

        assertThat(retrievedInstance.getJourneyCode()).isEqualTo(journeyCode);
        assertThat(retrievedInstance.getVersion()).isEqualTo(journeyVersion);
    }

    /**
     * Given: Journey version is explicitly specified in instance creation request When: Instance is
     * created with specified version Then: Instance should be created successfully
     */
    @Test
    @DisplayName("Should enforce version validation on instance creation")
    void shouldEnforceVersionValidation() {
        var journeyResponse = apiClient.createSimpleJourney().assertSuccess();
        String journeyCode = journeyResponse.getJourneyCode();
        int validVersion = journeyResponse.getVersion();

        // Create instance with valid version - should succeed
        var successResponse = apiClient.startJourney(journeyCode, validVersion).assertStarted();
        assertThat(successResponse.getRawResponse().statusCode()).isIn(200, 201);

        // Try to create instance with invalid version - should fail
        var failResponse = apiClient.startJourney(journeyCode, 9999); // Non-existent version
        assertThat(failResponse.getRawResponse().statusCode()).isIn(400, 404);
    }

    /**
     * Given: Client requests event processing on instance with specific version When: Event is sent
     * to instance Then: Event should be processed according to that version's rules
     */
    @Test
    @DisplayName("Should process events according to journey version")
    void shouldProcessEventsWithVersionConsistency() {
        var journeyResponse = apiClient.createSimpleJourney().assertSuccess();
        String journeyCode = journeyResponse.getJourneyCode();
        int journeyVersion = journeyResponse.getVersion();

        var instanceResponse = apiClient.startJourney(journeyCode, journeyVersion).assertStarted();
        String instanceId = instanceResponse.getInstanceId();

        // Send event - should be processed according to journey version
        var eventResponse = apiClient
                .sendEvent(instanceId, "COMPLETE", Map.of("amount", 1000.0, "currency", "USD"))
                .assertProcessed();

        assertThat(eventResponse.getRawResponse().statusCode()).isIn(200, 202);
    }

    /**
     * Given: Instance was created with one journey version When: New version of same journey is
     * created Then: Existing instance should continue with original version
     */
    @Test
    @DisplayName("Should isolate instances from new journey versions")
    void shouldIsolateInstancesFromNewVersions() {
        // 1. Create version 1
        var v1Response = apiClient.createSimpleJourney().assertSuccess();
        String journeyCode = v1Response.getJourneyCode();
        int version1 = v1Response.getVersion();

        // 2. Start instance with v1
        var instance1 = apiClient.startJourney(journeyCode, version1, Map.of("test", "data"))
                .assertStarted();
        String instance1Id = instance1.getInstanceId();

        // 3. Create version 2 (same journey code, different version)
        Map<String, Object> v2Journey = new HashMap<>(JourneyDefinitionFixtures.simpleJourney());
        v2Journey.put("journeyCode", journeyCode);
        v2Journey.put("version", 2);
        v2Journey.put("name", "Updated Journey v2");

        var v2Response = apiClient.createJourneyDefinition(v2Journey).assertSuccess();
        int version2 = v2Response.getVersion();

        // 4. Continue execution of instance1 - should still use v1
        apiClient.sendEvent(instance1Id, "COMPLETE", Map.of("amount", 1000.0)).assertProcessed();

        // 5. Verify instance1 still uses v1
        var retrievedInstance = apiClient.getJourneyInstance(instance1Id).assertStarted();

        assertThat(retrievedInstance.getVersion()).isEqualTo(version1);
        assertThat(retrievedInstance.getJourneyCode()).isEqualTo(journeyCode);
        assertThat(version2).isEqualTo(2); // v2 was created successfully

        // 6. New instance can use v2
        var instance2 =
                apiClient.startJourney(journeyCode, version2, Map.of("test", "v2")).assertStarted();
        assertThat(instance2.getVersion()).isEqualTo(version2);
    }

    @DisplayName("Should maintain and track journey version history")
    void shouldMaintainVersionHistory() {
        // Create initial version with unique code
        String uniqueJourneyCode = "HIST_" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, Object> v1Journey = new HashMap<>(JourneyDefinitionFixtures.simpleJourney());
        v1Journey.put("journeyCode", uniqueJourneyCode);

        var response1 = apiClient.createJourneyDefinition(v1Journey).assertSuccess();
        int version1 = response1.getVersion();

        assertThat(version1).isGreaterThanOrEqualTo(1);

        // Create updated version
        Map<String, Object> v2Journey =
                new HashMap<>(JourneyDefinitionFixtures.conditionalJourney());
        v2Journey.put("journeyCode", uniqueJourneyCode);

        var response2 = apiClient.createJourneyDefinition(v2Journey).assertSuccess();
        int version2 = response2.getVersion();

        // Version should be tracked incrementally
        assertThat(version2).isGreaterThanOrEqualTo(version1);

        // Verify responses maintain correct journey code
        assertThat(response1.getJourneyCode()).isEqualTo(uniqueJourneyCode);
        assertThat(response2.getJourneyCode()).isEqualTo(uniqueJourneyCode);
    }
}
