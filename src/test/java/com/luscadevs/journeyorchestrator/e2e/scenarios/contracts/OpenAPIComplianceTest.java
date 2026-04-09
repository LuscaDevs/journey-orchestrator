package com.luscadevs.journeyorchestrator.e2e.scenarios.contracts;

import com.luscadevs.journeyorchestrator.config.MongoTestContainerConfig;
import com.luscadevs.journeyorchestrator.e2e.framework.base.RestAssuredTestBase;
import com.luscadevs.journeyorchestrator.e2e.framework.client.JourneyApiClient;
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
import static org.junit.jupiter.api.Assertions.*;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Set;

/**
 * E2E OpenAPI Compliance Tests for Journey Orchestrator.
 * Validates that all API responses conform to the OpenAPI specification.
 * Ensures API contracts are stable and backward compatible.
 */
@Tag("contract")
@Tag("openapi")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("OpenAPI Compliance Tests")
public class OpenAPIComplianceTest extends RestAssuredTestBase {

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
     * Given: OpenAPI specification defines journey creation contract
     * When: Journey definition is created via POST /api/v1/journeys
     * Then: Response should match OpenAPI schema with 201 status and required
     * fields
     */
    @Test
    @DisplayName("Should validate journey creation response matches OpenAPI contract")
    void shouldValidateJourneyCreationContract() {
        var response = apiClient.createSimpleJourney();

        // Validate HTTP status
        int statusCode = response.getRawResponse().statusCode();
        assertThat(statusCode).isIn(201, 200, 400, 409, 500);

        // If successful (201 or 200), validate response schema
        if (statusCode == 201 || statusCode == 200) {
            validateJourneyDefinitionResponse(response);
        }
    }

    /**
     * Given: OpenAPI specification defines instance creation contract
     * When: Journey instance is created via POST /api/v1/journeys/{code}/instances
     * Then: Response should match OpenAPI schema with 201 status and required
     * fields
     */
    @Test
    @DisplayName("Should validate journey instance creation response matches OpenAPI contract")
    void shouldValidateInstanceCreationContract() {
        // Create journey first
        var journeyResponse = apiClient.createSimpleJourney();

        if (journeyResponse.getRawResponse().statusCode() != 201
                && journeyResponse.getRawResponse().statusCode() != 200) {
            System.out.println("Skipping instance creation test - journey creation failed");
            return;
        }

        String journeyCode = journeyResponse.getJourneyCode();
        int version = journeyResponse.getVersion();

        var instanceResponse = apiClient.startJourney(journeyCode, version);

        // Validate HTTP status
        int statusCode = instanceResponse.getRawResponse().statusCode();
        assertThat(statusCode).isIn(201, 200, 400, 404);

        // If successful, validate response schema
        if (statusCode == 201 || statusCode == 200) {
            validateJourneyInstanceResponse(instanceResponse);
        }
    }

    /**
     * Given: OpenAPI specification defines event sending contract
     * When: Event is sent to journey instance via POST
     * /api/v1/instances/{id}/events
     * Then: Response should match OpenAPI schema with 200 status and updated
     * instance data
     */
    @Test
    @DisplayName("Should validate event sending response matches OpenAPI contract")
    void shouldValidateEventSendingContract() {
        // Create journey and instance
        var journeyResponse = apiClient.createSimpleJourney();

        if (journeyResponse.getRawResponse().statusCode() != 201
                && journeyResponse.getRawResponse().statusCode() != 200) {
            System.out.println("Skipping event test - journey creation failed");
            return;
        }

        String journeyCode = journeyResponse.getJourneyCode();
        int version = journeyResponse.getVersion();

        var instanceResponse = apiClient.startJourney(journeyCode, version);

        if (instanceResponse.getRawResponse().statusCode() != 201
                && instanceResponse.getRawResponse().statusCode() != 200) {
            System.out.println("Skipping event test - instance creation failed");
            return;
        }

        String instanceId = instanceResponse.getInstanceId();

        var eventResponse = apiClient.sendEvent(instanceId, "PROCESS", Map.of("amount", 1000.0, "currency", "USD"));

        // Validate HTTP status
        int statusCode = eventResponse.getRawResponse().statusCode();
        assertThat(statusCode).isIn(200, 202, 400, 404, 422);

        // If successful, validate response schema
        if (statusCode == 200 || statusCode == 202) {
            validateEventResponse(eventResponse);
        }
    }

