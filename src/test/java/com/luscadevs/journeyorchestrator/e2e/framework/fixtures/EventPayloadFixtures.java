package com.luscadevs.journeyorchestrator.e2e.framework.fixtures;

import java.util.Map;
import java.util.HashMap;

/**
 * Factory class for creating event payload test fixtures. Provides reusable event payloads for E2E
 * testing scenarios.
 */
public class EventPayloadFixtures {

    /**
     * Creates a basic completion event payload
     */
    public static Map<String, Object> completionEvent() {
        return Map.of("event", "COMPLETE", "payload", Map.of("completedBy", "test-user",
                "timestamp", java.time.Instant.now().toString(), "reason", "test completion"));
    }

    /**
     * Creates a completion event payload with custom user
     */
    public static Map<String, Object> completionEvent(String completedBy) {
        return Map.of("event", "COMPLETE", "payload",
                Map.of("completedBy", completedBy, "timestamp", java.time.Instant.now().toString(),
                        "reason", "test completion by " + completedBy));
    }

    /**
     * Creates a process event payload with amount for conditional testing
     */
    public static Map<String, Object> processEvent(double amount) {
        return Map.of("event", "PROCESS", "payload",
                Map.of("amount", amount, "currency", "USD", "customerType",
                        amount > 1000 ? "premium" : "standard", "timestamp",
                        java.time.Instant.now().toString()));
    }

    /**
     * Creates a high-value process event payload
     */
    public static Map<String, Object> highValueProcessEvent() {
        return Map.of("event", "PROCESS", "payload",
                Map.of("amount", 1500.0, "currency", "USD", "customerType", "premium",
                        "customerRating", "A+", "riskScore", 150, "productCategory", "luxury",
                        "timeOfDay", "business_hours", "timestamp",
                        java.time.Instant.now().toString()));
    }

    /**
     * Creates a low-value process event payload
     */
    public static Map<String, Object> lowValueProcessEvent() {
        return Map.of("event", "PROCESS", "payload",
                Map.of("amount", 500.0, "currency", "USD", "customerType", "standard",
                        "customerRating", "B", "riskScore", 300, "productCategory", "basic",
                        "timeOfDay", "business_hours", "timestamp",
                        java.time.Instant.now().toString()));
    }

    /**
     * Creates a complex evaluation event payload for testing multiple conditions
     */
    public static Map<String, Object> complexEvaluationEvent() {
        return event("EVALUATE")
                .payload(Map.of("amount", 2000.0, "customerRating", "A+", "riskScore", 150,
                        "productCategory", "luxury", "timeOfDay", "business_hours"))
                .build().toMap();
    }

    /**
     * Creates a nested assessment event payload
     */
    public static Map<String, Object> nestedAssessmentEvent() {
        return event("ASSESS").payload(Map.of("primaryAmount", 1200.0, "secondaryAmount", 800.0,
                "customerSegment", "enterprise", "contractType", "annual")).build().toMap();
    }

    /**
     * Creates an approval event payload
     */
    public static Map<String, Object> approvalEvent() {
        return approvalEventBuilder("agent-456", 1000.0);
    }

    /**
     * Creates an approval event with custom agent and amount
     */
    public static Map<String, Object> approvalEvent(String approvedBy, double amount) {
        return approvalEventBuilder(approvedBy, amount);
    }

    /**
     * Creates a timeout event payload
     */
    public static Map<String, Object> timeoutEvent() {
        return event("TIMEOUT").payload(Map.of("timeoutReason", "Journey exceeded maximum duration",
                "timeoutDuration", "PT30S")).build().toMap();
    }

    /**
     * Creates a validation event payload
     */
    public static Map<String, Object> validationEvent(boolean validated) {
        return event("VALIDATE")
                .payload(Map.of("validated", validated, "validationTimestamp",
                        java.time.Instant.now().toString(), "validator", "system-validator"))
                .build().toMap();
    }

    /**
     * Creates a finalize event payload
     */
    public static Map<String, Object> finalizeEvent() {
        return event("FINALIZE")
                .payload(Map.of("finalized", true, "finalizationTimestamp",
                        java.time.Instant.now().toString(), "finalizer", "system-finalizer"))
                .build().toMap();
    }

    /**
     * Creates a multi-step event payload for sequential testing
     */
    public static Map<String, Object> multiStepEvent(int step) {
        return event(step == 1 ? "PROCESS" : step == 2 ? "VALIDATE" : "FINALIZE").payload(Map
                .of("step", step, "stepName", "step-" + step, "processedBy", "test-user-" + step))
                .build().toMap();
    }

    /**
     * Creates an incomplete event payload for error testing
     */
    public static Map<String, Object> incompleteEvent() {
        return Map.of("event", "PROCESS", "payload",
                Map.of("currency", "USD", "customerType", "premium"
                // Missing 'amount' field required by conditions
                ));
    }

    /**
     * Creates a custom event payload
     */
    public static Map<String, Object> customEvent(String eventType,
            Map<String, Object> customPayload) {
        return Map.of("event", eventType, "payload", Map.of("timestamp",
                java.time.Instant.now().toString(), "source", "test-framework"));
    }

    /**
     * Creates a batch of events for testing multiple scenarios
     */
    public static Map<String, Object>[] eventBatch() {
        return new Map[] {completionEvent("user-1"), processEvent(1500.0),
                approvalEvent("agent-1", 1000.0), validationEvent(true), finalizeEvent()};
    }

    /**
     * Creates an event payload with context data
     */
    public static Map<String, Object> contextualEvent(String eventType,
            Map<String, Object> context) {
        Map<String, Object> payload = Map.of("event", eventType, "payload",
                Map.of("timestamp", java.time.Instant.now().toString(), "context", context));
        return payload;
    }

    // Lombok builder pattern support
    public static EventPayload.EventPayloadBuilder event(String eventType) {
        Map<String, Object> defaultPayload = new HashMap<>();
        defaultPayload.put("timestamp", java.time.Instant.now().toString());

        return EventPayload.builder().event(eventType).payload(defaultPayload);
    }

    // Convenience methods for common event types
    public static Map<String, Object> completionEventBuilder(String completedBy) {
        return event("COMPLETE").payload(
                Map.of("completedBy", completedBy, "reason", "test completion by " + completedBy))
                .build().toMap();
    }

    public static Map<String, Object> processEventBuilder(double amount) {
        return event("PROCESS").payload(Map.of("amount", amount, "currency", "USD", "customerType",
                amount > 1000 ? "premium" : "standard")).build().toMap();
    }

    public static Map<String, Object> approvalEventBuilder(String approvedBy, double amount) {
        return event("APPROVE").payload(Map.of("approvedBy", approvedBy, "approvalAmount", amount,
                "approvalReason", "Approval by " + approvedBy)).build().toMap();
    }
}
