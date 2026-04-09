package com.luscadevs.journeyorchestrator.domain.journeyinstance;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Value object representing the result of condition evaluation.
 * 
 * This class encapsulates the outcome of condition evaluation including success status,
 * result value, error information, and performance metrics.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConditionEvaluationResult {
    
    /**
     * Whether evaluation succeeded
     */
    private Boolean success;
    
    /**
     * Evaluation result (if successful)
     */
    private Boolean result;
    
    /**
     * Error message (if failed)
     */
    private String errorMessage;
    
    /**
     * Type of error (if failed)
     */
    private ConditionErrorType errorType;
    
    /**
     * Time taken for evaluation
     */
    private java.time.Duration executionTime;
    
    /**
     * When evaluation occurred
     */
    private Instant evaluatedAt;
    
    /**
     * Creates a successful evaluation result
     * 
     * @param result Evaluation result
     * @param executionTime Time taken
     * @return Successful ConditionEvaluationResult
     */
    public static ConditionEvaluationResult success(Boolean result, java.time.Duration executionTime) {
        return ConditionEvaluationResult.builder()
                .success(true)
                .result(result)
                .executionTime(executionTime)
                .evaluatedAt(Instant.now())
                .build();
    }
    
    /**
     * Creates a failed evaluation result
     * 
     * @param errorType Type of error
     * @param errorMessage Error message
     * @param executionTime Time taken
     * @return Failed ConditionEvaluationResult
     */
    public static ConditionEvaluationResult failure(ConditionErrorType errorType, String errorMessage, java.time.Duration executionTime) {
        return ConditionEvaluationResult.builder()
                .success(false)
                .result(false)
                .errorType(errorType)
                .errorMessage(errorMessage)
                .executionTime(executionTime)
                .evaluatedAt(Instant.now())
                .build();
    }
    
    /**
     * Creates a timeout failure result
     * 
     * @param executionTime Time taken until timeout
     * @return Timeout failure ConditionEvaluationResult
     */
    public static ConditionEvaluationResult timeout(java.time.Duration executionTime) {
        return failure(ConditionErrorType.TIMEOUT, "Condition evaluation exceeded time limit", executionTime);
    }
    
    /**
     * Creates a syntax error failure result
     * 
     * @param errorMessage Syntax error message
     * @param executionTime Time taken
     * @return Syntax error failure ConditionEvaluationResult
     */
    public static ConditionEvaluationResult syntaxError(String errorMessage, java.time.Duration executionTime) {
        return failure(ConditionErrorType.SYNTAX_ERROR, errorMessage, executionTime);
    }
    
    /**
     * Creates a runtime error failure result
     * 
     * @param errorMessage Runtime error message
     * @param executionTime Time taken
     * @return Runtime error failure ConditionEvaluationResult
     */
    public static ConditionEvaluationResult runtimeError(String errorMessage, java.time.Duration executionTime) {
        return failure(ConditionErrorType.RUNTIME_ERROR, errorMessage, executionTime);
    }
    
    /**
     * Creates a security violation failure result
     * 
     * @param errorMessage Security error message
     * @param executionTime Time taken
     * @return Security violation failure ConditionEvaluationResult
     */
    public static ConditionEvaluationResult securityViolation(String errorMessage, java.time.Duration executionTime) {
        return failure(ConditionErrorType.SECURITY_VIOLATION, errorMessage, executionTime);
    }
    
    @Override
    public String toString() {
        return "ConditionEvaluationResult{" +
               "success=" + success +
               ", result=" + result +
               ", errorMessage='" + errorMessage + '\'' +
               ", errorType=" + errorType +
               ", executionTime=" + executionTime +
               ", evaluatedAt=" + evaluatedAt +
               '}';
    }
}
