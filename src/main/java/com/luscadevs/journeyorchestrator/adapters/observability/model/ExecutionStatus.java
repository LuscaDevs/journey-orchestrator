package com.luscadevs.journeyorchestrator.adapters.observability.model;

/**
 * Represents the outcome of method execution.
 */
public enum ExecutionStatus {
    /**
     * Method completed successfully.
     */
    SUCCESS,
    
    /**
     * Method failed with an exception.
     */
    FAILURE,
    
    /**
     * System error during logging (rare).
     */
    ERROR
}
