package com.luscadevs.journeyorchestrator.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationInfo {
    @JsonProperty("limit")
    private Integer limit;
    
    @JsonProperty("offset")
    private Integer offset;
    
    @JsonProperty("hasNext")
    private Boolean hasNext;
    
    @JsonProperty("hasPrevious")
    private Boolean hasPrevious;
}
