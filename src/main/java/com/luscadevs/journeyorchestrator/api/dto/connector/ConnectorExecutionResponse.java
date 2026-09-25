package com.luscadevs.journeyorchestrator.api.dto.connector;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for connector execution record response.
 * 
 * Represents a single connector execution record in API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorExecutionResponse {

    private String id;
    private String journeyInstanceId;
    private String stateId;
    private String connectorType;
    private String connectorConfiguration;

    private Instant startTime;

    private Instant endTime;

    private String duration;
    private String status;
    private String result;
    private String errorMessage;
}
