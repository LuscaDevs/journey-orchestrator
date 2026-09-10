package com.luscadevs.journeyorchestrator.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luscadevs.journeyorchestrator.application.port.in.ConnectorPort;
import com.luscadevs.journeyorchestrator.application.port.out.ConnectorRepositoryPort;
import com.luscadevs.journeyorchestrator.application.port.out.ConnectorResolverPort;
import com.luscadevs.journeyorchestrator.domain.connector.Connector;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorConfiguration;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorExecutionRecord;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorResult;
import com.luscadevs.journeyorchestrator.domain.connector.exception.ConnectorException;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service for executing connectors and managing their lifecycle.
 * 
 * This service implements the ConnectorPort interface and handles:
 * - Connector execution with audit trail
 * - Context enrichment from successful connector results
 * - Error handling and logging
 * - Execution history retrieval
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectorExecutionService implements ConnectorPort {
    
    private final ConnectorResolverPort connectorResolver;
    private final ConnectorRepositoryPort connectorRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public ConnectorResult executeConnector(String journeyInstanceId, String stateId,
                                            ConnectorConfiguration configuration, JourneyInstance instance) {
        Instant startTime = Instant.now();
        
        try {
            log.info("Executing connector for journey instance: {}, state: {}", 
                    journeyInstanceId, stateId);
            
            // Resolve connector implementation
            @SuppressWarnings("unchecked")
            Connector<ConnectorConfiguration> connector = 
                    (Connector<ConnectorConfiguration>) connectorResolver.resolve(configuration);
            
            // Execute connector
            ConnectorResult result = connector.execute(configuration, instance);
            
            Instant endTime = Instant.now();
            
            // Create execution record
            ConnectorExecutionRecord record;
            if (result.isSuccess()) {
                record = ConnectorExecutionRecord.success(
                        journeyInstanceId,
                        stateId,
                        configuration.getConnectorType(),
                        serializeConfiguration(configuration),
                        startTime,
                        endTime,
                        serializeResult(result.getData())
                );
                
                // Enrich context with connector result
                if (result.getData() != null && !result.getData().isEmpty()) {
                    instance.mergeData(result.getData());
                    log.debug("Enriched context with connector result: {} keys", 
                            result.getData().size());
                }
            } else {
                record = ConnectorExecutionRecord.failure(
                        journeyInstanceId,
                        stateId,
                        configuration.getConnectorType(),
                        serializeConfiguration(configuration),
                        startTime,
                        endTime,
                        result.getErrorMessage()
                );
            }
            
            // Save execution record
            connectorRepository.save(record);
            
            log.info("Connector execution completed: {}, status: {}", 
                    record.getId(), record.getStatus());
            
            return result;
            
        } catch (ConnectorException e) {
            Instant endTime = Instant.now();
            
            ConnectorExecutionRecord record = ConnectorExecutionRecord.failure(
                    journeyInstanceId,
                    stateId,
                    configuration.getConnectorType(),
                    serializeConfiguration(configuration),
                    startTime,
                    endTime,
                    e.getMessage()
            );
            
            connectorRepository.save(record);
            
            log.error("Connector execution failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<ConnectorExecutionRecord> getExecutionHistory(String journeyInstanceId) {
        return connectorRepository.findByJourneyInstanceId(journeyInstanceId);
    }
    
    @Override
    public List<ConnectorExecutionRecord> getExecutionHistoryWithFilters(
            String journeyInstanceId, Instant from, Instant to, 
            ConnectorExecutionRecord.ExecutionStatus status, int limit, int offset) {
        return connectorRepository.findByJourneyInstanceIdWithFilters(
                journeyInstanceId, from, to, status, limit, offset);
    }
    
    @Override
    public long countExecutions(String journeyInstanceId) {
        return connectorRepository.countByJourneyInstanceId(journeyInstanceId);
    }
    
    /**
     * Serializes connector configuration for audit purposes.
     */
    private String serializeConfiguration(ConnectorConfiguration configuration) {
        try {
            return objectMapper.writeValueAsString(configuration);
        } catch (Exception e) {
            log.warn("Failed to serialize connector configuration", e);
            return configuration.toString();
        }
    }
    
    /**
     * Serializes connector result data for audit purposes.
     */
    private String serializeResult(Object data) {
        if (data == null) {
            return null;
        }
        try {
            String serialized = objectMapper.writeValueAsString(data);
            // Truncate if too large (e.g., > 10KB)
            if (serialized.length() > 10240) {
                return serialized.substring(0, 10240) + "... (truncated)";
            }
            return serialized;
        } catch (Exception e) {
            log.warn("Failed to serialize connector result", e);
            return data.toString();
        }
    }
}
