package com.luscadevs.journeyorchestrator.domain.journey;

/**
 * Enumeration of supported operators in condition expressions.
 * 
 * This enum defines the logical and comparison operators that can be used in SpEL expressions for
 * conditional transitions.
 */
public enum ConditionOperator {

    /**
     * Logical AND operator
     */
    LOGICAL_AND("AND"),

    /**
     * Logical OR operator
     */
    LOGICAL_OR("OR"),

    /**
     * Logical NOT operator
     */
    LOGICAL_NOT("NOT"),

    /**
     * Equality comparison
     */
    EQUALS("=="),

    /**
     * Inequality comparison
     */
    NOT_EQUALS("!="),

    /**
     * Greater than comparison
     */
    GREATER_THAN(">"),

    /**
     * Less than comparison
     */
    LESS_THAN("<"),

    /**
     * Greater than or equal comparison
     */
    GREATER_EQUAL(">="),

    /**
     * Less than or equal comparison
     */
    LESS_EQUAL("<="),

    /**
     * Open parenthesis
     */
    OPEN_PARENTHESIS("("),

    /**
     * Close parenthesis
     */
    CLOSE_PARENTHESIS(")");

    private final String symbol;

    ConditionOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Gets the symbol representation
     * 
     * @return Operator symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Checks if this is a logical operator
     * 
     * @return true if logical operator
     */
    public boolean isLogical() {
        return this == LOGICAL_AND || this == LOGICAL_OR || this == LOGICAL_NOT;
    }

    /**
     * Checks if this is a comparison operator
     * 
     * @return true if comparison operator
     */
    public boolean isComparison() {
        return this == EQUALS || this == NOT_EQUALS || this == GREATER_THAN || this == LESS_THAN
                || this == GREATER_EQUAL || this == LESS_EQUAL;
    }

    public boolean isParenthesis() {
        return this == OPEN_PARENTHESIS || this == CLOSE_PARENTHESIS;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
