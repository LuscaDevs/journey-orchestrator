package com.luscadevs.journeyorchestrator.domain.connector;

import java.util.Map;
import java.util.Objects;

/**
 * Value object representing the result of a connector execution.
 * 
 * This value object is used for context enrichment - successful connector results
 * are merged into the execution context for use in subsequent journey steps.
 * 
 * On success: data is merged into execution context
 * On failure: errorMessage is recorded in audit trail
 */
public class ConnectorResult {
    
    private final boolean success;
    private final Map<String, Object> data;
    private final String errorMessage;
    private final Integer statusCode;
    
    protected ConnectorResult(boolean success, Map<String, Object> data, String errorMessage, Integer statusCode) {
        if (success && data == null) {
            throw new IllegalArgumentException("Data cannot be null when success is true");
        }
        if (!success && errorMessage == null) {
            throw new IllegalArgumentException("Error message is required when success is false");
        }
        
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }
    
    /**
     * Creates a successful connector result.
     * 
     * @param data The result data for context enrichment
     * @param statusCode Optional HTTP status code (for HTTP connectors)
     * @return A successful ConnectorResult
     */
    public static ConnectorResult success(Map<String, Object> data, Integer statusCode) {
        return new ConnectorResult(true, data, null, statusCode);
    }
    
    /**
     * Creates a successful connector result without status code.
     * 
     * @param data The result data for context enrichment
     * @return A successful ConnectorResult
     */
    public static ConnectorResult success(Map<String, Object> data) {
        return success(data, null);
    }
    
    /**
     * Creates a failed connector result.
     * 
     * @param errorMessage The error message describing the failure
     * @param statusCode Optional HTTP status code (for HTTP connectors)
     * @return A failed ConnectorResult
     */
    public static ConnectorResult failure(String errorMessage, Integer statusCode) {
        return new ConnectorResult(false, null, errorMessage, statusCode);
    }
    
    /**
     * Creates a failed connector result without status code.
     * 
     * @param errorMessage The error message describing the failure
     * @return A failed ConnectorResult
     */
    public static ConnectorResult failure(String errorMessage) {
        return failure(errorMessage, null);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Integer getStatusCode() {
        return statusCode;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectorResult that = (ConnectorResult) o;
        return success == that.success &&
                Objects.equals(data, that.data) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(statusCode, that.statusCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(success, data, errorMessage, statusCode);
    }
    
    @Override
    public String toString() {
        return "ConnectorResult{" +
                "success=" + success +
                ", data=" + data +
                ", errorMessage='" + errorMessage + '\'' +
                ", statusCode=" + statusCode +
                '}';
    }
}
