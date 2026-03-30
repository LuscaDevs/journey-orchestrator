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
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for condition evaluation
 */
@SpringBootTest
class ConditionEvaluatorPropertyTest {

    private ConditionEvaluatorService conditionEvaluator;
    private Random random;

    @BeforeEach
    void setUp() {
        conditionEvaluator = new ConditionEvaluatorService();
        random = new Random(42); // Fixed seed for reproducible tests
    }

    @Test
    @DisplayName("Should satisfy commutative property for AND operations")
    void shouldSatisfyCommutativePropertyForAnd() {
        IntStream.range(0, 100).forEach(i -> {
            // Generate random test data
            Map<String, Object> eventData = generateRandomEventData();
            Map<String, Object> journeyData = generateRandomJourneyData();
            Map<String, Object> systemData = generateRandomSystemData();

            ContextData context = ContextData.builder().journeyInstanceId("test-" + i)
                    .currentState("PROCESSING").eventData(eventData).journeyData(journeyData)
                    .systemData(systemData).build();

            // Test commutative property: A AND B == B AND A
            String expression1 = "#eventData.amount > 1000 AND #journeyData.riskScore > 50";
            String expression2 = "#journeyData.riskScore > 50 AND #eventData.amount > 1000";

            ConditionEvaluationResult result1 = conditionEvaluator.evaluate(expression1, context);
            ConditionEvaluationResult result2 = conditionEvaluator.evaluate(expression2, context);

            assertTrue(result1.getSuccess());
            assertTrue(result2.getSuccess());
            assertEquals(result1.getResult(), result2.getResult(),
                    "AND operation should be commutative for iteration " + i);
        });
    }

    @Test
    @DisplayName("Should satisfy commutative property for OR operations")
    void shouldSatisfyCommutativePropertyForOr() {
        IntStream.range(0, 100).forEach(i -> {
            Map<String, Object> eventData = generateRandomEventData();
            Map<String, Object> journeyData = generateRandomJourneyData();
            Map<String, Object> systemData = generateRandomSystemData();

            ContextData context = ContextData.builder().journeyInstanceId("test-" + i)
                    .currentState("PROCESSING").eventData(eventData).journeyData(journeyData)
                    .systemData(systemData).build();

            // Test commutative property: A OR B == B OR A
            String expression1 = "#eventData.amount > 1000 OR #journeyData.riskScore > 50";
            String expression2 = "#journeyData.riskScore > 50 OR #eventData.amount > 1000";

            ConditionEvaluationResult result1 = conditionEvaluator.evaluate(expression1, context);
            ConditionEvaluationResult result2 = conditionEvaluator.evaluate(expression2, context);

            assertTrue(result1.getSuccess());
            assertTrue(result2.getSuccess());
            assertEquals(result1.getResult(), result2.getResult(),
                    "OR operation should be commutative for iteration " + i);
        });
    }

    @Test
    @DisplayName("Should satisfy associative property for AND operations")
    void shouldSatisfyAssociativePropertyForAnd() {
        IntStream.range(0, 50).forEach(i -> {
            Map<String, Object> eventData = generateRandomEventData();
            Map<String, Object> journeyData = generateRandomJourneyData();
            Map<String, Object> systemData = generateRandomSystemData();

            ContextData context = ContextData.builder().journeyInstanceId("test-" + i)
                    .currentState("PROCESSING").eventData(eventData).journeyData(journeyData)
                    .systemData(systemData).build();

            // Test associative property: (A AND B) AND C == A AND (B AND C)
            String expression1 =
                    "(#eventData.amount > 1000 AND #journeyData.riskScore > 50) AND #systemData.systemLoad < 0.8";
            String expression2 =
                    "#eventData.amount > 1000 AND (#journeyData.riskScore > 50 AND #systemData.systemLoad < 0.8)";

            ConditionEvaluationResult result1 = conditionEvaluator.evaluate(expression1, context);
            ConditionEvaluationResult result2 = conditionEvaluator.evaluate(expression2, context);

            assertTrue(result1.getSuccess());
            assertTrue(result2.getSuccess());
            assertEquals(result1.getResult(), result2.getResult(),
                    "AND operation should be associative for iteration " + i);
        });
    }

