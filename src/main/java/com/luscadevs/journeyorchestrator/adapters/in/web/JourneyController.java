package com.luscadevs.journeyorchestrator.adapters.in.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.luscadevs.journeyorchestrator.adapters.in.web.dto.ApplyEventRequest;
import com.luscadevs.journeyorchestrator.adapters.in.web.dto.StartJourneyRequest;
import com.luscadevs.journeyorchestrator.api.dto.JourneyInstanceResponse;
import com.luscadevs.journeyorchestrator.api.mapper.JourneyInstanceMapper;
import com.luscadevs.journeyorchestrator.application.port.in.JourneyInstanceUseCase;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/journeys")
public class JourneyController {

    private final JourneyInstanceUseCase journeyService;

    public JourneyController(JourneyInstanceUseCase journeyService) {
        this.journeyService = journeyService;
    }

    @PostMapping("/start")
    public JourneyInstanceResponse startJourneyInstance(@RequestBody StartJourneyRequest request) {
        JourneyInstance instance = journeyService.startJourney(
                request.getId(),
                request.getVersion(),
                request.getContext());

        return JourneyInstanceMapper.toResponse(instance);
    }

    @PostMapping("{instanceId}/events")
    public JourneyInstanceResponse applyEvent(@PathVariable String instanceId, @RequestBody ApplyEventRequest request) {
        Event event = new Event(request.getEventName());
        JourneyInstance instance = journeyService.applyEvent(instanceId, event);
        return JourneyInstanceMapper.toResponse(instance);
    }

    @GetMapping("{instanceId}")
    public JourneyInstanceResponse getInstance(@PathVariable String instanceId) {
        JourneyInstance instance = journeyService.getInstance(instanceId);
        return JourneyInstanceMapper.toResponse(instance);
    }

}
