package com.luscadevs.journeyorchestrator.application.port.in;

import com.luscadevs.journeyorchestrator.domain.connector.ConnectorConfiguration;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorExecutionRecord;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorResult;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;
import java.time.Instant;
import java.util.List;

/**
 * Input port for connector execution operations.
 * 
 * This port defines the contract for executing connectors and managing their lifecycle.
 * It provides a clean interface for the application layer to interact with connectors.
 */
public interface ConnectorPort {
    
    /**
     * Executes a connector for a journey instance.
     * 
     * @param journeyInstanceId The journey instance ID
     * @param stateId The state ID that triggered the connector
     * @param configuration The connector configuration
     * @param instance The journey instance containing context
     * @return The connector execution result
     */
    ConnectorResult executeConnector(String journeyInstanceId, String stateId, 
                                      ConnectorConfiguration configuration, JourneyInstance instance);
    
    /**
     * Retrieves connector execution history for a journey instance.
     * 
     * @param journeyInstanceId The journey instance ID
     * @return List of connector execution records
     */
    List<ConnectorExecutionRecord> getExecutionHistory(String journeyInstanceId);
    
    /**
     * Retrieves connector execution history with filtering.
     * 
     * @param journeyInstanceId The journey instance ID
     * @param from Optional start timestamp filter
     * @param to Optional end timestamp filter
     * @param status Optional status filter
     * @param limit Maximum number of records to return
     * @param offset Number of records to skip
     * @return List of filtered connector execution records
     */
    List<ConnectorExecutionRecord> getExecutionHistoryWithFilters(
            String journeyInstanceId, Instant from, Instant to, 
            ConnectorExecutionRecord.ExecutionStatus status, int limit, int offset);
    
    /**
     * Counts total connector executions for a journey instance.
     * 
     * @param journeyInstanceId The journey instance ID
     * @return Total count of connector executions
     */
    long countExecutions(String journeyInstanceId);
}
