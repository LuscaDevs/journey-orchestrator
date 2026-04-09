package com.luscadevs.journeyorchestrator.adapters.in.web;

import com.luscadevs.journeyorchestrator.domain.journey.TransitionCondition;
import com.luscadevs.journeyorchestrator.api.model.TransitionConditionRequest;
import com.luscadevs.journeyorchestrator.api.model.TransitionConditionResponse;
import com.luscadevs.journeyorchestrator.api.mapper.TransitionConditionMapper;
import com.luscadevs.journeyorchestrator.application.port.TransitionConditionRepositoryPort;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.time.Instant;

/**
 * REST controller for transition condition management.
 * 
 * This controller provides API endpoints for creating, updating, and managing transition conditions
 * in journey orchestration.
 */
@RestController
@RequestMapping("/api/v1/journey-definitions/{journeyDefinitionId}/transitions/{transitionId}/conditions")
public class TransitionConditionController {

    private final TransitionConditionRepositoryPort conditionRepository;

    public TransitionConditionController(TransitionConditionRepositoryPort conditionRepository) {
        this.conditionRepository = conditionRepository;
    }

    /**
     * Creates a new transition condition
     * 
     * @param journeyDefinitionId Journey definition ID
     * @param transitionId Transition ID
     * @param request Condition creation request
     * @return Created condition response
     */
    @PostMapping
    public ResponseEntity<TransitionConditionResponse> createCondition(
            @PathVariable String journeyDefinitionId, @PathVariable String transitionId,
            @RequestBody TransitionConditionRequest request) {

        // Validate request
        if (request == null || request.getExpression() == null
                || request.getExpression().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Convert request to domain entity with additional fields
        TransitionCondition condition =
                TransitionConditionMapper.toDomain(request, journeyDefinitionId, transitionId);

        // Save to repository
        TransitionCondition savedCondition = conditionRepository.save(condition);

        return ResponseEntity.ok(TransitionConditionMapper.toResponse(savedCondition));
    }

    /**
     * Gets all conditions for a transition
     * 
     * @param journeyDefinitionId Journey definition ID
     * @param transitionId Transition ID
     * @return List of conditions
     */
    @GetMapping
    public ResponseEntity<List<TransitionConditionResponse>> getConditions(
            @PathVariable String journeyDefinitionId, @PathVariable String transitionId) {

        // Fetch from repository
        List<TransitionCondition> conditions = conditionRepository.findByTransitionId(transitionId);

        List<TransitionConditionResponse> responses = conditions.stream()
                .map(TransitionConditionMapper::toResponse).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Gets a specific condition by ID
     * 
     * @param journeyDefinitionId Journey definition ID
     * @param transitionId Transition ID
     * @param conditionId Condition ID
     * @return Condition details
     */
    @GetMapping("/{conditionId}")
    public ResponseEntity<TransitionConditionResponse> getCondition(
            @PathVariable String journeyDefinitionId, @PathVariable String transitionId,
            @PathVariable String conditionId) {

        // Fetch from repository
        return conditionRepository.findById(conditionId).map(
                condition -> ResponseEntity.ok(TransitionConditionMapper.toResponse(condition)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing condition
     * 
     * @param journeyDefinitionId Journey definition ID
     * @param transitionId Transition ID
     * @param conditionId Condition ID
     * @param request Condition update request
     * @return Updated condition response
     */
    @PutMapping("/{conditionId}")
    public ResponseEntity<TransitionConditionResponse> updateCondition(
            @PathVariable String journeyDefinitionId, @PathVariable String transitionId,
            @PathVariable String conditionId, @RequestBody TransitionConditionRequest request) {

        // Validate request
        if (request == null || request.getExpression() == null
                || request.getExpression().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Fetch and update existing condition
        return conditionRepository.findById(conditionId).map(existingCondition -> {
            TransitionCondition updatedCondition = TransitionCondition.builder().id(conditionId)
                    .journeyDefinitionId(journeyDefinitionId).transitionId(transitionId)
                    .expression(request.getExpression())
                    .compiledExpression(existingCondition.getCompiledExpression())
                    .validationHash(existingCondition.getValidationHash())
                    .createdAt(existingCondition.getCreatedAt()).updatedAt(Instant.now()).build();

            TransitionCondition savedCondition = conditionRepository.save(updatedCondition);
            return ResponseEntity.ok(TransitionConditionMapper.toResponse(savedCondition));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a condition
     * 
     * @param journeyDefinitionId Journey definition ID
     * @param transitionId Transition ID
     * @param conditionId Condition ID
     * @return Success response
     */
    @DeleteMapping("/{conditionId}")
    public ResponseEntity<Void> deleteCondition(@PathVariable String journeyDefinitionId,
            @PathVariable String transitionId, @PathVariable String conditionId) {

        // Delete from repository
        boolean deleted = conditionRepository.deleteById(conditionId);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
