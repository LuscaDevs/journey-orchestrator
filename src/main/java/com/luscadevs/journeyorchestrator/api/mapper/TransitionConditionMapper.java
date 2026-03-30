package com.luscadevs.journeyorchestrator.api.mapper;

import com.luscadevs.journeyorchestrator.domain.journey.TransitionCondition;
import com.luscadevs.journeyorchestrator.api.model.TransitionConditionRequest;
import com.luscadevs.journeyorchestrator.api.model.TransitionConditionResponse;

import org.springframework.stereotype.Component;

/**
 * Mapper for transition condition DTOs.
 * 
 * This mapper handles conversion between domain entities and DTOs for transition conditions in the
 * journey orchestration API.
 */
@Component
public class TransitionConditionMapper {

    /**
     * Converts a request DTO to domain entity
     * 
     * @param request The request DTO
     * @return TransitionCondition domain entity
     */
    public static TransitionCondition toDomain(TransitionConditionRequest request) {
        if (request == null) {
            return null;
        }

        return TransitionCondition.builder().id(java.util.UUID.randomUUID().toString())
                .expression(request.getExpression()).build();
    }

    /**
     * Converts a domain entity to response DTO
     * 
     * @param condition The domain entity
     * @return Response DTO
     */
    public static TransitionConditionResponse toResponse(TransitionCondition condition) {
        if (condition == null) {
            return null;
        }

        return TransitionConditionResponse.builder().id(condition.getId())
                .expression(condition.getExpression())
                .validationStatus(TransitionConditionResponse.ConditionValidationStatus.VALID)
                .complexityScore(1) // Default complexity
                .referencedProperties(java.util.List.of()).createdAt(condition.getCreatedAt())
                .updatedAt(condition.getUpdatedAt()).build();
    }

    /**
     * Converts a domain entity to response DTO with validation status
     * 
     * @param condition The domain entity
     * @param validationStatus The validation status
     * @return Response DTO
     */
    public static TransitionConditionResponse toResponse(TransitionCondition condition,
            TransitionConditionResponse.ConditionValidationStatus validationStatus) {
        if (condition == null) {
            return null;
        }

        return TransitionConditionResponse.builder().id(condition.getId())
                .expression(condition.getExpression()).validationStatus(validationStatus)
                .complexityScore(1) // Default complexity
                .referencedProperties(java.util.List.of()).createdAt(condition.getCreatedAt())
                .updatedAt(condition.getUpdatedAt()).build();
    }
}
