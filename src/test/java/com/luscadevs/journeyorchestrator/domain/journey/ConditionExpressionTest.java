package com.luscadevs.journeyorchestrator.domain.journey;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import java.util.HashSet;

/**
 * Unit tests for ConditionExpression value object.
 * 
 * Tests cover expression parsing, complexity calculation, and property extraction functionality.
 */
class ConditionExpressionTest {

    @Test
    @DisplayName("Should create ConditionExpression from valid expression")
    void shouldCreateConditionExpressionFromValidExpression() {
        // Given
        String expression =
                "context.journeyData.amount > 1000 AND context.eventData.priority == 'HIGH'";

        // When
        ConditionExpression conditionExpression = ConditionExpression.fromString(expression);

        // Then
        assertNotNull(conditionExpression);
        assertEquals(expression, conditionExpression.getExpression());
        assertNotNull(conditionExpression.getComplexityScore());
        assertNotNull(conditionExpression.getReferencedProperties());
    }

    @Test
    @DisplayName("Should throw exception for null expression")
    void shouldThrowExceptionForNullExpression() {
        // Given
        String expression = null;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ConditionExpression.fromString(expression));

        assertEquals("Expression cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for empty expression")
    void shouldThrowExceptionForEmptyExpression() {
        // Given
        String expression = "";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ConditionExpression.fromString(expression));

        assertEquals("Expression cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should calculate complexity correctly for simple expression")
    void shouldCalculateComplexityCorrectlyForSimpleExpression() {
        // Given
        String expression = "context.journeyData.amount > 1000";

        // When
        ConditionExpression conditionExpression = ConditionExpression.fromString(expression);

        // Then
        assertEquals(1, conditionExpression.getComplexityScore()); // One comparison operator
    }

    @Test
    @DisplayName("Should calculate complexity correctly for complex expression")
    void shouldCalculateComplexityCorrectlyForComplexExpression() {
        // Given
        String expression =
                "context.journeyData.amount > 1000 AND context.eventData.priority == 'HIGH' OR context.eventData.source == 'API'";

        // When
        ConditionExpression conditionExpression = ConditionExpression.fromString(expression);

        // Then
        assertEquals(7, conditionExpression.getComplexityScore()); // > (1) + AND (2) + == (1) + OR
                                                                   // (2) + == (1) = 7
    }

    @Test
    @DisplayName("Should extract referenced properties correctly")
    void shouldExtractReferencedPropertiesCorrectly() {
        // Given
        String expression =
                "context.journeyData.amount > 1000 AND context.eventData.priority == 'HIGH'";

        // When
        ConditionExpression conditionExpression = ConditionExpression.fromString(expression);

        // Then
        Set<String> expectedProperties = new HashSet<>();
        expectedProperties.add("context.journeyData.amount");
        expectedProperties.add("context.eventData.priority");

        assertEquals(expectedProperties, conditionExpression.getReferencedProperties());
    }

    @Test
    @DisplayName("Should handle nested parentheses in complexity calculation")
    void shouldHandleNestedParenthesesInComplexityCalculation() {
        // Given
        String expression =
                "(context.journeyData.amount > 1000 AND context.eventData.priority == 'HIGH')";

        // When
        ConditionExpression conditionExpression = ConditionExpression.fromString(expression);

        // Then
        assertEquals(6, conditionExpression.getComplexityScore()); // 2 operators + 2 parentheses
    }

    @Test
    @DisplayName("Should calculate complexity correctly for complex expression")
    void shouldCalculateComplexityCorrectlyForComplexExpression2() {
        // Given
        String expression =
                "context.journeyData.amount > 1000 AND context.eventData.priority == 'HIGH'";

        // When
        ConditionExpression conditionExpression = ConditionExpression.fromString(expression);

        // Then
        assertEquals(4, conditionExpression.getComplexityScore()); // > (1) + AND (2) + == (1) = 4
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        String expression = "context.journeyData.amount > 1000";

        // When
        ConditionExpression conditionExpression = ConditionExpression.fromString(expression);

        // Then
        String toString = conditionExpression.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("ConditionExpression"));
        assertTrue(toString.contains(expression));
        assertTrue(toString.contains("complexityScore"));
        assertTrue(toString.contains("referencedProperties"));
    }
}
