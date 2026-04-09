package com.luscadevs.journeyorchestrator.e2e.framework.fixtures;

import java.util.Map;

/**
 * Factory class for creating complete test scenario templates.
 * Provides reusable test scenarios combining journey definitions and event payloads.
 */
public class TestScenarioTemplates {

    /**
     * Creates a complete simple journey scenario
     */
    public static TestScenario simpleJourneyScenario() {
        return new TestScenario(
            JourneyDefinitionFixtures.simpleJourney(),
            EventPayloadFixtures.completionEvent()
        );
    }

    /**
     * Creates a complete conditional journey scenario with high value
     */
    public static TestScenario highValueConditionalScenario() {
        return new TestScenario(
            JourneyDefinitionFixtures.conditionalJourney(),
            EventPayloadFixtures.highValueProcessEvent()
        );
    }

    /**
     * Creates a complete conditional journey scenario with low value
     */
    public static TestScenario lowValueConditionalScenario() {
        return new TestScenario(
            JourneyDefinitionFixtures.conditionalJourney(),
            EventPayloadFixtures.lowValueProcessEvent()
        );
    }

    /**
     * Creates a complete multi-state journey scenario
     */
    public static TestScenario multiStateScenario() {
        return new TestScenario(
            JourneyDefinitionFixtures.multiStateJourney(),
            EventPayloadFixtures.multiStepEvent(1)
        );
    }

    /**
     * Creates a complete data-centric journey scenario
     */
    public static TestScenario dataCentricScenario() {
        return new TestScenario(
            JourneyDefinitionFixtures.dataCentricJourney(),
            EventPayloadFixtures.approvalEvent()
        );
    }

    /**
     * Creates a complete timeout journey scenario
     */
    public static TestScenario timeoutScenario() {
        return new TestScenario(
            JourneyDefinitionFixtures.timeoutJourney(),
            EventPayloadFixtures.timeoutEvent()
        );
    }

    /**
     * Creates a complete complex conditional journey scenario
     */
    public static TestScenario complexConditionalScenario() {
        return new TestScenario(
            JourneyDefinitionFixtures.complexConditionalJourney(),
            EventPayloadFixtures.complexEvaluationEvent()
        );
    }

    /**
     * Creates a complete nested conditional journey scenario
     */
    public static TestScenario nestedConditionalScenario() {
        return new TestScenario(
            JourneyDefinitionFixtures.nestedConditionalJourney(),
            EventPayloadFixtures.nestedAssessmentEvent()
        );
    }

    /**
     * Creates a complete error scenario with incomplete event data
     */
    public static TestScenario errorScenario() {
        return new TestScenario(
            JourneyDefinitionFixtures.conditionalJourney(),
            EventPayloadFixtures.incompleteEvent()
        );
    }

    /**
     * Creates a batch of test scenarios for comprehensive testing
     */
    public static TestScenario[] comprehensiveTestSuite() {
        return new TestScenario[]{
            simpleJourneyScenario(),
            highValueConditionalScenario(),
            lowValueConditionalScenario(),
            multiStateScenario(),
            dataCentricScenario(),
            timeoutScenario(),
            complexConditionalScenario(),
            nestedConditionalScenario()
        };
    }

    /**
     * Creates a custom test scenario
     */
    public static TestScenario customScenario(Map<String, Object> journeyDefinition, Map<String, Object> eventPayload) {
        return new TestScenario(journeyDefinition, eventPayload);
    }

    /**
     * Creates a scenario with initial context
     */
    public static TestScenario scenarioWithContext(Map<String, Object> journeyDefinition, Map<String, Object> eventPayload, Map<String, Object> initialContext) {
        return new TestScenario(journeyDefinition, eventPayload, initialContext);
    }

    /**
     * Test scenario data holder
     */
    public static class TestScenario {
        private final Map<String, Object> journeyDefinition;
        private final Map<String, Object> eventPayload;
        private final Map<String, Object> initialContext;
        private final String description;

