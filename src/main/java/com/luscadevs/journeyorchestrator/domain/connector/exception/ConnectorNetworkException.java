package com.luscadevs.journeyorchestrator.domain.connector.exception;

/**
 * Exception thrown when connector execution fails due to network-related issues.
 * This includes connection failures, DNS resolution errors, timeouts, etc.
 */
public class ConnectorNetworkException extends ConnectorException {
    
    public ConnectorNetworkException(String message) {
        super(message);
    }
    
    public ConnectorNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ConnectorNetworkException(String message, String connectorType, String connectorId) {
        super(message, connectorType, connectorId);
    }
    
    public ConnectorNetworkException(String message, Throwable cause, String connectorType, String connectorId) {
        super(message, cause, connectorType, connectorId);
    }
}
