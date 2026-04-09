package com.luscadevs.journeyorchestrator.api.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.luscadevs.journey.api.generated.JourneyInstancesApi;
import com.luscadevs.journey.api.generated.model.CreateJourneyInstanceRequest;
import com.luscadevs.journey.api.generated.model.EventRequest;
import com.luscadevs.journey.api.generated.model.JourneyInstanceResponse;
import com.luscadevs.journeyorchestrator.api.dto.TransitionHistoryListResponse;
import com.luscadevs.journeyorchestrator.api.mapper.JourneyInstanceMapper;
import com.luscadevs.journeyorchestrator.api.mapper.TransitionHistoryMapper;
import com.luscadevs.journeyorchestrator.application.service.JourneyInstanceService;
import com.luscadevs.journeyorchestrator.application.service.TransitionHistoryService;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/journey-instances")
@RequiredArgsConstructor
public class JourneyInstanceController implements JourneyInstancesApi {

    private final JourneyInstanceService journeyInstanceService;
    private final TransitionHistoryService transitionHistoryService;
    private final TransitionHistoryMapper transitionHistoryMapper;

    @Override
    @PostMapping
    public ResponseEntity<JourneyInstanceResponse> createJourneyInstance(
            @Valid @RequestBody CreateJourneyInstanceRequest createJourneyInstanceRequest) {
        JourneyInstance journeyInstance =
                journeyInstanceService.startJourney(createJourneyInstanceRequest.getJourneyCode(),
                        createJourneyInstanceRequest.getVersion(),
                        createJourneyInstanceRequest.getContext());
        return ResponseEntity.ok(JourneyInstanceMapper.toResponse(journeyInstance));
    }

    @Override
    @GetMapping("/{instanceId}")
    public ResponseEntity<JourneyInstanceResponse> getJourneyInstance(@NotNull String instanceId) {
        JourneyInstance journeyInstance = journeyInstanceService.getInstance(instanceId);
        JourneyInstanceResponse response = JourneyInstanceMapper.toResponse(journeyInstance);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/{instanceId}/events")
    public ResponseEntity<JourneyInstanceResponse> sendEvent(@NotNull String instanceId,
            @Valid @RequestBody EventRequest eventRequest) {
        Event event = Event.of(eventRequest.getEvent());
        JourneyInstance journeyInstance =
                journeyInstanceService.applyEvent(instanceId, event, eventRequest.getPayload());
        return ResponseEntity.ok(JourneyInstanceMapper.toResponse(journeyInstance));
    }

    @GetMapping
    public ResponseEntity<List<JourneyInstanceResponse>> getAllJourneyInstances() {
        List<JourneyInstance> instances = journeyInstanceService.getAllInstances();
        List<JourneyInstanceResponse> responses =
                instances.stream().map(JourneyInstanceMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{instanceId}/history")
    public ResponseEntity<TransitionHistoryListResponse> getJourneyInstanceTransitionHistory(
            @PathVariable @NotNull String instanceId,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String eventType,
            @RequestParam(defaultValue = "100") @Min(1) @Max(1000) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset) {

        // Validate that the journey instance exists
        journeyInstanceService.getInstance(instanceId);

        // Get transition history based on filters
        var historyEvents = switch (getFilterType(from, to, eventType)) {
            case NONE -> transitionHistoryService.getTransitionHistory(instanceId);
            case DATE_RANGE -> transitionHistoryService.getTransitionHistory(instanceId, from, to);
            case EVENT_TYPE -> transitionHistoryService.getTransitionHistoryByEventType(instanceId,
                    eventType);
            case BOTH -> {
                var dateFiltered =
                        transitionHistoryService.getTransitionHistory(instanceId, from, to);
                yield dateFiltered.stream().filter(event -> event.hasEventType(eventType)).toList();
            }
        };

        // Apply pagination
        int totalCount = historyEvents.size();
        int startIndex = Math.min(offset, totalCount);
        int endIndex = Math.min(startIndex + limit, totalCount);
        var paginatedEvents = historyEvents.subList(startIndex, endIndex);

        TransitionHistoryListResponse response = transitionHistoryMapper.toListResponse(instanceId,
                paginatedEvents, limit, offset, totalCount);

        return ResponseEntity.ok(response);
    }

    private FilterType getFilterType(Instant from, Instant to, String eventType) {
        boolean hasDateFilter = from != null || to != null;
        boolean hasEventTypeFilter = eventType != null && !eventType.trim().isEmpty();

        if (hasDateFilter && hasEventTypeFilter) {
            return FilterType.BOTH;
        } else if (hasDateFilter) {
            return FilterType.DATE_RANGE;
        } else if (hasEventTypeFilter) {
            return FilterType.EVENT_TYPE;
        } else {
            return FilterType.NONE;
        }
    }

    private enum FilterType {
        NONE, DATE_RANGE, EVENT_TYPE, BOTH
    }
}
