package com.luscadevs.journeyorchestrator.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.TransitionHistory;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({ "id", "journeyDefinitionId", "journeyVersion", "status", "currentState", "context", "history",
        "createdAt", "updatedAt" })
public class JourneyInstanceResponse {

    private String id;

    private String journeyDefinitionId;

    private Integer journeyVersion;

    private String status;

    private String currentState;

    private Map<String, Object> context;

    private List<TransitionHistory> history;

    private Instant createdAt;

    private Instant updatedAt;
}