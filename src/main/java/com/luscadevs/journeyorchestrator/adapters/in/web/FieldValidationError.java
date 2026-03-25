package com.luscadevs.journeyorchestrator.adapters.in.web;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents a field-level validation error with detailed information
 * for client-side error handling and user feedback.
 */
@Getter
@Builder
public class FieldValidationError {
    /**
     * Name of the field that failed validation.
     * Example: "journeyDefinition.name"
     */
    private final String field;
    
    /**
     * Validation-specific error code for programmatic handling.
     * Example: "VALIDATION_003"
     */
    private final String errorCode;
    
    /**
     * Human-readable validation error message.
     * Example: "Journey definition name is required"
     */
    private final String message;
    
    /**
     * The actual value that was rejected by validation.
     * Can be null for missing fields.
     */
    private final Object rejectedValue;
}
