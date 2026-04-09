package com.luscadevs.journeyorchestrator.api.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import com.luscadevs.journey.api.generated.JourneysApi;
import com.luscadevs.journey.api.generated.model.CreateJourneyDefinitionRequest;
import com.luscadevs.journey.api.generated.model.JourneyDefinitionResponse;
import com.luscadevs.journeyorchestrator.api.mapper.JourneyDefinitionMapper;
import com.luscadevs.journeyorchestrator.application.engine.ConditionEvaluatorService;
import com.luscadevs.journeyorchestrator.application.service.JourneyDefinitionService;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
public class JourneyDefinitionController implements JourneysApi {

    private static final Logger logger = LoggerFactory.getLogger(JourneyDefinitionController.class);

    private final JourneyDefinitionService journeyDefinitionService;
    private final ConditionEvaluatorService conditionEvaluatorService;

    public JourneyDefinitionController(JourneyDefinitionService journeyDefinitionService,
            ConditionEvaluatorService conditionEvaluatorService) {
        this.journeyDefinitionService = journeyDefinitionService;
        this.conditionEvaluatorService = conditionEvaluatorService;
        // Initialize the mapper with the condition evaluator
        JourneyDefinitionMapper.setConditionEvaluator(conditionEvaluatorService);
    }

    @Override
    public ResponseEntity<JourneyDefinitionResponse> createJourneyDefinition(
            @Valid CreateJourneyDefinitionRequest request) {

        JourneyDefinition definition = journeyDefinitionService.createJourneyDefinition(request);

        JourneyDefinitionResponse response = JourneyDefinitionMapper.toResponse(definition);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<List<JourneyDefinitionResponse>> getJourneyDefinitionsByCode(
            @NotNull String journeyCode) {
        List<JourneyDefinition> definitions =
                journeyDefinitionService.getJourneyDefinitionsByCode(journeyCode);
        List<JourneyDefinitionResponse> responses =
                definitions.stream().map(JourneyDefinitionMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        // TODO Auto-generated method stub
        return JourneysApi.super.getRequest();
    }

    @Override
    public ResponseEntity<List<JourneyDefinitionResponse>> listJourneyDefinitions() {
        List<JourneyDefinition> definitions = journeyDefinitionService.getAllJourneyDefinitions();

        logger.info("Found {} journey definitions", definitions.size());

        List<JourneyDefinitionResponse> responses = definitions.stream().map(definition -> {
            try {
                return JourneyDefinitionMapper.toResponse(definition);
            } catch (Exception e) {
                logger.error("Error mapping journey definition: {} - {}",
                        definition.getJourneyCode(), e.getMessage(), e);
                throw e;
            }
        }).toList();

        return ResponseEntity.ok(responses);
    }

}
