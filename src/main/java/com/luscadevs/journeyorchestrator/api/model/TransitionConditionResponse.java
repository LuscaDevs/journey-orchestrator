package com.luscadevs.journeyorchestrator.api.model;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO for transition condition responses.
 * 
 * This data transfer object represents condition information
 * returned from the journey orchestration API to clients.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransitionConditionResponse {
    
    /**
     * Unique identifier for the condition
     */
    private String id;
    
    /**
     * The condition expression string
     */
    private String expression;
    
    /**
     * Human-readable description of the condition
     */
    private String description;
    
    /**
     * Validation status of the condition
     */
    private ConditionValidationStatus validationStatus;
    
    /**
     * List of validation errors, if any
     */
    private List<String> validationErrors;
    
    /**
     * Complexity score for performance monitoring
     */
    private Integer complexityScore;
    
    /**
     * Properties referenced in the expression
     */
    private List<String> referencedProperties;
    
    /**
     * Creation timestamp
     */
    private Instant createdAt;
    
    /**
     * Last modification timestamp
     */
    private Instant updatedAt;
    
    /**
     * Condition validation status enumeration
     */
    public enum ConditionValidationStatus {
        VALID,
        INVALID,
        PENDING
    }
}
