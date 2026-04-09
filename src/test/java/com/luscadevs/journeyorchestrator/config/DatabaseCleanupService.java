package com.luscadevs.journeyorchestrator.config;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for cleaning up MongoDB test data between tests.
 */
@Service
public class DatabaseCleanupService {

    private final MongoTemplate mongoTemplate;

    public DatabaseCleanupService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Drops all collections in the test database to ensure clean state between tests.
     */
    public void cleanupDatabase() {
        mongoTemplate.getCollectionNames().forEach(collection -> {
            mongoTemplate.dropCollection(collection);
        });
    }

    /**
     * Drops specific collections related to journey testing.
     */
    public void cleanupJourneyData() {
        mongoTemplate.dropCollection("journey_definitions");
        mongoTemplate.dropCollection("journey_instances");
        mongoTemplate.dropCollection("transition_history");
    }
}
