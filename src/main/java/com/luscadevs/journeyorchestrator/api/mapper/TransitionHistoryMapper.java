package com.luscadevs.journeyorchestrator.api.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.luscadevs.journeyorchestrator.api.dto.EventInfo;
import com.luscadevs.journeyorchestrator.api.dto.PaginationInfo;
import com.luscadevs.journeyorchestrator.api.dto.TransitionHistoryEventResponse;
import com.luscadevs.journeyorchestrator.api.dto.TransitionHistoryListResponse;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistory;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TransitionHistoryMapper {

    public TransitionHistoryEventResponse toEventResponse(TransitionHistory event, int sequenceNumber) {
        log.debug("Converting transition history event to response DTO: {}", event.getId().getValue());
        
        EventInfo eventInfo = EventInfo.builder()
                .type(event.getEvent().getName())
                .data(convertMetadataToEventData(event.getEvent().getMetadata()))
                .build();
        
        TransitionHistoryEventResponse response = TransitionHistoryEventResponse.builder()
                .id(event.getId().getValue())
                .instanceId(event.getInstanceId())
                .fromState(event.getFromState() != null ? event.getFromState().getName() : null)
                .toState(event.getToState() != null ? event.getToState().getName() : null)
                .event(eventInfo)
                .timestamp(event.getTimestamp())
                .metadata(event.getMetadata())
                .sequenceNumber(sequenceNumber)
                .build();
        
        log.debug("Successfully converted transition history event to response DTO");
        return response;
    }

    public TransitionHistoryListResponse toListResponse(String instanceId, List<TransitionHistory> events, 
            int limit, int offset, long totalCount) {
        log.debug("Converting transition history list to response DTO for instance: {}", instanceId);
        
        List<TransitionHistoryEventResponse> eventResponses = events.stream()
                .map(event -> toEventResponse(event, events.indexOf(event) + 1))
                .collect(Collectors.toList());
        
        PaginationInfo paginationInfo = PaginationInfo.builder()
                .limit(limit)
                .offset(offset)
                .hasNext(offset + limit < totalCount)
                .hasPrevious(offset > 0)
                .build();
        
        TransitionHistoryListResponse response = TransitionHistoryListResponse.builder()
                .instanceId(instanceId)
                .events(eventResponses)
                .pagination(paginationInfo)
                .totalCount(totalCount)
                .build();
        
        log.debug("Successfully converted {} transition history events to response DTO", eventResponses.size());
        return response;
    }

    private Map<String, Object> convertMetadataToEventData(Map<String, Object> metadata) {
        if (metadata == null) {
            return Map.of();
        }
        
        // Convert event metadata to event data format
        // For now, we'll use the metadata as-is, but this could be enhanced
        // to extract specific event-relevant information
        return metadata;
    }
}
