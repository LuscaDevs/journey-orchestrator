package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.connector;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB repository for connector execution documents.
 * 
 * Provides CRUD operations and custom queries for connector execution records.
 */
@Repository
public interface ConnectorExecutionMongoRepository extends MongoRepository<ConnectorExecutionDocument, String> {
    
    /**
     * Finds all connector execution records for a journey instance.
     * 
     * @param journeyInstanceId The journey instance ID
     * @return List of connector execution documents
     */
    List<ConnectorExecutionDocument> findByJourneyInstanceId(String journeyInstanceId);
    
    /**
     * Finds connector execution records for a journey instance with filtering.
     * 
     * @param journeyInstanceId The journey instance ID
     * @param from Optional start timestamp filter
     * @param to Optional end timestamp filter
     * @param status Optional status filter
     * @return List of filtered connector execution documents
     */
    @Query("{ 'journeyInstanceId': ?0, " +
           "'startTime': { $gte: ?1, $lte: ?2 }, " +
           "'status': ?3 }")
    List<ConnectorExecutionDocument> findByJourneyInstanceIdWithFilters(
            String journeyInstanceId, Instant from, Instant to, String status);
    
    /**
     * Counts total connector execution records for a journey instance.
     * 
     * @param journeyInstanceId The journey instance ID
     * @return Total count of connector execution records
     */
    long countByJourneyInstanceId(String journeyInstanceId);
}
