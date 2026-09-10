package com.luscadevs.journeyorchestrator.domain.connector;

import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

/**
 * Strategy interface for connector implementations.
 * 
 * This interface defines the contract for all connector types (HTTP, Kafka, RabbitMQ, etc.).
 * The runtime delegates connector execution to concrete implementations without knowledge
 * of protocol-specific details, maintaining clean architecture principles.
 * 
 * Implementations must:
 * - Handle null context gracefully
 * - Throw ConnectorException on failure
 * - Return non-null ConnectorResult
 * 
 * @param <C> The type of connector configuration (extends ConnectorConfiguration)
 */
public interface Connector<C extends ConnectorConfiguration> {
    
    /**
     * Executes the connector with the given configuration and execution context.
     * 
     * @param config The connector configuration containing parameters for execution
     * @param context The journey instance containing variables and state data
     * @return ConnectorResult containing execution result data or error information
     * @throws ConnectorException if execution fails
     */
    ConnectorResult execute(C config, JourneyInstance context);
}
