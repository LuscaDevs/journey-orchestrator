package com.luscadevs.journeyorchestrator.api.model;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or updating transition conditions.
 * 
 * This data transfer object represents condition information
 * sent from clients to the journey orchestration API.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransitionConditionRequest {
    
    /**
     * The condition expression string
     */
    @NotBlank(message = "Expression cannot be blank")
    @Size(max = 1000, message = "Expression cannot exceed 1000 characters")
    private String expression;
    
    /**
     * Human-readable description of the condition
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    /**
     * Order priority for this condition (when multiple conditions exist)
     */
    @NotNull(message = "Condition order cannot be null")
    private Integer conditionOrder;
}
