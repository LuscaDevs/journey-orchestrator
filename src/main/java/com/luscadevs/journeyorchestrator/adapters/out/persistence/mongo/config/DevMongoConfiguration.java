package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * MongoDB configuration for development environment. Uses local MongoDB instance with
 * development-specific settings.
 */
@Configuration
@Profile("dev")
public class DevMongoConfiguration {

    private final MongoPersistenceProperties mongoProperties;

    public DevMongoConfiguration(MongoPersistenceProperties mongoProperties) {
        this.mongoProperties = mongoProperties;
    }

    @Bean
    public MongoClient mongoClient() {
        String connectionString = mongoProperties.getUri() != null ? mongoProperties.getUri()
                : "mongodb://localhost:27017/journey_orchestrator_dev";

        return MongoClients.create(connectionString);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient,
                mongoProperties.getDatabase() != null ? mongoProperties.getDatabase()
                        : "journey_orchestrator_dev");
    }
}
