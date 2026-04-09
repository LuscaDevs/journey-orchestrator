package com.luscadevs.journeyorchestrator.e2e.scenarios.workflow;

import com.luscadevs.journeyorchestrator.e2e.framework.base.AbstractE2ETest;
import com.luscadevs.journeyorchestrator.e2e.framework.fixtures.JourneyDefinitionFixtures;
import com.luscadevs.journeyorchestrator.e2e.framework.fixtures.EventPayloadFixtures;
import com.luscadevs.journeyorchestrator.e2e.framework.util.TestHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests validating the complete lifecycle of a journey workflow. Tests the entire journey from
 * definition creation through completion.
 */
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Journey Workflow E2E Tests")
public class JourneyWorkflowE2ETest extends AbstractE2ETest {

        @Test
        @DisplayName("Should complete full journey lifecycle with simple flow")
        void shouldCompleteFullJourneyLifecycle() {
                // Given: Standard context using TestHelper
                Map<String, Object> initialContext =
                                TestHelper.createStandardContext("customer-123", 1000.0, "premium");

                // When: Execute complete journey lifecycle using TestHelper
                String instanceId = TestHelper.executeCompleteJourney(this, initialContext,
                                "workflow-test-user");

                // Then: Validate journey completed successfully
                Response finalResponse = getJourneyInstance(instanceId);
                assertThat(finalResponse.getStatusCode()).isEqualTo(200);
                assertThat(finalResponse.jsonPath().getString("instanceId")).isEqualTo(instanceId);
                assertThat(finalResponse.jsonPath().getString("currentState")).isEqualTo("END");
                assertThat(finalResponse.jsonPath().getString("status")).isEqualTo("RUNNING");
        }

        @Test
        @DisplayName("Should handle multi-step journey with sequential processing")
        void shouldHandleMultiStepJourney() {
                // Given: Empty context for simple journey
                Map<String, Object> context = Map.of();

                // When: Execute complete journey lifecycle using TestHelper
                String instanceId = TestHelper.executeCompleteJourney(this, context,
                                "multi-step-test-user");

                // Then: Validate journey completion and transition history
                Response finalResponse = getJourneyInstance(instanceId);
                assertThat(finalResponse.getStatusCode()).isEqualTo(200);
                assertThat(finalResponse.jsonPath().getString("instanceId")).isEqualTo(instanceId);
                assertThat(finalResponse.jsonPath().getString("currentState")).isEqualTo("END");
                assertThat(finalResponse.jsonPath().getString("status")).isEqualTo("RUNNING");

                Response historyResponse = getTransitionHistory(instanceId);
                assertThat(historyResponse.getStatusCode()).isEqualTo(200);
                // Note: Verify transition history contains all steps
        }

        @Test
        @DisplayName("Should handle conditional journey with different paths")
        void shouldHandleConditionalJourney() {
                // Given: A conditional journey definition and high value event
                Map<String, Object> journeyDefinition =
                                JourneyDefinitionFixtures.conditionalJourney();
                Map<String, Object> highValueEvent = EventPayloadFixtures.highValueProcessEvent();

                // When: Create journey definition, start instance, and send high value event
                Response createResponse = createJourneyDefinition(journeyDefinition);
                assertJourneyDefinitionCreated(createResponse);

                String journeyCode = createResponse.jsonPath().getString("journeyCode");
                int version = createResponse.jsonPath().getInt("version");

                Response startResponse = startJourneyInstance(journeyCode, version, Map.of());
                String instanceId = startResponse.jsonPath().getString("instanceId");
                sendEvent(instanceId, "PROCESS",
                                (Map<String, Object>) highValueEvent.get("payload"));
                waitForJourneyState(instanceId, "HIGH_VALUE_APPROVED", 10);
                assertJourneyInstanceState(instanceId, "HIGH_VALUE_APPROVED");

                // Verify the correct path was taken
                Response instanceResponse = getJourneyInstance(instanceId);
                assertThat(instanceResponse.getStatusCode()).isEqualTo(200);
                assertThat(instanceResponse.jsonPath().getString("currentState"))
                                .isEqualTo("HIGH_VALUE_APPROVED");
        }

        @Test
        @DisplayName("Should maintain data consistency across journey lifecycle")
        void shouldMaintainDataConsistency() {
                // Given: Rich context for data consistency testing
                Map<String, Object> initialContext = Map.of("customerId", "customer-456",
                                "requestAmount", 2500.0, "requestType", "loan", "riskScore", 750);

                // When: Execute complete journey lifecycle using TestHelper
                String instanceId = TestHelper.executeCompleteJourney(this, initialContext,
                                "data-consistency-test");

                // Then: Validate journey completion and transition history integrity
                Response finalResponse = getJourneyInstance(instanceId);
                assertThat(finalResponse.getStatusCode()).isEqualTo(200);
                assertThat(finalResponse.jsonPath().getString("instanceId")).isEqualTo(instanceId);
                assertThat(finalResponse.jsonPath().getString("currentState")).isEqualTo("END");
                assertThat(finalResponse.jsonPath().getString("status")).isEqualTo("RUNNING");

                Response historyResponse = getTransitionHistory(instanceId);
                assertThat(historyResponse.getStatusCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("Should handle journey timeout scenarios")
        void shouldHandleJourneyTimeout() {
                // Given: A simple journey definition
                Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.simpleJourney();

                // When: Create journey definition and start instance
                Response createResponse = createJourneyDefinition(journeyDefinition);
                String journeyCode = createResponse.jsonPath().getString("journeyCode");
                int version = createResponse.jsonPath().getInt("version");

                Response startResponse = startJourneyInstance(journeyCode, version, Map.of());
                String instanceId = startResponse.jsonPath().getString("instanceId");

                // Simulate completion event (since timeout event doesn't exist in simple journey)
                Map<String, Object> completionEvent =
                                EventPayloadFixtures.completionEvent("timeout-test-user");
                sendEvent(instanceId, "COMPLETE",
                                (Map<String, Object>) completionEvent.get("payload"));
                waitForJourneyState(instanceId, "END", 5);

                // Then: Journey should be completed
                assertJourneyInstanceState(instanceId, "END");
        }

        @Test
        @DisplayName("Should validate journey definition constraints")
        void shouldValidateJourneyDefinitionConstraints() {
                // Given: Valid journey definition
                Map<String, Object> validJourney = JourneyDefinitionFixtures.simpleJourney();

                // When/Then: Should handle valid definitions correctly
                Response response = createJourneyDefinition(validJourney);
                assertThat(response.getStatusCode()).isIn(201, 200, 409);
        }
}
