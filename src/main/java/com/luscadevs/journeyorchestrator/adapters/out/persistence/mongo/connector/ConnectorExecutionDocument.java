package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.connector;

import com.luscadevs.journeyorchestrator.domain.connector.ConnectorExecutionRecord;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for connector execution records.
 * 
 * Stores audit information for each connector execution, including
 * timestamps, status, result, and error details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "connector_executions")
public class ConnectorExecutionDocument {
    
    @Id
    private String id;
    
    @Indexed
    private String journeyInstanceId;
    
    @Indexed
    private String stateId;
    
    private String connectorType;
    private String connectorConfiguration;
    private Instant startTime;
    private Instant endTime;
    private String duration;
    private String status;
    private String result;
    private String errorMessage;
    
    /**
     * Converts domain entity to MongoDB document.
     */
    public static ConnectorExecutionDocument fromDomain(ConnectorExecutionRecord record) {
        return ConnectorExecutionDocument.builder()
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
     * Converts MongoDB document to domain entity.
     */
    public static ConnectorExecutionRecord toDomain(ConnectorExecutionDocument document) {
        if (document.getStatus().equals(ConnectorExecutionRecord.ExecutionStatus.SUCCESS.name())) {
            return ConnectorExecutionRecord.success(
                    document.getJourneyInstanceId(),
                    document.getStateId(),
                    ConnectorType.valueOf(document.getConnectorType()),
                    document.getConnectorConfiguration(),
                    document.getStartTime(),
                    document.getEndTime(),
                    document.getResult()
            );
        } else {
            return ConnectorExecutionRecord.failure(
                    document.getJourneyInstanceId(),
                    document.getStateId(),
                    ConnectorType.valueOf(document.getConnectorType()),
                    document.getConnectorConfiguration(),
                    document.getStartTime(),
                    document.getEndTime(),
                    document.getErrorMessage()
            );
        }
    }
}
