package com.luscadevs.journeyorchestrator.domain.journeyinstance;

/**
 * Enumeration of condition evaluation error types.
 * 
 * This enum defines the different types of errors that can occur during
 * condition expression evaluation, providing structured error categorization.
 */
public enum ConditionErrorType {
    
    /**
     * Expression syntax is invalid
     */
    SYNTAX_ERROR,
    
    /**
     * Runtime evaluation error
     */
    RUNTIME_ERROR,
    
    /**
     * Security constraint violation
     */
    SECURITY_VIOLATION,
    
    /**
     * Evaluation exceeded time limit
     */
    TIMEOUT,
    
    /**
     * Unexpected error
     */
    UNKNOWN_ERROR;
    
    /**
     * Gets a user-friendly description of the error type
     * 
     * @return Error description
     */
    public String getDescription() {
        return switch (this) {
            case SYNTAX_ERROR -> "Expression has invalid syntax";
            case RUNTIME_ERROR -> "Runtime evaluation error occurred";
            case SECURITY_VIOLATION -> "Security constraint violation";
            case TIMEOUT -> "Evaluation exceeded time limit";
            case UNKNOWN_ERROR -> "Unexpected error occurred";
        };
    }
}
