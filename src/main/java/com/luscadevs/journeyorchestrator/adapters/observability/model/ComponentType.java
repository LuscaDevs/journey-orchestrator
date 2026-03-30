package com.luscadevs.journeyorchestrator.adapters.observability.model;

/**
 * Represents the type of component being logged.
 */
public enum ComponentType {
    /**
     * REST controller method.
     */
    CONTROLLER,
    
    /**
     * Application service method.
     */
    SERVICE
}
