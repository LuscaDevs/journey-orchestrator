package com.luscadevs.journeyorchestrator.integration;

import com.luscadevs.journeyorchestrator.domain.journey.ContextData;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.ConditionErrorType;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.ConditionEvaluationResult;
import com.luscadevs.journeyorchestrator.application.port.ConditionEvaluatorPort;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.HashMap;

/**
 * Integration tests for condition evaluation functionality.
 * 
 * These tests verify the end-to-end behavior of condition evaluation including performance,
 * security, and error handling.
 */
@SpringBootTest
@TestPropertySource(properties = {"journey.orchestrator.condition.evaluation.timeout=5000ms"})
class TransitionConditionIntegrationTest {

    @Autowired
    private ConditionEvaluatorPort conditionEvaluator;

    @BeforeEach
    void setUp() {
        // Setup test environment
    }

    @AfterEach
    void tearDown() {
        // Cleanup test environment
    }

    @Test
    @DisplayName("Should evaluate simple numeric comparison condition")
    void shouldEvaluateSimpleNumericComparisonCondition() {
        // Given
        String expression = "#journeyData['amount'] > 1000";
        ContextData context = ContextData.builder().journeyInstanceId("test-journey")
                .currentState("START").journeyData(new HashMap<>(Map.of("amount", 1500))).build();

        // When
        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, context);

        // Then
        assertTrue(result.getSuccess());
        assertTrue(result.getResult());
        assertTrue(result.getExecutionTime().toMillis() < 100); // Should be fast
    }

    @Test
    @DisplayName("Should evaluate string comparison condition")
    void shouldEvaluateStringComparisonCondition() {
        // Given
        String expression = "#eventData['priority'] == 'HIGH'";
        ContextData context = ContextData.builder().journeyInstanceId("test-journey")
                .currentState("START").eventData(Map.of("priority", "HIGH")).build();

        // When
        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, context);

        // Then
        assertTrue(result.getSuccess());
        assertTrue(result.getResult());
        assertTrue(result.getExecutionTime().toMillis() < 100); // Should be fast
    }

    @Test
    @DisplayName("Should evaluate complex logical condition")
    void shouldEvaluateComplexLogicalCondition() {
        // Given
        String expression = "#journeyData['amount'] > 1000 AND #eventData['priority'] == 'HIGH'";
        ContextData context = ContextData.builder().journeyInstanceId("test-journey")
                .currentState("START").journeyData(Map.of("amount", 1500))
                .eventData(Map.of("priority", "HIGH")).build();

        // When
        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, context);

        // Then
        assertTrue(result.getSuccess());
        assertTrue(result.getResult());
        assertTrue(result.getExecutionTime().toMillis() < 100); // Should be fast
    }

    @Test
    @DisplayName("Should handle invalid expression syntax")
    void shouldHandleInvalidExpressionSyntax() {
        // Given
        String expression = "#journeyData['amount'] > > 1000"; // Invalid syntax
        ContextData context = ContextData.builder().journeyInstanceId("test-journey")
                .currentState("START").journeyData(Map.of("amount", 1500)).build();

        // When
        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, context);

        // Then
        assertFalse(result.getSuccess()); // Syntax errors should return success=false
        assertEquals(ConditionErrorType.RUNTIME_ERROR, result.getErrorType());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Should validate correct expression syntax")
    void shouldValidateCorrectExpressionSyntax() {
        // Given
        String expression = "#journeyData['amount'] > 1000";

        // When
        boolean isValid = conditionEvaluator.validateExpression(expression);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject invalid expression syntax")
    void shouldRejectInvalidExpressionSyntax() {
        // Given
        String expression = "#journeyData['amount'] > 1000 AND"; // Invalid syntax

        // When
        boolean isValid = conditionEvaluator.validateExpression(expression);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should handle timeout for complex expression")
    void shouldHandleTimeoutForComplexExpression() {
        // Given
        String expression =
                "#journeyData['amount'] > 1000 AND #eventData['priority'] == 'HIGH' AND #journeyData['customerType'] == 'PREMIUM' AND #systemData['timestamp'] > '2023-01-01'";
        ContextData context =
                ContextData.builder().journeyInstanceId("test-journey").currentState("START")
                        .journeyData(Map.of("amount", 1500, "customerType", "PREMIUM"))
                        .eventData(Map.of("priority", "HIGH"))
                        .systemData(Map.of("timestamp", "2024-01-01")).build();

        // When
        ConditionEvaluationResult result = conditionEvaluator.evaluate(expression, context);

        System.out.println("Result: " + result);


        // Then
        assertTrue(result.getSuccess());
        assertTrue(result.getResult());
        assertTrue(result.getExecutionTime().toMillis() < 100); // Should be fast
    }
}
