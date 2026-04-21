package com.luscadevs.journeyorchestrator.e2e.scenarios.client;

import com.luscadevs.journeyorchestrator.e2e.framework.base.AbstractE2ETest;
import com.luscadevs.journeyorchestrator.e2e.framework.client.JourneyApiClient;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Demonstration of the JourneyApiClient wrapper usage. Shows how the fluent API client simplifies
 * test code.
 */
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("API Client Demo E2E Tests")
public class ApiClientDemoE2ETest extends AbstractE2ETest {

        private JourneyApiClient apiClient;

        @BeforeEach
        void setUpApiClient() {
                this.apiClient = new JourneyApiClient(requestSpec);
        }

        @Test
        @DisplayName("Should demonstrate fluent API client usage")
        void shouldDemonstrateFluentApiClientUsage() {
                // Given: Using the fluent API client
                JourneyApiClient.JourneyDefinitionResponse journeyDef =
                                apiClient.createSimpleJourney().assertCreated();

                String journeyCode = journeyDef.getJourneyCode();
                int version = journeyDef.getVersion();

                // When: Starting a journey instance with context
                Map<String, Object> context = Map.of("customerId", "customer-123", "orderAmount",
                                1000.0, "productType", "premium");

                JourneyApiClient.JourneyInstanceResponse instance =
                                apiClient.journeyInstance().forJourney(journeyCode).version(version)
                                                .withContext(context).start().assertStarted();

                String instanceId = instance.getInstanceId();

                // Then: Send events using the fluent API
                apiClient.sendCompletionEvent(instanceId, "test-user").assertProcessed();

                // Verify final state
                apiClient.getJourneyInstance(instanceId).assertState("END");
        }

        @Test
        @DisplayName("Should demonstrate builder pattern for journey definitions")
        void shouldDemonstrateJourneyDefinitionBuilder() {
                // Given: Using the journey definition builder
                JourneyApiClient.JourneyDefinitionResponse journeyDef = apiClient
                                .journeyDefinition().withCode("CUSTOM_JOURNEY")
                                .withName("Custom Test Journey")
                                .withDescription("A journey created with the builder pattern")
                                .create().assertSuccess();

                // When: Start an instance
                JourneyApiClient.JourneyInstanceResponse instance = apiClient.journeyInstance()
                                .forJourney(journeyDef.getJourneyCode())
                                .version(journeyDef.getVersion())
                                .withContext("testKey", "testValue").start().assertStarted();

                // Then: Send custom event
                apiClient.event().toInstance(instance.getInstanceId()).type("COMPLETE")
                                .withPayload("completedBy", "api-client-demo").send()
                                .assertProcessed();
        }

        @Test
        @DisplayName("Should demonstrate event builder patterns")
        void shouldDemonstrateEventBuilderPatterns() {
                // Given: Create a journey and instance
                JourneyApiClient.JourneyDefinitionResponse journeyDef =
                                apiClient.createSimpleJourney().assertCreated();

                JourneyApiClient.JourneyInstanceResponse instance = apiClient
                                .startJourney(journeyDef.getJourneyCode(), journeyDef.getVersion())
                                .assertStarted();

                // When: Send completion event (simple journey only supports COMPLETE)
                apiClient.sendCompletionEvent(instance.getInstanceId(), "event-builder-test")
                                .assertProcessed();

                // Then: Verify journey completion
                apiClient.getJourneyInstance(instance.getInstanceId()).assertState("END");
        }

        @Test
        @DisplayName("Should demonstrate error handling with API client")
        void shouldDemonstrateErrorHandlingWithApiClient() {
                // Given: Try to start journey with invalid version
                JourneyApiClient.JourneyDefinitionResponse journeyDef =
                                apiClient.createSimpleJourney().assertCreated();

                // When: Start instance with non-existent version
                JourneyApiClient.JourneyInstanceResponse response = apiClient.journeyInstance()
                                .forJourney(journeyDef.getJourneyCode()).version(999) // Invalid
                                                                                      // version
                                .withContext("test", "value").start();

                // Then: Should handle error gracefully
                response.getRawResponse().then().statusCode(404);
        }

        @Test
        @DisplayName("Should demonstrate transition history retrieval")
        void shouldDemonstrateTransitionHistoryRetrieval() {
                // Given: Create and start a journey
                JourneyApiClient.JourneyDefinitionResponse journeyDef =
                                apiClient.createSimpleJourney().assertCreated();

                JourneyApiClient.JourneyInstanceResponse instance = apiClient
                                .startJourney(journeyDef.getJourneyCode(), journeyDef.getVersion())
                                .assertStarted();

                // When: Send events to create history
                apiClient.sendCompletionEvent(instance.getInstanceId(), "history-test-user")
                                .assertProcessed();

                // Then: Retrieve transition history
                apiClient.getTransitionHistory(instance.getInstanceId()).assertRetrieved();
        }
}
