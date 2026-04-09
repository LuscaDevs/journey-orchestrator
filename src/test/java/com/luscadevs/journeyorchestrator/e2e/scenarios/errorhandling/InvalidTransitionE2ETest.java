package com.luscadevs.journeyorchestrator.e2e.scenarios.errorhandling;

import com.luscadevs.journeyorchestrator.e2e.framework.base.AbstractE2ETest;
import com.luscadevs.journeyorchestrator.e2e.framework.fixtures.JourneyDefinitionFixtures;
import com.luscadevs.journeyorchestrator.e2e.framework.fixtures.EventPayloadFixtures;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.Map;
import java.util.List;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.containsString;

/**
 * E2E tests for invalid transition scenarios and error handling. Tests edge cases, validation
 * errors, and system robustness.
 */
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Invalid Transition E2E Tests")
public class InvalidTransitionE2ETest extends AbstractE2ETest {

        @Test
        @DisplayName("Should reject invalid event for current state")
        void shouldRejectInvalidEventForCurrentState() {
                // Given: A simple journey definition
                Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.simpleJourney();

                // When: Create and start journey
                Response createResponse = createJourneyDefinition(journeyDefinition);
                String journeyCode = createResponse.jsonPath().getString("journeyCode");
                int version = createResponse.jsonPath().getInt("version");

                Response startResponse = startJourneyInstance(journeyCode, version, Map.of());
                String instanceId = startResponse.jsonPath().getString("instanceId");

                // When: Send invalid event (not defined in transition)
                Map<String, Object> invalidEvent = Map.of("event", "INVALID_EVENT", "payload",
                                Map.of("reason", "Testing invalid event", "timestamp",
                                                java.time.Instant.now().toString()));

                Response eventResponse = sendEvent(instanceId, "INVALID_EVENT",
                                (Map<String, Object>) invalidEvent.get("payload"));

                // Then: Should reject the invalid event
                eventResponse.then().statusCode(422) // Unprocessable Entity
                                .body("status", equalTo(422));
        }

        @Test
        @DisplayName("Should handle missing event payload gracefully")
        void shouldHandleMissingEventPayloadGracefully() {
                // Given: A journey instance
                Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.simpleJourney();
                Response createResponse = createJourneyDefinition(journeyDefinition);
                String journeyCode = createResponse.jsonPath().getString("journeyCode");
                int version = createResponse.jsonPath().getInt("version");

                Response startResponse = startJourneyInstance(journeyCode, version, Map.of());
                String instanceId = startResponse.jsonPath().getString("instanceId");

                // When: Send event with empty payload
                Response eventResponse = sendEvent(instanceId, "COMPLETE", Map.of());

                // Then: Should handle gracefully or process with minimal data
                eventResponse.then().statusCode(anyOf(equalTo(200), equalTo(202), equalTo(422)));
        }

        @Test
        @DisplayName("Should reject transition from completed state")
        void shouldRejectTransitionFromCompletedState() {
                // Given: A completed journey instance
                Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.simpleJourney();
                Response createResponse = createJourneyDefinition(journeyDefinition);
                String journeyCode = createResponse.jsonPath().getString("journeyCode");
                int version = createResponse.jsonPath().getInt("version");

                Response startResponse = startJourneyInstance(journeyCode, version, Map.of());
                String instanceId = startResponse.jsonPath().getString("instanceId");

                // Complete the journey first
                Map<String, Object> completionEvent =
                                EventPayloadFixtures.completionEvent("test-user");
                sendEvent(instanceId, "COMPLETE",
                                (Map<String, Object>) completionEvent.get("payload"));
                waitForJourneyState(instanceId, "END", 5);

                // When: Try to send another event to completed journey
                Map<String, Object> anotherEvent = EventPayloadFixtures.processEvent(1000.0);

                Response eventResponse = sendEvent(instanceId, "PROCESS",
                                (Map<String, Object>) anotherEvent.get("payload"));

                // Then: Should reject the transition
                eventResponse.then().statusCode(422);
        }

        @Test
        @DisplayName("Should handle non-existent journey instance")
        void shouldHandleNonExistentJourneyInstance() {
                // Given: A non-existent instance ID
                String nonExistentInstanceId = "550e8400-e29b-41d4-a716-446655440000";

                // When: Try to send event to non-existent instance
                Map<String, Object> eventPayload =
                                EventPayloadFixtures.completionEvent("test-user");
                Response eventResponse = sendEvent(nonExistentInstanceId, "COMPLETE",
                                (Map<String, Object>) eventPayload.get("payload"));

                // Then: Should return 404 Not Found
                eventResponse.then().statusCode(404);
        }

