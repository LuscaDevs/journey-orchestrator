package com.luscadevs.journeyorchestrator.adapters.observability.model;

/**
 * Represents the phase of method execution being logged.
 */
public enum ExecutionPhase {
    /**
     * Method execution has begun.
     */
    START,
    
    /**
     * Method execution has finished (success or failure).
     */
    COMPLETION
}
