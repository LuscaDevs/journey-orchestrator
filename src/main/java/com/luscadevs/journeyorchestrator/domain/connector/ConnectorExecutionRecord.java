package com.luscadevs.journeyorchestrator.domain.connector;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a single connector execution with audit information.
 * 
 * This entity stores detailed audit information for each connector execution,
 * including timestamps, duration, status, result, and error details.
 * It is stored as part of the journey instance for audit trail purposes.
 */
public class ConnectorExecutionRecord {
    
    private final String id;
    private final String journeyInstanceId;
    private final String stateId;
    private final ConnectorType connectorType;
    private final String connectorConfiguration;
    private final Instant startTime;
    private final Instant endTime;
    private final Duration duration;
    private final ExecutionStatus status;
    private final String result;
    private final String errorMessage;
    
    public enum ExecutionStatus {
        SUCCESS,
        FAILURE
    }
    
    private ConnectorExecutionRecord(String id, String journeyInstanceId, String stateId,
                                    ConnectorType connectorType, String connectorConfiguration,
                                    Instant startTime, Instant endTime, Duration duration,
                                    ExecutionStatus status, String result, String errorMessage) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be null or blank");
        }
        if (journeyInstanceId == null || journeyInstanceId.isBlank()) {
            throw new IllegalArgumentException("Journey instance ID cannot be null or blank");
        }
        if (stateId == null || stateId.isBlank()) {
            throw new IllegalArgumentException("State ID cannot be null or blank");
        }
        if (connectorType == null) {
            throw new IllegalArgumentException("Connector type cannot be null");
        }
        if (connectorConfiguration == null || connectorConfiguration.isBlank()) {
            throw new IllegalArgumentException("Connector configuration cannot be null or blank");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (status == ExecutionStatus.FAILURE && (errorMessage == null || errorMessage.isBlank())) {
            throw new IllegalArgumentException("Error message is required when status is FAILURE");
        }
        
        this.id = id;
        this.journeyInstanceId = journeyInstanceId;
        this.stateId = stateId;
        this.connectorType = connectorType;
        this.connectorConfiguration = connectorConfiguration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration != null ? duration : Duration.between(startTime, endTime);
        this.status = status;
        this.result = result;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Creates a successful connector execution record.
     * 
     * @param journeyInstanceId The journey instance ID
     * @param stateId The state ID that triggered the connector
     * @param connectorType The type of connector executed
     * @param connectorConfiguration The serialized configuration
     * @param startTime Execution start timestamp
     * @param endTime Execution end timestamp
     * @param result Execution result data (may be truncated for large payloads)
     * @return A successful ConnectorExecutionRecord
     */
    public static ConnectorExecutionRecord success(String journeyInstanceId, String stateId,
                                                   ConnectorType connectorType, String connectorConfiguration,
                                                   Instant startTime, Instant endTime, String result) {
        String id = UUID.randomUUID().toString();
        return new ConnectorExecutionRecord(id, journeyInstanceId, stateId, connectorType,
                connectorConfiguration, startTime, endTime, null, ExecutionStatus.SUCCESS, result, null);
    }
    
    /**
     * Creates a failed connector execution record.
     * 
     * @param journeyInstanceId The journey instance ID
     * @param stateId The state ID that triggered the connector
     * @param connectorType The type of connector executed
     * @param connectorConfiguration The serialized configuration
     * @param startTime Execution start timestamp
     * @param endTime Execution end timestamp
     * @param errorMessage The error message describing the failure
     * @return A failed ConnectorExecutionRecord
     */
    public static ConnectorExecutionRecord failure(String journeyInstanceId, String stateId,
                                                   ConnectorType connectorType, String connectorConfiguration,
                                                   Instant startTime, Instant endTime, String errorMessage) {
        String id = UUID.randomUUID().toString();
        return new ConnectorExecutionRecord(id, journeyInstanceId, stateId, connectorType,
                connectorConfiguration, startTime, endTime, null, ExecutionStatus.FAILURE, null, errorMessage);
    }
    
    public String getId() {
        return id;
    }
    
    public String getJourneyInstanceId() {
        return journeyInstanceId;
    }
    
    public String getStateId() {
        return stateId;
    }
    
    public ConnectorType getConnectorType() {
        return connectorType;
    }
    
    public String getConnectorConfiguration() {
        return connectorConfiguration;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public Instant getEndTime() {
        return endTime;
    }
    
    public Duration getDuration() {
        return duration;
    }
    
    public ExecutionStatus getStatus() {
        return status;
    }
    
    public String getResult() {
        return result;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectorExecutionRecord that = (ConnectorExecutionRecord) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "ConnectorExecutionRecord{" +
                "id='" + id + '\'' +
                ", journeyInstanceId='" + journeyInstanceId + '\'' +
                ", stateId='" + stateId + '\'' +
                ", connectorType=" + connectorType +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + duration +
                ", status=" + status +
                '}';
    }
}
