package com.luscadevs.journeyorchestrator.e2e.framework.util;

import com.luscadevs.journeyorchestrator.e2e.framework.base.AbstractE2ETest;
import com.luscadevs.journeyorchestrator.e2e.framework.fixtures.JourneyDefinitionFixtures;
import com.luscadevs.journeyorchestrator.e2e.framework.fixtures.EventPayloadFixtures;
import io.restassured.response.Response;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Utility class for common test operations to reduce duplication across E2E tests. Provides
 * reusable methods for journey creation, execution, and validation.
 */
public class TestHelper {

    /**
     * Creates and starts a journey instance with the given context. Handles cases where the journey
     * definition already exists.
     */
    public static Response createAndStartJourney(AbstractE2ETest testBase,
            Map<String, Object> context) {
        Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.simpleJourney();
        Response createResponse = invokeCreateJourneyDefinition(testBase, journeyDefinition);

        // Handle case where journey already exists
        String journeyCode;
        int version;

        if (createResponse.getStatusCode() == 201) {
            journeyCode = createResponse.jsonPath().getString("journeyCode");
            version = createResponse.jsonPath().getInt("version");
        } else {
            // Journey already exists, use default values
            journeyCode = "SIMPLE_JOURNEY";
            version = 1;
        }

        return invokeStartJourneyInstance(testBase, journeyCode, version, context);
    }

    /**
     * Uses reflection to access protected createJourneyDefinition method
     */
    private static Response invokeCreateJourneyDefinition(AbstractE2ETest testBase,
            Map<String, Object> journeyDefinition) {
        try {
            var method =
                    AbstractE2ETest.class.getDeclaredMethod("createJourneyDefinition", Map.class);
            method.setAccessible(true);
            return (Response) method.invoke(testBase, journeyDefinition);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke createJourneyDefinition", e);
        }
    }

    /**
     * Uses reflection to access protected startJourneyInstance method
     */
    private static Response invokeStartJourneyInstance(AbstractE2ETest testBase, String journeyCode,
            int version, Map<String, Object> context) {
        try {
            var method = AbstractE2ETest.class.getDeclaredMethod("startJourneyInstance",
                    String.class, int.class, Map.class);
            method.setAccessible(true);
            return (Response) method.invoke(testBase, journeyCode, version, context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke startJourneyInstance", e);
        }
    }

    /**
     * Uses reflection to access protected sendEvent method
     */
    private static Response invokeSendEvent(AbstractE2ETest testBase, String instanceId,
            String event, Map<String, Object> payload) {
        try {
            var method = AbstractE2ETest.class.getDeclaredMethod("sendEvent", String.class,
                    String.class, Map.class);
            method.setAccessible(true);
            return (Response) method.invoke(testBase, instanceId, event, payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke sendEvent", e);
        }
    }

    /**
     * Uses reflection to access protected getJourneyInstance method
     */
    private static Response invokeGetJourneyInstance(AbstractE2ETest testBase, String instanceId) {
        try {
            var method =
                    AbstractE2ETest.class.getDeclaredMethod("getJourneyInstance", String.class);
            method.setAccessible(true);
            return (Response) method.invoke(testBase, instanceId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke getJourneyInstance", e);
        }
    }

    /**
     * Uses reflection to access protected waitForJourneyState method
     */
    private static void invokeWaitForJourneyState(AbstractE2ETest testBase, String instanceId,
            String expectedState, int timeoutSeconds) {
        try {
            var method = AbstractE2ETest.class.getDeclaredMethod("waitForJourneyState",
                    String.class, String.class, int.class);
            method.setAccessible(true);
            method.invoke(testBase, instanceId, expectedState, timeoutSeconds);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke waitForJourneyState", e);
        }
    }

    /**
     * Completes a journey by sending a completion event.
     */
    public static Response completeJourney(AbstractE2ETest testBase, String instanceId,
            String completedBy) {
        Map<String, Object> completionEvent = EventPayloadFixtures.completionEvent(completedBy);
        return invokeSendEvent(testBase, instanceId, "COMPLETE",
                (Map<String, Object>) completionEvent.get("payload"));
    }

    /**
     * Asserts that a journey was completed successfully.
     */
    public static void assertJourneyCompleted(AbstractE2ETest testBase, Response createResponse,
            Response startResponse, Response completionResponse, String instanceId) {
        assertThat(createResponse.getStatusCode()).isEqualTo(201);
        assertThat(createResponse.jsonPath().getString("journeyCode")).isEqualTo("SIMPLE_JOURNEY");

        assertThat(startResponse.getStatusCode()).isEqualTo(200);
        assertThat(startResponse.jsonPath().getString("instanceId")).isNotNull();
        assertThat(startResponse.jsonPath().getString("currentState")).isEqualTo("START");

        assertThat(completionResponse.getStatusCode()).isIn(200, 202);

        Response finalResponse = invokeGetJourneyInstance(testBase, instanceId);
        assertThat(finalResponse.getStatusCode()).isEqualTo(200);
        assertThat(finalResponse.jsonPath().getString("currentState")).isEqualTo("END");
    }

    /**
     * Creates a standard test context with customer and order information.
     */
    public static Map<String, Object> createStandardContext(String customerId, double amount,
            String productType) {
        return Map.of("customerId", customerId, "orderAmount", amount, "productType", productType);
    }

    /**
     * Creates a rich test context with additional fields.
     */
    public static Map<String, Object> createRichContext(String customerId, double amount,
            String productType, String customerTier, String region, String source) {
        return Map.of("customerId", customerId, "orderAmount", amount, "productType", productType,
                "customerTier", customerTier, "region", region, "requestSource", source);
    }

    /**
     * Executes a complete journey lifecycle: create, start, and complete.
     */
    public static String executeCompleteJourney(AbstractE2ETest testBase,
            Map<String, Object> context, String completedBy) {
        Response startResponse = createAndStartJourney(testBase, context);
        String instanceId = startResponse.jsonPath().getString("instanceId");

        Response completionResponse = completeJourney(testBase, instanceId, completedBy);
        invokeWaitForJourneyState(testBase, instanceId, "END", 10);

        return instanceId;
    }

    /**
     * Sends a process event to the journey instance.
     */
    public static Response sendProcessEvent(AbstractE2ETest testBase, String instanceId,
            double amount) {
        Map<String, Object> processEvent = EventPayloadFixtures.processEvent(amount);
        return invokeSendEvent(testBase, instanceId, "PROCESS",
                (Map<String, Object>) processEvent.get("payload"));
    }

    /**
     * Sends a high value process event to the journey instance.
     */
    public static Response sendHighValueProcessEvent(AbstractE2ETest testBase, String instanceId) {
        Map<String, Object> highValueEvent = EventPayloadFixtures.highValueProcessEvent();
        return invokeSendEvent(testBase, instanceId, "PROCESS",
                (Map<String, Object>) highValueEvent.get("payload"));
    }
}
