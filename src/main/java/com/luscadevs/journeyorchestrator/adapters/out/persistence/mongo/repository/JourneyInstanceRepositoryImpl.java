package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyInstanceDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper.JourneyInstanceDocumentMapper;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyInstanceRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.ConcurrentModificationException;

/**
 * MongoDB implementation of JourneyInstanceRepositoryPort.
 */
@Repository
@Primary
public class JourneyInstanceRepositoryImpl implements JourneyInstanceRepositoryPort {

    private final MongoJourneyInstanceRepository mongoJourneyInstanceRepository;
    private final JourneyInstanceDocumentMapper mapper;

    @Autowired
    public JourneyInstanceRepositoryImpl(
            MongoJourneyInstanceRepository mongoJourneyInstanceRepository,
            JourneyInstanceDocumentMapper mapper) {
        this.mongoJourneyInstanceRepository = mongoJourneyInstanceRepository;
        this.mapper = mapper;
    }

    @Override
    public JourneyInstance save(JourneyInstance journeyInstance) {
        try {
            JourneyInstanceDocument document = mapper.toDocument(journeyInstance);
            JourneyInstanceDocument saved = mongoJourneyInstanceRepository.save(document);
            return mapper.toDomain(saved);
        } catch (OptimisticLockingFailureException e) {
            throw new ConcurrentModificationException(
                    "JourneyInstance modified concurrently: " + journeyInstance.getId(), e);
        }
    }

    @Override
    public Optional<JourneyInstance> findById(String instanceId) {
        return mongoJourneyInstanceRepository.findById(instanceId).map(mapper::toDomain);
    }

    @Override
    public List<JourneyInstance> findAll() {
        return mongoJourneyInstanceRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(String instanceId) {
        mongoJourneyInstanceRepository.deleteById(instanceId);
    }
}
