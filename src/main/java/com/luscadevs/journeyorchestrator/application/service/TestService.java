package com.luscadevs.journeyorchestrator.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Test service to verify execution observability logging.
 */
@Service
public class TestService {
    
    private static final Logger logger = LoggerFactory.getLogger(TestService.class);
    
    /**
     * Test method that should be logged.
     */
    public String performOperation(String input) {
        logger.info("Starting operation with input: {}", input);
        
        try {
            // Simulate some work
            Thread.sleep(100);
            
            String result = "Processed: " + input.toUpperCase();
            logger.info("Operation completed successfully");
            
            return result;
        } catch (InterruptedException e) {
            logger.error("Operation interrupted", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operation failed", e);
        }
    }
    
    /**
     * Async operation to test async logging.
     */
    public CompletableFuture<String> performAsyncOperation(String input) {
        logger.info("Starting async operation with input: {}", input);
        
        return CompletableFuture
            .supplyAsync(() -> {
                try {
                    Thread.sleep(50);
                    return "Async processed: " + input.toUpperCase();
                } catch (InterruptedException e) {
                    logger.error("Async operation interrupted", e);
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Async operation failed", e);
                }
            })
            .orTimeout(5, TimeUnit.SECONDS);
    }
}
