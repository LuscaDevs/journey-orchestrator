package com.luscadevs.journeyorchestrator.domain.engine;

import com.luscadevs.journeyorchestrator.application.engine.ConditionEvaluatorService;
import com.luscadevs.journeyorchestrator.domain.journey.ContextData;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.ConditionEvaluationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for complex condition expression evaluation
 */
@SpringBootTest
class ConditionEvaluatorTest {

    private ConditionEvaluatorService conditionEvaluator;
    private ContextData testContext;

    @BeforeEach
    void setUp() {
        conditionEvaluator = new ConditionEvaluatorService();

        // Setup test context data
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("amount", 1500);
        eventData.put("priority", "HIGH");
        eventData.put("category", "FINANCE");

        Map<String, Object> journeyData = new HashMap<>();
        journeyData.put("customerLevel", "PREMIUM");
        journeyData.put("riskScore", 75);
        journeyData.put("approved", true);

        Map<String, Object> systemData = new HashMap<>();
        systemData.put("currentTime", "2025-03-30T10:00:00Z");
        systemData.put("systemLoad", 0.65);

        testContext = ContextData.builder().journeyInstanceId("test-instance-1")
                .currentState("PROCESSING").eventData(eventData).journeyData(journeyData)
                .systemData(systemData).build();
    }

    @Test
    @DisplayName("Should evaluate complex logical expressions with AND operators")
    void shouldEvaluateComplexAndExpressions() {
        String expression =
                "#eventData.amount > 1000 AND #eventData.priority == 'HIGH' AND #journeyData.customerLevel == 'PREMIUM'";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, testContext);

        assertTrue(result.getSuccess());
        assertTrue(result.getResult());
        assertTrue(result.getExecutionTime().toMillis() < 100); // Should be under 100ms
    }

    @Test
    @DisplayName("Should evaluate complex logical expressions with OR operators")
    void shouldEvaluateComplexOrExpressions() {
        String expression =
                "#eventData.priority == 'HIGH' OR #eventData.category == 'FINANCE' OR #journeyData.riskScore > 80";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, testContext);

        assertTrue(result.getSuccess());
        assertTrue(result.getResult()); // priority is HIGH, so should be true
    }

    @Test
    @DisplayName("Should evaluate complex logical expressions with NOT operators")
    void shouldEvaluateComplexNotExpressions() {
        String expression =
                "NOT (#eventData.priority == 'LOW') AND NOT (#journeyData.riskScore < 50)";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, testContext);

        assertTrue(result.getSuccess());
        assertTrue(result.getResult()); // priority is not LOW and riskScore is not < 50
    }

    @Test
    @DisplayName("Should evaluate nested expressions with parentheses")
    void shouldEvaluateNestedExpressions() {
        String expression =
                "(#eventData.amount > 1000 AND #eventData.priority == 'HIGH') OR (#journeyData.approved == true AND #systemData.systemLoad < 0.8)";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, testContext);

        assertTrue(result.getSuccess());
        assertTrue(result.getResult()); // First condition is true
    }

    @Test
    @DisplayName("Should evaluate deeply nested expressions")
    void shouldEvaluateDeeplyNestedExpressions() {
        String expression =
                "((#eventData.amount > 1000 AND #eventData.priority == 'HIGH') OR #journeyData.customerLevel == 'PREMIUM') AND NOT (#systemData.systemLoad > 0.9)";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, testContext);

        assertTrue(result.getSuccess());
        assertTrue(result.getResult()); // All conditions are met
    }

    @Test
    @DisplayName("Should evaluate mixed comparison operators")
    void shouldEvaluateMixedComparisonOperators() {
        String expression =
                "#eventData.amount >= 1000 AND #journeyData.riskScore <= 80 AND #systemData.systemLoad > 0.5 AND #systemData.systemLoad < 0.9";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, testContext);

        assertTrue(result.getSuccess());
        assertTrue(result.getResult()); // All conditions are met
    }

    @Test
    @DisplayName("Should evaluate inequality operators")
    void shouldEvaluateInequalityOperators() {
        String expression = "#eventData.priority != 'LOW' AND #eventData.category != 'HEALTHCARE'";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, testContext);

        assertTrue(result.getSuccess());
        assertTrue(result.getResult()); // Both conditions are true
    }

    @Test
    @DisplayName("Should handle complex expression that evaluates to false")
    void shouldHandleComplexFalseExpression() {
        String expression =
                "(#eventData.amount > 2000 AND #eventData.priority == 'CRITICAL') OR (#journeyData.riskScore > 90 AND #systemData.systemLoad > 0.8)";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, testContext);

        assertTrue(result.getSuccess());
        assertFalse(result.getResult()); // None of the conditions are met
    }

    @Test
    @DisplayName("Should handle expression with string concatenation and comparison")
    void shouldHandleStringOperations() {
        String expression = "#eventData.priority + '_' + #eventData.category == 'HIGH_FINANCE'";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, testContext);

        assertTrue(result.getSuccess());
        assertTrue(result.getResult());
    }

    @Test
    @DisplayName("Should handle expression with arithmetic operations")
    void shouldHandleArithmeticOperations() {
        String expression = "((#eventData.amount * 0.1) + #journeyData.riskScore) > 200";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, testContext);

        assertTrue(result.getSuccess());
        assertTrue(result.getResult()); // (1500 * 0.1) + 75 = 225 > 200
    }

    @Test
    @DisplayName("Should handle ternary operator expressions")
    void shouldHandleTernaryOperator() {
        String expression = "#eventData.amount > 1000 ? #journeyData.approved : false";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, testContext);

        assertTrue(result.getSuccess());
        assertTrue(result.getResult()); // amount > 1000 is true, so returns approved (true)
    }

    @Test
    @DisplayName("Should handle null safety operators")
    void shouldHandleNullSafetyOperators() {
        // Create context with null values
        Map<String, Object> eventDataWithNull = new HashMap<>();
        eventDataWithNull.put("amount", null);
        eventDataWithNull.put("priority", "HIGH");

        ContextData contextWithNull = ContextData.builder().journeyInstanceId("test-instance-2")
                .currentState("PROCESSING").eventData(eventDataWithNull)
                .journeyData(new HashMap<>()).systemData(new HashMap<>()).build();

        String expression = "#eventData.amount?.intValue() ?: 0 > 100";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, contextWithNull);

        assertTrue(result.getSuccess());
        assertFalse(result.getResult()); // amount is null, so defaults to 0, which is not > 100
    }

    @Test
    @DisplayName("Should handle regex matching in expressions")
    void shouldHandleRegexMatching() {
        String expression = "#eventData.priority matches 'HIGH|CRITICAL'";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, testContext);

        assertTrue(result.getSuccess());
        assertTrue(result.getResult()); // priority matches the regex
    }

    @Test
    @DisplayName("Should handle collection operations")
    void shouldHandleCollectionOperations() {
        // Add collection data to context
        Map<String, Object> journeyDataWithList = new HashMap<>(testContext.getJourneyData());
        journeyDataWithList.put("tags", java.util.List.of("IMPORTANT", "URGENT", "FINANCE"));

        ContextData contextWithList = ContextData.builder().journeyInstanceId("test-instance-3")
                .currentState("PROCESSING").eventData(testContext.getEventData())
                .journeyData(journeyDataWithList).systemData(testContext.getSystemData()).build();

        String expression = "#journeyData.tags.?[contains('URGENT')].size() > 0";

        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, contextWithList);

        assertTrue(result.getSuccess());
        assertTrue(result.getResult()); // List contains 'URGENT'
    }
}
