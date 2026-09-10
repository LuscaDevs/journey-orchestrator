package com.luscadevs.journeyorchestrator.application.port.out;

import com.luscadevs.journeyorchestrator.domain.connector.Connector;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorConfiguration;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorType;

/**
 * Output port for resolving connector implementations.
 * 
 * This port defines the contract for resolving the appropriate connector implementation
 * based on the connector type configuration. This follows the Strategy pattern.
 */
public interface ConnectorResolverPort {
    
    /**
     * Resolves the connector implementation for the given configuration.
     * 
     * @param configuration The connector configuration
     * @return The connector implementation
     * @throws IllegalArgumentException if no connector is found for the type
     */
    Connector<? extends ConnectorConfiguration> resolve(ConnectorConfiguration configuration);
    
    /**
     * Resolves the connector implementation for the given connector type.
     * 
     * @param connectorType The connector type
     * @return The connector implementation
     * @throws IllegalArgumentException if no connector is found for the type
     */
    Connector<? extends ConnectorConfiguration> resolve(ConnectorType connectorType);
    
    /**
     * Checks if a connector implementation is available for the given type.
     * 
     * @param connectorType The connector type
     * @return true if a connector implementation is available, false otherwise
     */
    boolean isAvailable(ConnectorType connectorType);
}
