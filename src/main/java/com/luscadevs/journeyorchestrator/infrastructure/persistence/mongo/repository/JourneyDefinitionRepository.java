package com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.luscadevs.journeyorchestrator.infrastructure.persistence.mongo.document.JourneyDefinitionDocument;

import java.util.Optional;

public interface JourneyDefinitionRepository
        extends MongoRepository<JourneyDefinitionDocument, String> {

    Optional<JourneyDefinitionDocument> findByJourneyCodeAndVersion(
            String journeyCode,
            String version);

}