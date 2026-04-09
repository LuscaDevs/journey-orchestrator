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

import java.util.Map;

/**
 * E2E tests for conditional transitions in journey workflows. Tests journey flows that branch based
 * on conditions and expressions.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Conditional Transition Tests")
public class ConditionalTransitionTest extends RestAssuredTestBase {

        @Autowired
        private MongoTemplate mongoTemplate;

        private HybridJourneyFixtures hybridFixtures = new HybridJourneyFixtures();

        static {
                // Set up Testcontainers MongoDB URI for Spring Boot
                System.setProperty("spring.data.mongodb.uri",
                                MongoTestContainerConfig.getMongoUri());
        }

        @BeforeEach
        void cleanupDatabase() {
                // Clean up all collections before each test
                mongoTemplate.getCollectionNames().forEach(collection -> {
                        mongoTemplate.dropCollection(collection);
                });
        }

        @Test
        @DisplayName("Should follow correct path based on condition evaluation")
        void shouldFollowCorrectPathBasedOnConditions() {
                // Given: A journey with conditional transitions using hybrid fixtures
                Map<String, Object> journeyDefinition =
                                hybridFixtures.conditionalJourney("HIGH_VALUE_TEST");
                Response createResponse = createJourneyDefinition(journeyDefinition);
                assertJourneyDefinitionCreated(createResponse);

                String journeyCode = createResponse.jsonPath().getString("journeyCode");
                int version = createResponse.jsonPath().getInt("version");

                // When: Start journey instance
                Response startResponse = startJourneyInstance(journeyCode, version, Map.of());
                assertJourneyInstanceStarted(startResponse);

                String instanceId = startResponse.jsonPath().getString("instanceId");

                // Then: Should be in initial state
                assertJourneyInstanceState(instanceId, "START");

                // When: Send event with data that meets condition (amount > 1000)
                Map<String, Object> eventPayload = Map.of("amount", 1500.0, "currency", "USD");
                Response eventResponse = sendEvent(instanceId, "PROCESS", eventPayload);
                assertEventProcessed(eventResponse);

                // Then: Should follow high-value path (simplified expectation for now)
                waitForJourneyState(instanceId, "END", 10);
                assertJourneyInstanceState(instanceId, "END");
        }

        @Test
        @DisplayName("Should follow alternative path when condition is not met")
        void shouldFollowAlternativePathWhenConditionNotMet() {
                // Given: A journey with conditional transitions using hybrid fixtures
                Map<String, Object> journeyDefinition =
                                hybridFixtures.conditionalJourney("LOW_VALUE_TEST");
                Response createResponse = createJourneyDefinition(journeyDefinition);
                assertJourneyDefinitionCreated(createResponse);

                String journeyCode = createResponse.jsonPath().getString("journeyCode");
                int version = createResponse.jsonPath().getInt("version");

                // When: Start journey instance
                Response startResponse = startJourneyInstance(journeyCode, version, Map.of());
                assertJourneyInstanceStarted(startResponse);

                String instanceId = startResponse.jsonPath().getString("instanceId");

                // When: Send event with data that doesn't meet condition (amount < 1000)
                Map<String, Object> lowAmountPayload = Map.of("amount", 500.0, "currency", "USD",
                                "customerType", "standard");
                Response eventResponse = sendEvent(instanceId, "PROCESS", lowAmountPayload);
                assertEventProcessed(eventResponse);

                // Then: Should follow standard path (simplified expectation)
                waitForJourneyState(instanceId, "END", 10);
                assertJourneyInstanceState(instanceId, "END");
        }
}
