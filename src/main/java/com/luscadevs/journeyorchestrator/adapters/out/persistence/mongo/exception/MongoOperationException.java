package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.exception;

/**
 * Exception thrown when MongoDB operation fails.
 */
public class MongoOperationException extends RuntimeException {
    
    public MongoOperationException(String message) {
        super(message);
    }
    
    public MongoOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
