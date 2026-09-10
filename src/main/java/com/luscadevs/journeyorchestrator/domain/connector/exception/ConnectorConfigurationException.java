package com.luscadevs.journeyorchestrator.domain.connector.exception;

/**
 * Exception thrown when connector configuration is invalid or missing required fields.
 */
public class ConnectorConfigurationException extends ConnectorException {
    
    public ConnectorConfigurationException(String message) {
        super(message);
    }
    
    public ConnectorConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ConnectorConfigurationException(String message, String connectorType, String connectorId) {
        super(message, connectorType, connectorId);
    }
}
