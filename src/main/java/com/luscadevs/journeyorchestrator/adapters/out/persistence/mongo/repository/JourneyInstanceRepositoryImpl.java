package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyInstanceDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper.JourneyInstanceDocumentMapper;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyInstanceRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
        JourneyInstanceDocument document = mapper.toDocument(journeyInstance);
        JourneyInstanceDocument saved = mongoJourneyInstanceRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<JourneyInstance> findById(String instanceId) {
        return mongoJourneyInstanceRepository.findById(instanceId)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(String instanceId) {
        mongoJourneyInstanceRepository.deleteById(instanceId);
    }
}
