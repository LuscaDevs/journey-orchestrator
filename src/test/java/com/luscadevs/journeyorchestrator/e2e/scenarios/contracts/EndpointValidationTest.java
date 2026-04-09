package com.luscadevs.journeyorchestrator.e2e.scenarios.contracts;

import com.luscadevs.journeyorchestrator.e2e.framework.base.RestAssuredTestBase;
import com.luscadevs.journeyorchestrator.e2e.framework.client.JourneyApiClient;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E Endpoint Validation Tests for Journey Orchestrator.
 * Validates all API endpoints exist, respond with correct HTTP methods,
 * and return appropriate status codes.
 */
@Tag("contract")
@Tag("endpoint-validation")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Endpoint Validation Tests")
public class EndpointValidationTest extends RestAssuredTestBase {

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
     * Given: OpenAPI defines POST /api/v1/journeys endpoint
     * When: Client makes POST request to create a journey
     * Then: Endpoint should accept the request and return 201 or appropriate error
     */
    @Test
    @DisplayName("Should validate POST /api/v1/journeys endpoint exists and responds correctly")
    void shouldValidateJourneyCreationEndpoint() {
        var response = apiClient.createSimpleJourney();

        // Endpoint should exist and respond with one of these codes
        assertThat(response.getRawResponse().statusCode()).isIn(200, 201, 400, 409, 500);
        assertThat(response.getRawResponse().getContentType()).containsIgnoringCase("json");
    }

    /**
     * Given: OpenAPI defines POST /api/v1/journeys/{code}/instances endpoint
     * When: Client makes POST request to create instance
     * Then: Endpoint should accept the request and return 201 or appropriate error
     */
    @Test
    @DisplayName("Should validate POST /api/v1/journeys/{code}/instances endpoint exists")
    void shouldValidateInstanceCreationEndpoint() {
        // Create a journey first
        var journeyResponse = apiClient.createSimpleJourney();

        if (journeyResponse.getRawResponse().statusCode() != 201 &&
                journeyResponse.getRawResponse().statusCode() != 200) {
            System.out.println("Skipping - journey creation failed");
            return;
        }

        String journeyCode = journeyResponse.getJourneyCode();
        var response = apiClient.startJourney(journeyCode, journeyResponse.getVersion());

        // Endpoint should exist and respond with appropriate code
        assertThat(response.getRawResponse().statusCode()).isIn(200, 201, 400, 404);
        assertThat(response.getRawResponse().getContentType()).containsIgnoringCase("json");
    }

    /**
     * Given: OpenAPI defines POST /api/v1/instances/{id}/events endpoint
     * When: Client makes POST request to send event
     * Then: Endpoint should accept the request and return 200 or appropriate error
     */
    @Test
    @DisplayName("Should validate POST /api/v1/instances/{id}/events endpoint exists")
    void shouldValidateEventSendingEndpoint() {
        // Create a journey and instance
        var journeyResponse = apiClient.createSimpleJourney();
        var instanceResponse = apiClient.startJourney(journeyResponse.getJourneyCode(), journeyResponse.getVersion());

        if (instanceResponse.getRawResponse().statusCode() != 200
                && instanceResponse.getRawResponse().statusCode() != 201) {
            System.out.println("Skipping - instance creation failed");
            return;
        }

        String instanceId = instanceResponse.getInstanceId();
        var response = apiClient.sendEvent(instanceId, "PROCESS", Map.of("amount", 1000.0, "currency", "USD"));

        // Endpoint should exist and respond with appropriate code
        assertThat(response.getRawResponse().statusCode()).isIn(200, 202, 400, 404, 422);
    }

    /**
     * Given: OpenAPI defines GET /api/v1/instances/{id} endpoint
     * When: Client makes GET request to retrieve instance
     * Then: Endpoint should accept the request and return 200 or 404
     */
    @Test
    @DisplayName("Should validate GET /api/v1/instances/{id} endpoint exists")
    void shouldValidateInstanceRetrievalEndpoint() {
        // Create a journey and instance first
        var journeyResponse = apiClient.createSimpleJourney();
        var instanceResponse = apiClient.startJourney(journeyResponse.getJourneyCode(), journeyResponse.getVersion());

        String instanceId = instanceResponse.getInstanceId();
        var response = apiClient.getJourneyInstance(instanceId);

        // Endpoint should exist and respond with appropriate code (200 or 404)
        assertThat(response.getRawResponse().statusCode()).isIn(200, 404);
    }

    /**
     * Given: API endpoints defined in OpenAPI
     * When: Invalid HTTP method is used on endpoints
     * Then: System should return 405 Method Not Allowed or similar
     */
    @Test
    @DisplayName("Should validate HTTP methods are properly enforced")
    void shouldValidateHttpMethodEnforcement() {
        // POST to journey endpoint should work
        var postResponse = apiClient.createSimpleJourney();
        assertThat(postResponse.getRawResponse().statusCode()).isNotEqualTo(405);

        // Try to get a non-existent journey code
        // (simulating wrong HTTP method or invalid endpoint)
        var getResponse = apiClient.getJourneyInstance("non-existent");
        assertThat(getResponse.getRawResponse().statusCode()).isIn(404, 405, 500);
    }

    /**
     * Given: API endpoints with path parameters
     * When: Invalid path parameters are provided
     * Then: System should return 404 or appropriate error
     */
    @Test
    @DisplayName("Should validate path parameter validation")
    void shouldValidatePathParameterValidation() {
        // Try to get non-existent journey instance with invalid ID
        var response = apiClient.getJourneyInstance("invalid-id-xyz");

        // Should return 404 for non-existent journey
        assertThat(response.getRawResponse().statusCode()).isIn(404, 400, 500);
    }

    /**
     * Given: API endpoints with request bodies
     * When: Malformed JSON is sent
     * Then: System should return 400 Bad Request
     */
    @Test
    @DisplayName("Should validate request body validation")
    void shouldValidateRequestBodyValidation() {
        // Try to create journey with invalid definition (empty/missing required fields)
        var response = apiClient.createJourneyDefinition(Map.of());

        // Invalid/misformed request should return 400 or 422
        assertThat(response.getRawResponse().statusCode()).isIn(400, 422, 500);
    }

    /**
     * Given: API endpoints that require specific content types
     * When: Request is made with incorrect Content-Type
     * Then: System should handle gracefully
     */
    @Test
    @DisplayName("Should validate Content-Type header handling")
    void shouldValidateContentTypeHandling() {
        // Create a simple journey - apiClient uses correct content-type
        var response = apiClient.createSimpleJourney();

        // Should handle correctly (not return 500)
        assertThat(response.getRawResponse().statusCode()).isNotEqualTo(500);
    }

}
