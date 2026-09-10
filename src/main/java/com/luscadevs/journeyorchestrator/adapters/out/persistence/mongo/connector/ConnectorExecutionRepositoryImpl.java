package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.connector;

import com.luscadevs.journeyorchestrator.application.port.out.ConnectorRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorExecutionRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB implementation of ConnectorRepositoryPort.
 * 
 * Handles persistence of connector execution records using MongoDB.
 */
@Repository
@RequiredArgsConstructor
public class ConnectorExecutionRepositoryImpl implements ConnectorRepositoryPort {
    
    private final ConnectorExecutionMongoRepository mongoRepository;
    
    @Override
    public ConnectorExecutionRecord save(ConnectorExecutionRecord record) {
        ConnectorExecutionDocument document = ConnectorExecutionDocument.fromDomain(record);
        ConnectorExecutionDocument saved = mongoRepository.save(document);
        return ConnectorExecutionDocument.toDomain(saved);
    }
    
    @Override
    public List<ConnectorExecutionRecord> findByJourneyInstanceId(String journeyInstanceId) {
        List<ConnectorExecutionDocument> documents = mongoRepository.findByJourneyInstanceId(journeyInstanceId);
        return documents.stream()
                .map(ConnectorExecutionDocument::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ConnectorExecutionRecord> findByJourneyInstanceIdWithFilters(
            String journeyInstanceId, Instant from, Instant to, 
            ConnectorExecutionRecord.ExecutionStatus status, int limit, int offset) {
        
        List<ConnectorExecutionDocument> documents;
        
        if (from != null && to != null && status != null) {
            documents = mongoRepository.findByJourneyInstanceIdWithFilters(
                    journeyInstanceId, from, to, status.name());
        } else {
            documents = mongoRepository.findByJourneyInstanceId(journeyInstanceId);
        }
        
        // Apply pagination
        return documents.stream()
                .skip(offset)
                .limit(limit)
                .map(ConnectorExecutionDocument::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countByJourneyInstanceId(String journeyInstanceId) {
        return mongoRepository.countByJourneyInstanceId(journeyInstanceId);
    }
    
    @Override
    public Optional<ConnectorExecutionRecord> findById(String id) {
        return mongoRepository.findById(id)
                .map(ConnectorExecutionDocument::toDomain);
    }
}
