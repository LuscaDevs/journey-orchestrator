package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.util.concurrent.TimeUnit;

/**
 * MongoDB configuration for staging environment.
 * Uses staging MongoDB instance with staging-specific settings and connection pooling.
 */
@Configuration
@Profile("staging")
public class StagingMongoConfiguration {

    private final MongoProperties mongoProperties;

    public StagingMongoConfiguration(MongoProperties mongoProperties) {
        this.mongoProperties = mongoProperties;
    }

    @Bean
    public MongoClient mongoClient() {
        String connectionString = mongoProperties.getUri() != null ? 
                mongoProperties.getUri() : 
                "mongodb://staging-mongo-cluster:27017/journey_orchestrator_staging";
        
        MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                .applyConnectionString(new com.mongodb.ConnectionString(connectionString))
                .applyToConnectionPoolSettings(builder -> 
                    builder
                        .maxSize(mongoProperties.getMaxPoolSize())
                        .minSize(mongoProperties.getMinPoolSize())
                        .maxWaitTime(mongoProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                        .maxConnectionIdleTime(60000, TimeUnit.MILLISECONDS)
                        .maxConnectionLifeTime(300000, TimeUnit.MILLISECONDS)
                )
                .applyToSocketSettings(builder ->
                    builder
                        .connectTimeout(mongoProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                        .readTimeout(30000, TimeUnit.MILLISECONDS)
                );
        
        return MongoClients.create(settingsBuilder.build());
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, 
                mongoProperties.getDatabase() != null ? 
                        mongoProperties.getDatabase() : 
                        "journey_orchestrator_staging");
    }
}
