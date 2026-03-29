package com.luscadevs.journeyorchestrator.api.dto;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransitionHistoryEventResponse {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("instanceId")
    private String instanceId;
    
    @JsonProperty("fromState")
    private String fromState;
    
    @JsonProperty("toState")
    private String toState;
    
    @JsonProperty("event")
    private EventInfo event;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("sequenceNumber")
    private Integer sequenceNumber;
}
