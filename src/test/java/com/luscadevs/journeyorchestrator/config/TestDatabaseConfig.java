package com.luscadevs.journeyorchestrator.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Test configuration for database cleanup between tests. Provides beans for proper MongoDB test
 * isolation.
 */
@TestConfiguration
public class TestDatabaseConfig {

    /**
     * Database cleanup utility bean
     */
    @Bean
    public DatabaseCleanupService databaseCleanupService(MongoTemplate mongoTemplate) {
        return new DatabaseCleanupService(mongoTemplate);
    }
}
