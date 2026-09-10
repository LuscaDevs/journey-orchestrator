package com.luscadevs.journeyorchestrator.api.dto.connector;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant startTime;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant endTime;
    
    private String duration;
    private String status;
    private String result;
    private String errorMessage;
}
