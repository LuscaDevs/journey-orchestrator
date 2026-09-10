package com.luscadevs.journeyorchestrator.domain.connector.exception;

/**
 * Exception thrown when connector execution exceeds the configured timeout.
 */
public class ConnectorTimeoutException extends ConnectorException {
    
    private final long timeoutMs;
    private final long actualDurationMs;
    
    public ConnectorTimeoutException(String message, long timeoutMs, long actualDurationMs) {
        super(message);
        this.timeoutMs = timeoutMs;
        this.actualDurationMs = actualDurationMs;
    }
    
    public ConnectorTimeoutException(String message, Throwable cause, long timeoutMs, long actualDurationMs) {
        super(message, cause);
        this.timeoutMs = timeoutMs;
        this.actualDurationMs = actualDurationMs;
    }
    
    public ConnectorTimeoutException(String message, String connectorType, String connectorId, 
                                      long timeoutMs, long actualDurationMs) {
        super(message, connectorType, connectorId);
        this.timeoutMs = timeoutMs;
        this.actualDurationMs = actualDurationMs;
    }
    
    public long getTimeoutMs() {
        return timeoutMs;
    }
    
    public long getActualDurationMs() {
        return actualDurationMs;
    }
}
