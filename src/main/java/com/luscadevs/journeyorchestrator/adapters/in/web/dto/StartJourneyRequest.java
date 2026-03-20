package com.luscadevs.journeyorchestrator.adapters.in.web.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartJourneyRequest {
    private String id;
    private Integer version;
    private Map<String, Object> context;
}
