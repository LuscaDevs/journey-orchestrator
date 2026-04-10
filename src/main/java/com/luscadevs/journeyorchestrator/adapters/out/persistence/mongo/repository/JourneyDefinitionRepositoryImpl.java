package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyDefinitionDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper.JourneyDefinitionDocumentMapper;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyDefinitionRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB implementation of JourneyDefinitionRepositoryPort.
 */
@Repository
@Primary
public class JourneyDefinitionRepositoryImpl implements JourneyDefinitionRepositoryPort {

    private final MongoJourneyDefinitionRepository mongoJourneyDefinitionRepository;
    private final JourneyDefinitionDocumentMapper mapper;

    @Autowired
    public JourneyDefinitionRepositoryImpl(
            MongoJourneyDefinitionRepository mongoJourneyDefinitionRepository,
            JourneyDefinitionDocumentMapper mapper) {
        this.mongoJourneyDefinitionRepository = mongoJourneyDefinitionRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<JourneyDefinition> findByJourneyCodeAndVersion(String journeyCode,
            Integer version) {
        if (journeyCode == null || version == null) {
            return Optional.empty();
        }
        return mongoJourneyDefinitionRepository.findByJourneyCodeAndVersion(journeyCode, version)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<JourneyDefinition> findLatestVersion(String journeyCode) {
        if (journeyCode == null) {
            return Optional.empty();
        }
        return mongoJourneyDefinitionRepository
                .findFirstByJourneyCodeOrderByVersionDesc(journeyCode).map(mapper::toDomain);
    }

    @Override
    public Optional<List<JourneyDefinition>> findByCode(String code) {
        List<JourneyDefinitionDocument> documents =
                mongoJourneyDefinitionRepository.findByJourneyCode(code);
        if (documents.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(documents.stream().map(mapper::toDomain).toList());
    }

    @Override
    public List<JourneyDefinition> findAll() {
        return mongoJourneyDefinitionRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public JourneyDefinition save(JourneyDefinition journeyDefinition) {
        JourneyDefinitionDocument document = mapper.toDocument(journeyDefinition);
        JourneyDefinitionDocument saved = mongoJourneyDefinitionRepository.save(document);
        return mapper.toDomain(saved);
    }
}
