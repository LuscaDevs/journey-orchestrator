package com.luscadevs.journeyorchestrator.domain.journey;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a conditional expression that determines if a transition can be executed based on
 * runtime context data.
 * 
 * This entity contains the SpEL expression that will be evaluated against journey context data to
 * determine whether a transition should be executed.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransitionCondition {

    /**
     * Unique identifier for the condition
     */
    private String id;

    /**
     * Raw SpEL expression string
     */
    private String expression;

    /**
     * Pre-compiled SpEL expression (serialized)
     */
    private byte[] compiledExpression;

    /**
     * SHA-256 hash for integrity verification
     */
    private String validationHash;

    /**
     * Creation timestamp
     */
    private Instant createdAt;

    /**
     * Last modification timestamp
     */
    private Instant updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TransitionCondition that = (TransitionCondition) o;
        return Objects.equals(id, that.id) && Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, expression);
    }

    @Override
    public String toString() {
        return "TransitionCondition{" + "id='" + id + '\'' + ", expression='" + expression + '\''
                + ", validationHash='" + validationHash + '\'' + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt + '}';
    }
}
