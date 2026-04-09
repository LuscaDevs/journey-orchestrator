package com.luscadevs.journeyorchestrator.e2e.framework.fixtures;

import java.util.Map;

/**
 * Factory class for creating journey definition test fixtures. Provides reusable journey
 * definitions for E2E testing scenarios.
 */
public class JourneyDefinitionFixtures {

        /**
         * Creates a simple journey definition with basic START -> END flow
         */
        public static Map<String, Object> simpleJourney() {
                return Map.of("journeyCode", "SIMPLE_JOURNEY", "name", "Simple Test Journey",
                                "version", 1, "active", true, "states",
                                java.util.List.of(Map.of("name", "START", "type", "INITIAL",
                                                "description", "Initial state of the journey"),
                                                Map.of("name", "END", "type", "FINAL",
                                                                "description",
                                                                "Final state of the journey")),
                                "transitions",
                                java.util.List.of(Map.of("source", "START", "event", "COMPLETE",
                                                "target", "END", "description",
                                                "Transition to end state")));
        }

        /**
         * Creates a conditional journey definition with multiple paths
         */
        public static Map<String, Object> conditionalJourney() {
                return Map.of("journeyCode", "CONDITIONAL_JOURNEY", "name",
                                "Conditional Test Journey", "version", 1, "active", true,
                                "description",
                                "A journey with conditional transitions based on event data",
                                "states",
                                java.util.List.of(Map.of(
                                                "name", "START", "type", "INITIAL", "description",
                                                "Initial state for conditional evaluation"),
                                                Map.of("name", "HIGH_VALUE_APPROVED", "type",
                                                                "FINAL", "description",
                                                                "High value approval final state"),
                                                Map.of("name", "STANDARD_APPROVED", "type", "FINAL",
                                                                "description",
                                                                "Standard approval final state"),
                                                Map.of("name", "LOW_VALUE_APPROVED", "type",
                                                                "FINAL", "description",
                                                                "Low value approval final state"),
                                                Map.of("name", "CONDITION_ERROR", "type", "FINAL",
                                                                "description",
                                                                "Condition evaluation error state")),
                                "transitions",
                                java.util.List.of(Map.of("source", "START", "event", "PROCESS",
                                                "target", "HIGH_VALUE_APPROVED", "condition",
                                                "#eventData.amount > 1000", "description",
                                                "High value approval path"),
                                                Map.of("source", "START", "event", "PROCESS",
                                                                "target", "STANDARD_APPROVED",
                                                                "condition",
                                                                "#eventData.amount > 500 && #eventData.amount <= 1000",
                                                                "description",
                                                                "Standard approval path"),
                                                Map.of("source", "START", "event", "PROCESS",
                                                                "target", "LOW_VALUE_APPROVED",
                                                                "condition",
                                                                "#eventData.amount <= 500",
                                                                "description",
                                                                "Low value approval path"),
                                                Map.of("source", "START", "event", "PROCESS",
                                                                "target", "CONDITION_ERROR",
                                                                "description",
                                                                "Default path for condition evaluation errors")));
        }

        /**
         * Creates a multi-state journey definition with sequential processing
         */
        public static Map<String, Object> multiStateJourney() {
                return Map.of("journeyCode", "MULTI_STATE_JOURNEY", "name",
                                "Multi State Test Journey", "version", 1, "description",
                                "A journey with multiple sequential states", "states",
                                Map.of("name", "INITIAL", "type", "INITIAL", "description",
                                                "Initial state"),
                                "transitions",
                                Map.of("source", "INITIAL", "event", "PROCESS", "target",
                                                "PROCESSING", "description", "Start processing"),
                                "additionalTransitions",
                                Map.of("transitions", java.util.List.of(
                                                Map.of("source", "PROCESSING", "event", "VALIDATE",
                                                                "target", "VALIDATED",
                                                                "description", "Validation step"),
                                                Map.of("source", "VALIDATED", "event", "FINALIZE",
                                                                "target", "FINAL", "description",
                                                                "Finalization step"))),
                                "endStates", Map.of("name", "FINAL", "type", "FINAL", "description",
                                                "Final state"));
        }

        /**
         * Creates a data-centric journey definition for testing context management
         */
        public static Map<String, Object> dataCentricJourney() {
                return Map.of("journeyCode", "DATA_CENTRIC_JOURNEY", "name",
                                "Data Centric Test Journey", "version", 1, "active", true,
                                "description", "A journey focused on data context management",
                                "states",
                                java.util.List.of(Map.of(
                                                "name", "START", "type", "INITIAL", "description",
                                                "Initial state with data requirements"),
                                                Map.of("name", "APPROVED", "type", "FINAL",
                                                                "description",
                                                                "Approved state with preserved data")),
                                "transitions",
                                java.util.List.of(Map.of("source", "START", "event", "APPROVE",
                                                "target", "APPROVED", "description",
                                                "Approval process with data preservation")));
        }