    @Test
    @DisplayName("Should satisfy associative property for OR operations")
    void shouldSatisfyAssociativePropertyForOr() {
        IntStream.range(0, 50).forEach(i -> {
            Map<String, Object> eventData = generateRandomEventData();
            Map<String, Object> journeyData = generateRandomJourneyData();
            Map<String, Object> systemData = generateRandomSystemData();

            ContextData context = ContextData.builder().journeyInstanceId("test-" + i)
                    .currentState("PROCESSING").eventData(eventData).journeyData(journeyData)
                    .systemData(systemData).build();

            // Test associative property: (A OR B) OR C == A OR (B OR C)
            String expression1 =
                    "(#eventData.amount > 1000 OR #journeyData.riskScore > 50) OR #systemData.systemLoad < 0.8";
            String expression2 =
                    "#eventData.amount > 1000 OR (#journeyData.riskScore > 50 OR #systemData.systemLoad < 0.8)";

            ConditionEvaluationResult result1 = conditionEvaluator.evaluate(expression1, context);
            ConditionEvaluationResult result2 = conditionEvaluator.evaluate(expression2, context);

            assertTrue(result1.getSuccess());
            assertTrue(result2.getSuccess());
            assertEquals(result1.getResult(), result2.getResult(),
                    "OR operation should be associative for iteration " + i);
        });
    }

    @Test
    @DisplayName("Should satisfy distributive property")
    void shouldSatisfyDistributiveProperty() {
        IntStream.range(0, 30).forEach(i -> {
            Map<String, Object> eventData = generateRandomEventData();
            Map<String, Object> journeyData = generateRandomJourneyData();
            Map<String, Object> systemData = generateRandomSystemData();

            ContextData context = ContextData.builder().journeyInstanceId("test-" + i)
                    .currentState("PROCESSING").eventData(eventData).journeyData(journeyData)
                    .systemData(systemData).build();

            // Test distributive property: A AND (B OR C) == (A AND B) OR (A AND C)
            String expression1 =
                    "#eventData.amount > 1000 AND (#journeyData.riskScore > 50 OR #systemData.systemLoad < 0.8)";
            String expression2 =
                    "(#eventData.amount > 1000 AND #journeyData.riskScore > 50) OR (#eventData.amount > 1000 AND #systemData.systemLoad < 0.8)";

            ConditionEvaluationResult result1 = conditionEvaluator.evaluate(expression1, context);
            ConditionEvaluationResult result2 = conditionEvaluator.evaluate(expression2, context);

            assertTrue(result1.getSuccess());
            assertTrue(result2.getSuccess());
            assertEquals(result1.getResult(), result2.getResult(),
                    "Distributive property should hold for iteration " + i);
        });
    }

