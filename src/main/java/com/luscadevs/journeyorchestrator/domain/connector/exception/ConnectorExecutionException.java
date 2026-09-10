package com.luscadevs.journeyorchestrator.domain.connector.exception;

/**
 * Exception thrown when connector execution fails due to protocol-specific errors.
 * This includes HTTP errors, message broker errors, etc.
 */
public class ConnectorExecutionException extends ConnectorException {
    
    private final Integer statusCode;
    
    public ConnectorExecutionException(String message) {
        super(message);
        this.statusCode = null;
    }
    
    public ConnectorExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
    }
    
    public ConnectorExecutionException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public ConnectorExecutionException(String message, Throwable cause, Integer statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    public ConnectorExecutionException(String message, String connectorType, String connectorId, Integer statusCode) {
        super(message, connectorType, connectorId);
        this.statusCode = statusCode;
    }
    
    public Integer getStatusCode() {
        return statusCode;
    }
}