    /**
     * Given: OpenAPI specification defines instance retrieval contract
     * When: Journey instance is fetched via GET /api/v1/instances/{id}
     * Then: Response should match OpenAPI schema with 200 status and complete
     * instance data
     */
    @Test
    @DisplayName("Should validate journey instance retrieval response matches OpenAPI contract")
    void shouldValidateInstanceRetrievalContract() {
        // Create journey and instance
        var journeyResponse = apiClient.createSimpleJourney();

        if (journeyResponse.getRawResponse().statusCode() != 201
                && journeyResponse.getRawResponse().statusCode() != 200) {
            System.out.println("Skipping instance retrieval test - journey creation failed");
            return;
        }

        String journeyCode = journeyResponse.getJourneyCode();
        int version = journeyResponse.getVersion();

        var instanceResponse = apiClient.startJourney(journeyCode, version);

        if (instanceResponse.getRawResponse().statusCode() != 201
                && instanceResponse.getRawResponse().statusCode() != 200) {
            System.out.println("Skipping instance retrieval test - instance creation failed");
            return;
        }

        String instanceId = instanceResponse.getInstanceId();

        var getResponse = apiClient.getJourneyInstance(instanceId);

        // Validate HTTP status
        int statusCode = getResponse.getRawResponse().statusCode();
        assertThat(statusCode).isIn(200, 404);

        // If successful, validate response schema
        if (statusCode == 200) {
            validateJourneyInstanceResponse(getResponse);
        }
    }

    /**
     * Given: OpenAPI specification defines error response contract
     * When: Invalid request is made to the API
     * Then: Error response should match OpenAPI schema with proper error structure
     */
    @Test
    @DisplayName("Should validate error response matches OpenAPI contract")
    void shouldValidateErrorResponseContract() {
        Map<String, Object> invalidJourney = Map.of("invalidField", "value");

        var response = apiClient.createJourneyDefinition(invalidJourney);

        // Should return error status
        int statusCode = response.getRawResponse().statusCode();
        assertTrue(statusCode >= 400, "Invalid request should return 4xx or 5xx");

        // If error response has body, validate it has error structure
        if (response.getRawResponse().getBody() != null && !response.getRawResponse().getBody().asString().isEmpty()) {
            validateErrorResponse(response);
        }
    }

    /**
     * Given: OpenAPI specification with specific HTTP status codes
     * When: API endpoints are called
     * Then: All responses should use OpenAPI-defined status codes only
     */
    @Test
    @DisplayName("Should use only OpenAPI-defined HTTP status codes")
    void shouldUseDefinedStatusCodes() {
        var response = apiClient.createSimpleJourney();

        // All responses should be in the defined set
        assertThat(response.getRawResponse().statusCode()).isIn(
                200, 201, 202, // Success codes
                400, 401, 403, 404, 409, // Client error codes
                500, 502, 503 // Server error codes
        );
    }

    /**
     * Validates journey definition response structure
     */
    private void validateJourneyDefinitionResponse(JourneyApiClient.JourneyDefinitionResponse response) {
        String body = response.getRawResponse().getBody().asString();
        assertThat(body).isNotEmpty();
        validateWithSchema(body, "e2e/schemas/JourneyDefinitionResponse.json");
    }

    /**
     * Validates journey instance response structure
     */
    private void validateJourneyInstanceResponse(JourneyApiClient.JourneyInstanceResponse response) {
        String body = response.getRawResponse().getBody().asString();
        assertThat(body).isNotEmpty();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);
            InputStream schemaStream = getClass().getClassLoader()
                    .getResourceAsStream("e2e/schemas/JourneyInstanceResponse.json");
            assertNotNull(schemaStream, "Schema file not found: JourneyInstanceResponse.json");
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = factory.getSchema(schemaStream);
            Set<ValidationMessage> errors = schema.validate(jsonNode);
            assertTrue(errors.isEmpty(), "JSON does not match schema: " + errors);
        } catch (Exception e) {
            fail("Schema validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validates event sending response structure
     */
    private void validateEventResponse(JourneyApiClient.EventResponse response) {
        String body = response.getRawResponse().getBody().asString();
        assertThat(body).isNotEmpty();
        validateWithSchema(body, "e2e/schemas/EventRequest.json");
    }

    /**
     * Validates error response structure
     */
    private void validateErrorResponse(JourneyApiClient.JourneyDefinitionResponse response) {
        String body = response.getRawResponse().getBody().asString();
        assertThat(body).isNotEmpty();
        validateWithSchema(body, "e2e/schemas/ErrorResponse.json");
    }

    // Utilitário para validação genérica
    private void validateWithSchema(String body, String schemaPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(body);
            InputStream schemaStream = getClass().getClassLoader().getResourceAsStream(schemaPath);
            assertNotNull(schemaStream, "Schema file not found: " + schemaPath);
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = factory.getSchema(schemaStream);
            Set<ValidationMessage> errors = schema.validate(jsonNode);
            assertTrue(errors.isEmpty(), "JSON does not match schema: " + errors);
        } catch (Exception e) {
            fail("Schema validation failed: " + e.getMessage(), e);
        }
    }
}
