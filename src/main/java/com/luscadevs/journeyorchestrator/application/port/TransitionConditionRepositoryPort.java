package com.luscadevs.journeyorchestrator.application.port;

import com.luscadevs.journeyorchestrator.domain.journey.TransitionCondition;

import java.util.List;
import java.util.Optional;

/**
 * Application port for transition condition repository.
 * 
 * This port defines the input/output boundary for transition condition
 * persistence operations, following hexagonal architecture principles.
 */
public interface TransitionConditionRepositoryPort {
    
    /**
     * Saves a transition condition
     * 
     * @param condition The condition to save
     * @return Saved condition
     */
    TransitionCondition save(TransitionCondition condition);
    
    /**
     * Finds a transition condition by ID
     * 
     * @param id The condition ID
     * @return Optional containing the condition if found
     */
    Optional<TransitionCondition> findById(String id);
    
    /**
     * Finds transition conditions by journey definition ID
     * 
     * @param journeyDefinitionId The journey definition ID
     * @return List of conditions for the journey definition
     */
    List<TransitionCondition> findByJourneyDefinitionId(String journeyDefinitionId);
    
    /**
     * Deletes a transition condition by ID
     * 
     * @param id The condition ID to delete
     * @return True if deleted, false if not found
     */
    boolean deleteById(String id);
    
    /**
     * Finds all conditions for a transition
     * 
     * @param transitionId The transition ID
     * @return List of conditions for the transition
     */
    List<TransitionCondition> findByTransitionId(String transitionId);
}
