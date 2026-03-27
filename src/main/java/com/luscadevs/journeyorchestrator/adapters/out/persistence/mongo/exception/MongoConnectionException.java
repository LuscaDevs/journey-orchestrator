package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.exception;

/**
 * Exception thrown when MongoDB connection fails.
 */
public class MongoConnectionException extends RuntimeException {
    
    public MongoConnectionException(String message) {
        super(message);
    }
    
    public MongoConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