    @Test
    @DisplayName("Should satisfy De Morgan's laws")
    void shouldSatisfyDeMorgansLaws() {
        IntStream.range(0, 50).forEach(i -> {
            Map<String, Object> eventData = generateRandomEventData();
            Map<String, Object> journeyData = generateRandomJourneyData();
            Map<String, Object> systemData = generateRandomSystemData();

            ContextData context = ContextData.builder().journeyInstanceId("test-" + i)
                    .currentState("PROCESSING").eventData(eventData).journeyData(journeyData)
                    .systemData(systemData).build();

            // Test De Morgan's first law: NOT (A AND B) == (NOT A) OR (NOT B)
            String expression1 = "NOT (#eventData.amount > 1000 AND #journeyData.riskScore > 50)";
            String expression2 =
                    "(NOT (#eventData.amount > 1000)) OR (NOT (#journeyData.riskScore > 50))";

            ConditionEvaluationResult result1 = conditionEvaluator.evaluate(expression1, context);
            ConditionEvaluationResult result2 = conditionEvaluator.evaluate(expression2, context);

            assertTrue(result1.getSuccess());
            assertTrue(result2.getSuccess());
            assertEquals(result1.getResult(), result2.getResult(),
                    "De Morgan's first law should hold for iteration " + i);

            // Test De Morgan's second law: NOT (A OR B) == (NOT A) AND (NOT B)
            String expression3 = "NOT (#eventData.amount > 1000 OR #journeyData.riskScore > 50)";
            String expression4 =
                    "(NOT (#eventData.amount > 1000)) AND (NOT (#journeyData.riskScore > 50))";

            ConditionEvaluationResult result3 = conditionEvaluator.evaluate(expression3, context);
            ConditionEvaluationResult result4 = conditionEvaluator.evaluate(expression4, context);

            assertTrue(result3.getSuccess());
            assertTrue(result4.getSuccess());
            assertEquals(result3.getResult(), result4.getResult(),
                    "De Morgan's second law should hold for iteration " + i);
        });
    }

    @Test
    @DisplayName("Should satisfy double negation property")
    void shouldSatisfyDoubleNegationProperty() {
        IntStream.range(0, 100).forEach(i -> {
            Map<String, Object> eventData = generateRandomEventData();
            Map<String, Object> journeyData = generateRandomJourneyData();
            Map<String, Object> systemData = generateRandomSystemData();

            ContextData context = ContextData.builder().journeyInstanceId("test-" + i)
                    .currentState("PROCESSING").eventData(eventData).journeyData(journeyData)
                    .systemData(systemData).build();

            // Test double negation: NOT (NOT A) == A
            String baseExpression = "#eventData.amount > 1000";
            String doubleNegationExpression = "NOT (NOT (#eventData.amount > 1000))";

            ConditionEvaluationResult baseResult =
                    conditionEvaluator.evaluate(baseExpression, context);
            ConditionEvaluationResult doubleNegationResult =
                    conditionEvaluator.evaluate(doubleNegationExpression, context);

            assertTrue(baseResult.getSuccess());
            assertTrue(doubleNegationResult.getSuccess());
            assertEquals(baseResult.getResult(), doubleNegationResult.getResult(),
                    "Double negation should equal original expression for iteration " + i);
        });
    }

    @Test
    @DisplayName("Should satisfy identity properties")
    void shouldSatisfyIdentityProperties() {
        IntStream.range(0, 50).forEach(i -> {
            Map<String, Object> eventData = generateRandomEventData();
            Map<String, Object> journeyData = generateRandomJourneyData();
            Map<String, Object> systemData = generateRandomSystemData();

            ContextData context = ContextData.builder().journeyInstanceId("test-" + i)
                    .currentState("PROCESSING").eventData(eventData).journeyData(journeyData)
                    .systemData(systemData).build();

            // Test identity for AND: A AND true == A
            String baseExpression = "#eventData.amount > 1000";
            String andTrueExpression = "#eventData.amount > 1000 AND true";

            ConditionEvaluationResult baseResult =
                    conditionEvaluator.evaluate(baseExpression, context);
            ConditionEvaluationResult andTrueResult =
                    conditionEvaluator.evaluate(andTrueExpression, context);

            assertTrue(baseResult.getSuccess());
            assertTrue(andTrueResult.getSuccess());
            assertEquals(baseResult.getResult(), andTrueResult.getResult(),
                    "AND with true should equal original expression for iteration " + i);

            // Test identity for OR: A OR false == A
            String orFalseExpression = "#eventData.amount > 1000 OR false";

            ConditionEvaluationResult orFalseResult =
                    conditionEvaluator.evaluate(orFalseExpression, context);

            assertTrue(orFalseResult.getSuccess());
            assertEquals(baseResult.getResult(), orFalseResult.getResult(),
                    "OR with false should equal original expression for iteration " + i);
        });
    }