        public TestScenario(Map<String, Object> journeyDefinition, Map<String, Object> eventPayload) {
            this.journeyDefinition = journeyDefinition;
            this.eventPayload = eventPayload;
            this.initialContext = Map.of();
            this.description = "Test scenario with " + journeyDefinition.get("journeyCode");
        }

        public TestScenario(Map<String, Object> journeyDefinition, Map<String, Object> eventPayload, Map<String, Object> initialContext) {
            this.journeyDefinition = journeyDefinition;
            this.eventPayload = eventPayload;
            this.initialContext = initialContext;
            this.description = "Test scenario with " + journeyDefinition.get("journeyCode") + " and initial context";
        }

        public Map<String, Object> getJourneyDefinition() {
            return journeyDefinition;
        }

        public Map<String, Object> getEventPayload() {
            return eventPayload;
        }

        public Map<String, Object> getInitialContext() {
            return initialContext;
        }

        public String getDescription() {
            return description;
        }

        public String getJourneyCode() {
            return (String) journeyDefinition.get("journeyCode");
        }

        public String getEventType() {
            return (String) eventPayload.get("event");
        }

        /**
         * Gets the expected target state based on the scenario
         */
        public String getExpectedTargetState() {
            String journeyCode = getJourneyCode();
            
            if ("SIMPLE_JOURNEY".equals(journeyCode)) {
                return "END";
            } else if ("CONDITIONAL_JOURNEY".equals(journeyCode)) {
                Object amount = ((Map<String, Object>) eventPayload.get("payload")).get("amount");
                if (amount instanceof Number) {
                    double amt = ((Number) amount).doubleValue();
                    if (amt > 1000) return "HIGH_VALUE_APPROVED";
                    if (amt > 500) return "STANDARD_APPROVED";
                    return "LOW_VALUE_APPROVED";
                }
                return "CONDITION_ERROR";
            } else if ("MULTI_STATE_JOURNEY".equals(journeyCode)) {
                return "PROCESSING";
            } else if ("DATA_CENTRIC_JOURNEY".equals(journeyCode)) {
                return "APPROVED";
            } else if ("TIMEOUT_JOURNEY".equals(journeyCode)) {
                return "TIMEOUT";
            } else if ("COMPLEX_CONDITIONAL_JOURNEY".equals(journeyCode)) {
                return "PREMIUM_APPROVED";
            } else if ("NESTED_CONDITIONAL_JOURNEY".equals(journeyCode)) {
                return "ENTERISE_APPROVED";
            }
            
            return "UNKNOWN";
        }

        /**
         * Validates that the scenario is well-formed
         */
        public boolean isValid() {
            return journeyDefinition != null 
                && eventPayload != null
                && journeyDefinition.containsKey("journeyCode")
                && eventPayload.containsKey("event")
                && journeyDefinition.containsKey("states")
                && journeyDefinition.containsKey("transitions");
        }

        @Override
        public String toString() {
            return String.format("TestScenario{journeyCode='%s', event='%s', expectedState='%s'}", 
                               getJourneyCode(), getEventType(), getExpectedTargetState());
        }
    }

    /**
     * Scenario builder for creating complex scenarios
     */
    public static class ScenarioBuilder {
        private Map<String, Object> journeyDefinition;
        private Map<String, Object> eventPayload;
        private Map<String, Object> initialContext = Map.of();

        public ScenarioBuilder withJourney(Map<String, Object> journeyDefinition) {
            this.journeyDefinition = journeyDefinition;
            return this;
        }

        public ScenarioBuilder withEvent(Map<String, Object> eventPayload) {
            this.eventPayload = eventPayload;
            return this;
        }

        public ScenarioBuilder withContext(Map<String, Object> initialContext) {
            this.initialContext = initialContext;
            return this;
        }

        public TestScenario build() {
            if (journeyDefinition == null || eventPayload == null) {
                throw new IllegalStateException("Journey definition and event payload are required");
            }
            return new TestScenario(journeyDefinition, eventPayload, initialContext);
        }
    }

    /**
     * Creates a new scenario builder
     */
    public static ScenarioBuilder builder() {
        return new ScenarioBuilder();
    }
}
