package com.luscadevs.journeyorchestrator.api.mapper;

import com.luscadevs.journeyorchestrator.api.dto.connector.ConnectorExecutionResponse;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorExecutionRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for connector execution records between domain entities and DTOs.
 */
@Component
public class ConnectorExecutionMapper {
    
    /**
     * Converts domain entity to DTO.
     */
    public ConnectorExecutionResponse toResponse(ConnectorExecutionRecord record) {
        return ConnectorExecutionResponse.builder()
                .id(record.getId())
                .journeyInstanceId(record.getJourneyInstanceId())
                .stateId(record.getStateId())
                .connectorType(record.getConnectorType().name())
                .connectorConfiguration(record.getConnectorConfiguration())
                .startTime(record.getStartTime())
                .endTime(record.getEndTime())
                .duration(record.getDuration().toString())
                .status(record.getStatus().name())
                .result(record.getResult())
                .errorMessage(record.getErrorMessage())
                .build();
    }
    
    /**
     * Converts list of domain entities to DTOs.
     */
    public List<ConnectorExecutionResponse> toResponseList(List<ConnectorExecutionRecord> records) {
        return records.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
