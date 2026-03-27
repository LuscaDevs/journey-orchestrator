package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyInstanceDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data MongoDB repository for JourneyInstanceDocument.
 */
@Repository
public interface MongoJourneyInstanceRepository extends MongoRepository<JourneyInstanceDocument, String> {

    /**
     * Find journey instances by journey definition ID.
     */
    List<JourneyInstanceDocument> findByJourneyDefinitionId(String journeyDefinitionId);

    /**
     * Find journey instances by current state.
     */
    List<JourneyInstanceDocument> findByCurrentState(String currentState);

    /**
     * Find journey instances by status.
     */
    List<JourneyInstanceDocument> findByStatus(String status);

    /**
     * Find journey instances by journey definition ID and status.
     */
    List<JourneyInstanceDocument> findByJourneyDefinitionIdAndStatus(String journeyDefinitionId, String status);
}