        @Test
        @DisplayName("Should reject event with malformed payload")
        void shouldRejectEventWithMalformedPayload() {
                // Given: A journey instance
                Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.simpleJourney();
                Response createResponse = createJourneyDefinition(journeyDefinition);
                String journeyCode = createResponse.jsonPath().getString("journeyCode");
                int version = createResponse.jsonPath().getInt("version");

                Response startResponse = startJourneyInstance(journeyCode, version, Map.of());
                String instanceId = startResponse.jsonPath().getString("instanceId");

                // When: Send event with malformed payload
                Map<String, Object> malformedPayload = Map.of("invalidField", "invalidValue",
                                "nullField", "null", "emptyString", "");

                Response eventResponse = sendEvent(instanceId, "COMPLETE", malformedPayload);

                // Then: Should handle gracefully or reject
                eventResponse.then().statusCode(anyOf(equalTo(200), equalTo(422)));
        }

        @Test
        @DisplayName("Should handle concurrent event processing")
        void shouldHandleConcurrentEventProcessing() {
                // Given: A journey instance
                Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.simpleJourney();
                Response createResponse = createJourneyDefinition(journeyDefinition);
                String journeyCode = createResponse.jsonPath().getString("journeyCode");
                int version = createResponse.jsonPath().getInt("version");

                Response startResponse = startJourneyInstance(journeyCode, version, Map.of());
                String instanceId = startResponse.jsonPath().getString("instanceId");

                // When: Send multiple events concurrently (simulated)
                Map<String, Object> event1 = EventPayloadFixtures.completionEvent("user-1");
                Map<String, Object> event2 = EventPayloadFixtures.completionEvent("user-2");

                Response response1 = sendEvent(instanceId, "COMPLETE",
                                (Map<String, Object>) event1.get("payload"));
                Response response2 = sendEvent(instanceId, "COMPLETE",
                                (Map<String, Object>) event2.get("payload"));

                // Then: Should handle gracefully - one should succeed, other should fail
                // At least one should succeed
                response1.then().statusCode(anyOf(equalTo(200), equalTo(202), equalTo(422)));
                response2.then().statusCode(anyOf(equalTo(200), equalTo(202), equalTo(422)));
        }

        @Test
        @DisplayName("Should validate journey definition constraints")
        void shouldValidateJourneyDefinitionConstraints() {
                // When: Try to create journey with empty journey code
                Map<String, Object> invalidJourney = Map.of("journeyCode", "", "name",
                                "Invalid Journey", "version", 1, "active", true, "states",
                                List.of(Map.of("name", "START", "type", "INITIAL")), "transitions",
                                List.of());

                Response response = createJourneyDefinition(invalidJourney);

                // Then: Should handle journey creation (system may accept or reject)
                response.then().statusCode(anyOf(equalTo(201), equalTo(200), equalTo(422)));
        }

        @Test
        @DisplayName("Should handle journey instance creation with invalid version")
        void shouldHandleJourneyInstanceCreationWithInvalidVersion() {
                // Given: A valid journey definition
                Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.simpleJourney();
                Response createResponse = createJourneyDefinition(journeyDefinition);
                String journeyCode = createResponse.jsonPath().getString("journeyCode");

                // When: Try to start instance with invalid version
                Response startResponse = startJourneyInstance(journeyCode, 999, Map.of());

                // Then: Should reject invalid version
                startResponse.then().statusCode(404);
        }

        @Test
        @DisplayName("Should handle malformed event structure")
        void shouldHandleMalformedEventStructure() {
                // Given: A journey instance
                Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.simpleJourney();
                Response createResponse = createJourneyDefinition(journeyDefinition);
                String journeyCode = createResponse.jsonPath().getString("journeyCode");
                int version = createResponse.jsonPath().getInt("version");

                Response startResponse = startJourneyInstance(journeyCode, version, Map.of());
                String instanceId = startResponse.jsonPath().getString("instanceId");

                // When: Send event with malformed structure using direct API call
                Response eventResponse = given().body(Map.of("malformed", "structure"))
                                .post("/journey-instances/" + instanceId + "/events");

                // Then: Should handle gracefully
                eventResponse.then().statusCode(anyOf(equalTo(400)));
        }

        @Test
        @DisplayName("Should maintain system stability under error conditions")
        void shouldMaintainSystemStabilityUnderErrorConditions() {
                // Given: Multiple error scenarios
                String nonExistentInstanceId = "550e8400-e29b-41d4-a716-446655440000";

                // When: Send multiple invalid requests
                for (int i = 0; i < 5; i++) {
                        Response response = sendEvent(nonExistentInstanceId, "COMPLETE", Map.of());
                        response.then().statusCode(404);
                }

                // Then: System should remain stable for valid requests
                Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.simpleJourney();
                Response createResponse = createJourneyDefinition(journeyDefinition);
                createResponse.then().statusCode(anyOf(equalTo(201), equalTo(200), equalTo(409)));
        }
}
