package com.luscadevs.journeyorchestrator.domain.engine;

import com.luscadevs.journeyorchestrator.domain.journey.ContextData;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.ConditionEvaluationResult;

import java.util.List;

/**
 * Domain interface for transition selection.
 * 
 * This interface defines the contract for selecting appropriate transitions
 * based on condition evaluation results, ensuring deterministic behavior.
 */
public interface TransitionSelector {
    
    /**
     * Selects the appropriate transition based on condition evaluation results
     * 
     * @param transitionIds List of transition IDs to evaluate
     * @param context The runtime context data
     * @return ID of selected transition or null if none match
     */
    String selectTransition(List<String> transitionIds, ContextData context);
    
    /**
     * Evaluates all conditions for a set of transitions
     * 
     * @param transitionIds List of transition IDs
     * @param context The runtime context data
     * @return List of evaluation results in order of evaluation
     */
    List<ConditionEvaluationResult> evaluateAllConditions(List<String> transitionIds, ContextData context);
}