    @Test
    @DisplayName("Should satisfy domination properties")
    void shouldSatisfyDominationProperties() {
        IntStream.range(0, 50).forEach(i -> {
            Map<String, Object> eventData = generateRandomEventData();
            Map<String, Object> journeyData = generateRandomJourneyData();
            Map<String, Object> systemData = generateRandomSystemData();

            ContextData context = ContextData.builder().journeyInstanceId("test-" + i)
                    .currentState("PROCESSING").eventData(eventData).journeyData(journeyData)
                    .systemData(systemData).build();

            // Test domination for AND: A AND false == false
            String andFalseExpression = "#eventData.amount > 1000 AND false";

            ConditionEvaluationResult andFalseResult =
                    conditionEvaluator.evaluate(andFalseExpression, context);

            assertTrue(andFalseResult.getSuccess());
            assertFalse(andFalseResult.getResult(),
                    "AND with false should always be false for iteration " + i);

            // Test domination for OR: A OR true == true
            String orTrueExpression = "#eventData.amount > 1000 OR true";

            ConditionEvaluationResult orTrueResult =
                    conditionEvaluator.evaluate(orTrueExpression, context);

            assertTrue(orTrueResult.getSuccess());
            assertTrue(orTrueResult.getResult(),
                    "OR with true should always be true for iteration " + i);
        });
    }

    @Test
    @DisplayName("Should maintain performance under complexity")
    void shouldMaintainPerformanceUnderComplexity() {
        // Test that evaluation time remains reasonable even for complex expressions
        IntStream.range(0, 20).forEach(i -> {
            Map<String, Object> eventData = generateRandomEventData();
            Map<String, Object> journeyData = generateRandomJourneyData();
            Map<String, Object> systemData = generateRandomSystemData();

            ContextData context = ContextData.builder().journeyInstanceId("test-" + i)
                    .currentState("PROCESSING").eventData(eventData).journeyData(journeyData)
                    .systemData(systemData).build();

            // Create a complex expression
            String complexExpression =
                    "(#eventData.amount > 1000 AND #journeyData.riskScore > 50) OR "
                            + "(#eventData.priority == 'HIGH' AND #systemData.systemLoad < 0.8) OR "
                            + "(#journeyData.approved == true AND NOT (#eventData.category == 'LOW_PRIORITY'))";

            long startTime = System.currentTimeMillis();
            ConditionEvaluationResult result =
                    conditionEvaluator.evaluate(complexExpression, context);
            long endTime = System.currentTimeMillis();

            assertTrue(result.getSuccess());
            assertTrue((endTime - startTime) < 50, // Should complete within 50ms
                    "Complex expression evaluation should complete within 50ms for iteration " + i
                            + ", took: " + (endTime - startTime) + "ms");
        });
    }

    private Map<String, Object> generateRandomEventData() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("amount", random.nextInt(5000));
        eventData.put("priority", random.nextBoolean() ? "HIGH" : "LOW");
        eventData.put("category", random.nextBoolean() ? "FINANCE" : "HEALTHCARE");
        return eventData;
    }

    private Map<String, Object> generateRandomJourneyData() {
        Map<String, Object> journeyData = new HashMap<>();
        journeyData.put("customerLevel", random.nextBoolean() ? "PREMIUM" : "STANDARD");
        journeyData.put("riskScore", random.nextInt(100));
        journeyData.put("approved", random.nextBoolean());
        return journeyData;
    }

    private Map<String, Object> generateRandomSystemData() {
        Map<String, Object> systemData = new HashMap<>();
        systemData.put("currentTime", "2025-03-30T10:00:00Z");
        systemData.put("systemLoad", random.nextDouble());
        return systemData;
    }
}
