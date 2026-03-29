package com.luscadevs.journeyorchestrator.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransitionHistoryListResponse {
    @JsonProperty("instanceId")
    private String instanceId;
    
    @JsonProperty("events")
    private List<TransitionHistoryEventResponse> events;
    
    @JsonProperty("pagination")
    private PaginationInfo pagination;
    
    @JsonProperty("totalCount")
    private Long totalCount;
}
