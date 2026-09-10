package com.luscadevs.journeyorchestrator.domain.connector.exception;

/**
 * Base exception for all connector-related errors.
 * 
 * This exception hierarchy provides clear separation of different types of connector failures:
 * - Configuration errors (invalid parameters, missing required fields)
 * - Timeout errors (execution exceeded configured timeout)
 * - Network errors (connection failures, DNS issues)
 * - Execution errors (protocol-specific failures, HTTP errors, etc.)
 */
public class ConnectorException extends RuntimeException {
    
    private final String connectorType;
    private final String connectorId;
    
    public ConnectorException(String message) {
        super(message);
        this.connectorType = null;
        this.connectorId = null;
    }
    
    public ConnectorException(String message, Throwable cause) {
        super(message, cause);
        this.connectorType = null;
        this.connectorId = null;
    }
    
    public ConnectorException(String message, String connectorType, String connectorId) {
        super(message);
        this.connectorType = connectorType;
        this.connectorId = connectorId;
    }
    
    public ConnectorException(String message, Throwable cause, String connectorType, String connectorId) {
        super(message, cause);
        this.connectorType = connectorType;
        this.connectorId = connectorId;
    }
    
    public String getConnectorType() {
        return connectorType;
    }
    
    public String getConnectorId() {
        return connectorId;
    }
}
