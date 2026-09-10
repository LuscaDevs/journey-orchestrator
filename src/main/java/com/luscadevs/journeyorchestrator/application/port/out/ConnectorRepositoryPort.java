package com.luscadevs.journeyorchestrator.application.port.out;

import com.luscadevs.journeyorchestrator.domain.connector.ConnectorExecutionRecord;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Output port for connector execution record persistence.
 * 
 * This port defines the contract for storing and retrieving connector execution records.
 * Implementations handle the actual persistence mechanism (e.g., MongoDB).
 */
public interface ConnectorRepositoryPort {
    
    /**
     * Saves a connector execution record.
     * 
     * @param record The execution record to save
     * @return The saved record
     */
    ConnectorExecutionRecord save(ConnectorExecutionRecord record);
    
    /**
     * Retrieves connector execution records for a journey instance.
     * 
     * @param journeyInstanceId The journey instance ID
     * @return List of execution records for the instance
     */
    List<ConnectorExecutionRecord> findByJourneyInstanceId(String journeyInstanceId);
    
    /**
     * Retrieves connector execution records for a journey instance with filtering.
     * 
     * @param journeyInstanceId The journey instance ID
     * @param from Optional start timestamp filter
     * @param to Optional end timestamp filter
     * @param status Optional status filter
     * @param limit Maximum number of records to return
     * @param offset Number of records to skip
     * @return List of filtered execution records
     */
    List<ConnectorExecutionRecord> findByJourneyInstanceIdWithFilters(
            String journeyInstanceId, Instant from, Instant to, 
            ConnectorExecutionRecord.ExecutionStatus status, int limit, int offset);
    
    /**
     * Counts total execution records for a journey instance.
     * 
     * @param journeyInstanceId The journey instance ID
     * @return Total count of execution records
     */
    long countByJourneyInstanceId(String journeyInstanceId);
    
    /**
     * Retrieves a specific execution record by ID.
     * 
     * @param id The execution record ID
     * @return Optional containing the record if found
     */
    Optional<ConnectorExecutionRecord> findById(String id);
}
