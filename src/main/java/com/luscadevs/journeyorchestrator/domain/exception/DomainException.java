package com.luscadevs.journeyorchestrator.domain.exception;

import lombok.Getter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base exception class for all domain-specific business logic errors. Extends RuntimeException to
 * avoid checked exception overhead while maintaining type safety.
 */
@Getter
public abstract class DomainException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String details;

    // Business context can be added by subclasses
    protected Map<String, Object> context = new HashMap<>();

    protected DomainException(ErrorCode errorCode, String details) {
        super(details); // Pass details to RuntimeException constructor
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Adds contextual information to the exception.
     * 
     * @param key The context key
     * @param value The context value
     * @return This exception instance for method chaining
     */
    public DomainException withContext(String key, Object value) {
        context.put(key, value);
        return this;
    }

    /**
     * Returns an unmodifiable view of the exception context.
     * 
     * @return Immutable map containing contextual information
     */
    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(context);
    }
}
