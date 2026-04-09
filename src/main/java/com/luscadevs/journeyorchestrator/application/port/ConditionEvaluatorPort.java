package com.luscadevs.journeyorchestrator.application.port;

import com.luscadevs.journeyorchestrator.domain.journey.ContextData;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.ConditionEvaluationResult;

/**
 * Application port for condition evaluation.
 * 
 * This port defines the input/output boundary for condition evaluation
 * services, following hexagonal architecture principles.
 */
public interface ConditionEvaluatorPort {
    
    /**
     * Evaluates a condition expression against the provided context
     * 
     * @param expression The condition expression to evaluate
     * @param context The runtime context data
     * @return Result of the evaluation
     */
    ConditionEvaluationResult evaluate(String expression, ContextData context);
    
    /**
     * Validates a condition expression for syntax and security
     * 
     * @param expression The condition expression to validate
     * @return True if valid, false otherwise
     */
    boolean validateExpression(String expression);
}
