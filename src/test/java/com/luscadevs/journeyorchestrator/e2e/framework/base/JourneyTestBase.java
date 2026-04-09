package com.luscadevs.journeyorchestrator.e2e.framework.base;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.Map;

/**
 * Specialized base class for journey-specific E2E tests. Provides journey-specific test utilities
 * and API interaction methods.
 */
public abstract class JourneyTestBase extends E2ETestBase {

    /**
     * Creates a journey definition from template via API
     */
    protected Response createJourneyDefinition(Map<String, Object> journeyDefinition) {
        return RestAssured.given().contentType("application/json").accept("application/json")
                .body(journeyDefinition).post("/api/journeys");
    }

    /**
     * Starts a journey instance via API
     */
    protected Response startJourneyInstance(String journeyCode, int version,
            Map<String, Object> context) {
        Map<String, Object> request =
                Map.of("journeyCode", journeyCode, "version", version, "context", context);

        return RestAssured.given().contentType("application/json").accept("application/json")
                .body(request).post("/api/journey-instances");
    }

    /**
     * Sends an event to a journey instance via API
     */
    protected Response sendEvent(String instanceId, String eventType, Map<String, Object> payload) {
        Map<String, Object> request = Map.of("event", eventType, "payload", payload);

        return RestAssured.given().contentType("application/json").accept("application/json")
                .body(request).post("/api/journey-instances/" + instanceId + "/events");
    }

    /**
     * Gets journey instance details via API
     */
    protected Response getJourneyInstance(String instanceId) {
        return RestAssured.given().accept("application/json")
                .get("/api/journey-instances/" + instanceId);
    }

    /**
     * Gets transition history for journey instance via API
     */
    protected Response getTransitionHistory(String instanceId) {
        return RestAssured.given().accept("application/json")
                .get("/api/journey-instances/" + instanceId + "/history");
    }

    /**
     * Asserts that journey instance is in expected state
     */
    protected void assertJourneyInstanceState(String instanceId, String expectedState) {
        Response response = getJourneyInstance(instanceId);
        response.then().statusCode(200).body("currentState",
                org.hamcrest.Matchers.equalTo(expectedState));
    }

    /**
     * Asserts that journey instance has expected status
     */
    protected void assertJourneyInstanceStatus(String instanceId, String expectedStatus) {
        Response response = getJourneyInstance(instanceId);
        response.then().statusCode(200).body("status",
                org.hamcrest.Matchers.equalTo(expectedStatus));
    }

    /**
     * Asserts that journey definition was created successfully
     */
    protected void assertJourneyDefinitionCreated(Response response) {
        response.then().statusCode(201).body("journeyCode", org.hamcrest.Matchers.notNullValue())
                .body("version", org.hamcrest.Matchers.notNullValue());
    }

    /**
     * Asserts that journey instance was started successfully
     */
    protected void assertJourneyInstanceStarted(Response response) {
        response.then().statusCode(201).body("instanceId", org.hamcrest.Matchers.notNullValue())
                .body("currentState", org.hamcrest.Matchers.notNullValue())
                .body("status", org.hamcrest.Matchers.equalTo("RUNNING"));
    }

    /**
     * Asserts that event was processed successfully
     */
    protected void assertEventProcessed(Response response) {
        response.then().statusCode(200).body("instanceId", org.hamcrest.Matchers.notNullValue())
                .body("currentState", org.hamcrest.Matchers.notNullValue());
    }

    /**
     * Creates a simple journey definition for testing
     */
    protected Map<String, Object> createSimpleJourneyDefinition() {
        return Map.of("journeyCode", "SIMPLE_JOURNEY", "name", "Simple Test Journey", "version", 1,
                "states", Map.of("name", "START", "type", "INITIAL"), "transitions",
                Map.of("source", "START", "event", "COMPLETE", "target", "END"));
    }

    /**
     * Creates a conditional journey definition for testing
     */
    protected Map<String, Object> createConditionalJourneyDefinition() {
        return Map.of("journeyCode", "CONDITIONAL_JOURNEY", "name", "Conditional Test Journey",
                "version", 1, "states", Map.of("name", "START", "type", "INITIAL"), "transitions",
                Map.of("source", "START", "event", "PROCESS", "target", "APPROVED", "condition",
                        "#eventData.amount > 1000"));
    }

    /**
     * Creates a test event payload
     */
    protected Map<String, Object> createEventPayload(String eventType, Map<String, Object> data) {
        return Map.of("event", eventType, "payload", data);
    }

    /**
     * Waits for journey instance to reach expected state
     */
    protected void waitForJourneyState(String instanceId, String expectedState, int maxAttempts) {
        for (int i = 0; i < maxAttempts; i++) {
            try {
                Response response = getJourneyInstance(instanceId);
                String currentState = response.jsonPath().getString("currentState");

                if (expectedState.equals(currentState)) {
                    return;
                }

                Thread.sleep(1000); // Wait 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Continue trying
            }
        }

        throw new AssertionError("Journey instance " + instanceId + " did not reach state "
                + expectedState + " after " + maxAttempts + " attempts");
    }
}