        /**
         * Creates a timeout journey definition for testing timeout scenarios
         */
        public static Map<String, Object> timeoutJourney() {
                return Map.of("journeyCode", "TIMEOUT_JOURNEY", "name", "Timeout Test Journey",
                                "version", 1, "description", "A journey with timeout configuration",
                                "states",
                                Map.of("name", "START", "type", "INITIAL", "description",
                                                "Initial state with timeout"),
                                "transitions",
                                Map.of("source", "START", "event", "TIMEOUT", "target", "TIMEOUT",
                                                "description", "Timeout transition"),
                                "endStates",
                                Map.of("name", "TIMEOUT", "type", "FINAL", "description",
                                                "Timeout final state"),
                                "timeout",
                                Map.of("duration", "PT30S", "targetState", "TIMEOUT", "description",
                                                "30 second timeout to timeout state"));
        }

        /**
         * Creates a complex conditional journey with nested logic
         */
        public static Map<String, Object> complexConditionalJourney() {
                return Map.of("journeyCode", "COMPLEX_CONDITIONAL_JOURNEY", "name",
                                "Complex Conditional Test Journey", "version", 1, "description",
                                "A journey with complex nested conditional logic", "states",
                                Map.of("name", "START", "type", "INITIAL", "description",
                                                "Initial state for complex evaluation"),
                                "transitions",
                                Map.of("source", "START", "event", "EVALUATE", "target",
                                                "PREMIUM_APPROVED", "condition",
                                                "#eventData.amount > 1500 && #eventData.customerRating == 'A+' && #eventData.riskScore < 200 && #eventData.productCategory == 'luxury' && #eventData.timeOfDay == 'business_hours'",
                                                "description",
                                                "Premium approval with multiple conditions"),
                                "alternativeTransitions",
                                Map.of("transitions", java.util.List.of(Map.of("source", "START",
                                                "event", "EVALUATE", "target", "STANDARD_APPROVED",
                                                "condition",
                                                "#eventData.amount > 500 && #eventData.amount <= 1500",
                                                "description", "Standard approval path"),
                                                Map.of("source", "START", "event", "EVALUATE",
                                                                "target", "BASIC_APPROVED",
                                                                "condition",
                                                                "#eventData.amount <= 500",
                                                                "description",
                                                                "Basic approval path"),
                                                Map.of("source", "START", "event", "EVALUATE",
                                                                "target", "CONDITION_ERROR",
                                                                "description",
                                                                "Default path for condition evaluation errors"))),
                                "endStates", Map.of("name", "PREMIUM_APPROVED", "type", "FINAL",
                                                "description", "Premium approval final state"));
        }

        /**
         * Creates a nested conditional journey with complex logic
         */
        public static Map<String, Object> nestedConditionalJourney() {
                return Map.of("journeyCode", "NESTED_CONDITIONAL_JOURNEY", "name",
                                "Nested Conditional Test Journey", "version", 1, "description",
                                "A journey with nested conditional logic", "states",
                                Map.of("name", "START", "type", "INITIAL", "description",
                                                "Initial state for nested evaluation"),
                                "transitions",
                                Map.of("source", "START", "event", "ASSESS", "target",
                                                "ENTERISE_APPROVED", "condition",
                                                "((#eventData.primaryAmount > 1000 && #eventData.secondaryAmount > 500) || (#eventData.primaryAmount + #eventData.secondaryAmount > 2000)) && #eventData.customerSegment == 'enterprise' && #eventData.contractType == 'annual'",
                                                "description",
                                                "Enterprise approval with nested conditions"),
                                "alternativeTransitions",
                                Map.of("transitions", java.util.List.of(Map.of("source", "START",
                                                "event", "ASSESS", "target", "SME_APPROVED",
                                                "condition",
                                                "#eventData.primaryAmount > 500 && #eventData.customerSegment == 'sme'",
                                                "description", "SME approval path"),
                                                Map.of("source", "START", "event", "ASSESS",
                                                                "target", "INDIVIDUAL_APPROVED",
                                                                "condition",
                                                                "#eventData.customerSegment == 'individual'",
                                                                "description",
                                                                "Individual approval path"),
                                                Map.of("source", "START", "event", "ASSESS",
                                                                "target", "CONDITION_ERROR",
                                                                "description",
                                                                "Default path for condition evaluation errors"))),
                                "endStates", Map.of("name", "ENTERISE_APPROVED", "type", "FINAL",
                                                "description", "Enterprise approval final state"));
        }

        /**
         * Creates a custom journey definition with specified parameters
         */
        public static Map<String, Object> customJourney(String journeyCode, String name,
                        String fromState, String event, String toState) {
                return Map.of("journeyCode", journeyCode, "name", name, "version", 1, "description",
                                "Custom journey: " + name, "states",
                                Map.of("name", fromState, "type", "INITIAL", "description",
                                                "Custom initial state"),
                                "transitions",
                                Map.of("source", fromState, "event", event, "target", toState,
                                                "description", "Custom transition"),
                                "endStates", Map.of("name", toState, "type", "FINAL", "description",
                                                "Custom final state"));
        }
}
