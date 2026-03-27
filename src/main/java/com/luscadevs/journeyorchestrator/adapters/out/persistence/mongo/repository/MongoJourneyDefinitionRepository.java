package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyDefinitionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for JourneyDefinitionDocument.
 */
@Repository
public interface MongoJourneyDefinitionRepository extends MongoRepository<JourneyDefinitionDocument, String> {

    /**
     * Find journey definition by journey code and version.
     */
    Optional<JourneyDefinitionDocument> findByJourneyCodeAndVersion(String journeyCode, String version);

    /**
     * Find all journey definitions by journey code.
     */
    List<JourneyDefinitionDocument> findByJourneyCode(String journeyCode);
}
