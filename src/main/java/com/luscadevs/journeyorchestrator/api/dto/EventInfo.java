package com.luscadevs.journeyorchestrator.api.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventInfo {
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("data")
    private Map<String, Object> data;
}
