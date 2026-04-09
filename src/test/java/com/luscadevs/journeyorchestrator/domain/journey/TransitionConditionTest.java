package com.luscadevs.journeyorchestrator.domain.journey;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Unit tests for TransitionCondition domain entity.
 * 
 * Tests cover entity behavior, validation, and business rules for transition conditions in journey
 * orchestration.
 */
class TransitionConditionTest {

        @Test
        @DisplayName("Should create TransitionCondition with all required fields")
        void shouldCreateTransitionConditionWithAllRequiredFields() {
                // Given
                String id = "condition-123";
                String expression = "context.journeyData.amount > 1000";
                byte[] compiledExpression = "compiled".getBytes();
                String validationHash = "hash123";
                Instant createdAt = Instant.now();
                Instant updatedAt = Instant.now();

                // When
                TransitionCondition condition = TransitionCondition.builder().id(id)
                                .expression(expression).compiledExpression(compiledExpression)
                                .validationHash(validationHash).createdAt(createdAt)
                                .updatedAt(updatedAt).build();

                // Then
                assertNotNull(condition);
                assertEquals(id, condition.getId());
                assertEquals(expression, condition.getExpression());
                assertArrayEquals(compiledExpression, condition.getCompiledExpression());
                assertEquals(validationHash, condition.getValidationHash());
                assertEquals(createdAt, condition.getCreatedAt());
                assertEquals(updatedAt, condition.getUpdatedAt());
        }

        @Test
        @DisplayName("Should implement equals correctly")
        void shouldImplementEqualsCorrectly() {
                // Given
                TransitionCondition condition1 = TransitionCondition.builder().id("test-id")
                                .expression("test-expression").build();

                TransitionCondition condition2 = TransitionCondition.builder().id("test-id")
                                .expression("test-expression").build();

                TransitionCondition condition3 = TransitionCondition.builder().id("different-id")
                                .expression("different-expression").build();

                // When & Then
                assertEquals(condition1, condition2);
                assertEquals(condition1, condition1);
                assertNotEquals(condition1, condition3);
        }

        @Test
        @DisplayName("Should implement hashCode correctly")
        void shouldImplementHashCodeCorrectly() {
                // Given
                TransitionCondition condition = TransitionCondition.builder().id("test-id")
                                .expression("test-expression").build();

                // Then
                int hashCode = condition.hashCode();

                // Then
                assertTrue(hashCode != 0);
                assertEquals(Objects.hash(condition.getId(), condition.getExpression()), hashCode);
        }

        @Test
        @DisplayName("Should implement toString correctly")
        void shouldImplementToStringCorrectly() {
                // Given
                TransitionCondition condition = TransitionCondition.builder().id("test-id")
                                .expression("test-expression").build();

                // When
                String toString = condition.toString();

                // Then
                assertNotNull(toString);
                assertTrue(toString.contains("TransitionCondition"));
                assertTrue(toString.contains("test-id"));
                assertTrue(toString.contains("test-expression"));
        }
}
