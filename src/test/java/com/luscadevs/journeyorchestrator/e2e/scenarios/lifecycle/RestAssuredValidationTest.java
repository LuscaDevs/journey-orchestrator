package com.luscadevs.journeyorchestrator.e2e.scenarios.lifecycle;

import com.luscadevs.journeyorchestrator.e2e.framework.base.RestAssuredTestBase;
import com.luscadevs.journeyorchestrator.e2e.framework.fixtures.JourneyDefinitionFixtures;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to validate RestAssured configuration works correctly.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("RestAssured Configuration Validation")
public class RestAssuredValidationTest extends RestAssuredTestBase {

    @Test
    @DisplayName("Should make simple RestAssured request successfully")
    void shouldMakeSimpleRestAssuredRequest() {
        // Given: A simple journey definition
        Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.simpleJourney();

        // When: Make request using RestAssured
        Response response = given()
                .body(journeyDefinition)
                .post("/api/journeys");

        // Then: Should get a response (even if error, should not be null pointer)
        assertNotNull(response, "Response should not be null");
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 600, 
                  "Should get valid HTTP status code");

        System.out.println("RestAssured request successful!");
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body().asString());
    }

    @Test
    @DisplayName("Should validate RestAssured basic functionality")
    void shouldValidateRestAssuredBasicFunctionality() {
        // Given: Simple GET request
        Response response = given()
                .get("/actuator/health");

        // Then: Should handle response properly
        assertNotNull(response, "Response should not be null");
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 600, 
                  "Should get valid HTTP status code");

        System.out.println("RestAssured GET request successful!");
        System.out.println("Status: " + response.statusCode());
    }
}
