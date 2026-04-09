package com.luscadevs.journeyorchestrator.domain.engine;

import com.luscadevs.journeyorchestrator.domain.journey.ContextData;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.ConditionEvaluationResult;

/**
 * Domain interface for condition evaluation.
 * 
 * This interface defines the contract for evaluating conditional expressions
 * against journey context data, maintaining business-agnostic behavior.
 */
public interface ConditionEvaluator {
    
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
