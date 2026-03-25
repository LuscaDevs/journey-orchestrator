package com.luscadevs.journeyorchestrator.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.luscadevs.journey.api.generated.JourneyInstancesApi;
import com.luscadevs.journey.api.generated.model.CreateJourneyInstanceRequest;
import com.luscadevs.journey.api.generated.model.EventRequest;
import com.luscadevs.journey.api.generated.model.JourneyInstanceResponse;
import com.luscadevs.journeyorchestrator.api.mapper.JourneyInstanceMapper;
import com.luscadevs.journeyorchestrator.application.service.JourneyInstanceService;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
public class JourneyInstanceController implements JourneyInstancesApi {

    private final JourneyInstanceService journeyInstanceService;

    public JourneyInstanceController(JourneyInstanceService journeyInstanceService) {
        this.journeyInstanceService = journeyInstanceService;
    }

    @Override
    public ResponseEntity<JourneyInstanceResponse> createJourneyInstance(
            @Valid CreateJourneyInstanceRequest createJourneyInstanceRequest) {
        JourneyInstance journeyInstance = journeyInstanceService.startJourney(
                createJourneyInstanceRequest.getJourneyCode(),
                createJourneyInstanceRequest.getVersion(), createJourneyInstanceRequest.getContext());
        return ResponseEntity.ok(JourneyInstanceMapper.toResponse(journeyInstance));
    }

    @Override
    public ResponseEntity<JourneyInstanceResponse> getJourneyInstance(@NotNull String instanceId) {
        JourneyInstance journeyInstance = journeyInstanceService.getInstance(instanceId);
        JourneyInstanceResponse response = JourneyInstanceMapper.toResponse(journeyInstance);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<JourneyInstanceResponse> sendEvent(@NotNull String instanceId,
            @Valid EventRequest eventRequest) {
        Event event = Event.of(eventRequest.getEvent());
        JourneyInstance journeyInstance = journeyInstanceService.applyEvent(instanceId, event);
        return ResponseEntity.ok(JourneyInstanceMapper.toResponse(journeyInstance));
    }

}
