package com.luscadevs.journeyorchestrator.domain.connector.http;

/**
 * Enum representing supported HTTP methods for HTTP Connector.
 * 
 * This enum defines the HTTP methods that can be used when configuringan HTTP Connector
 * in a journey SERVICE_TASK state.
 */
public enum HttpMethod {
    /**
     * HTTP GET method - used to retrieve data.
     */
    GET,
    
    /**
     * HTTP POST method - used to create new resources.
     */
    POST,
    
    /**
     * HTTP PUT method - used to update existing resources.
     */
    PUT,
    
    /**
     * HTTP DELETE method - used to delete resources.
     */
    DELETE,
    
    /**
     * HTTP PATCH method - used for partial updates.
     */
    PATCH
}
