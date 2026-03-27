package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MongoDB configuration for production environment.
 * Uses production MongoDB cluster with production-specific settings, security, and connection pooling.
 */
@Configuration
@Profile("prod")
public class ProdMongoConfiguration {

    private final MongoProperties mongoProperties;

    public ProdMongoConfiguration(MongoProperties mongoProperties) {
        this.mongoProperties = mongoProperties;
    }

    @Bean
    public MongoClient mongoClient() {
        if (mongoProperties.getUri() != null) {
            // Use connection string if provided (recommended for production)
            MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                    .applyConnectionString(new com.mongodb.ConnectionString(mongoProperties.getUri()))
                    .applyToConnectionPoolSettings(builder -> 
                        builder
                            .maxSize(mongoProperties.getMaxPoolSize())
                            .minSize(mongoProperties.getMinPoolSize())
                            .maxWaitTime(mongoProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                            .maxConnectionIdleTime(30000, TimeUnit.MILLISECONDS)
                            .maxConnectionLifeTime(180000, TimeUnit.MILLISECONDS)
                    )
                    .applyToSocketSettings(builder ->
                        builder
                            .connectTimeout(mongoProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                            .readTimeout(20000, TimeUnit.MILLISECONDS)
                    )
                    .applyToClusterSettings(builder ->
                        builder
                            .serverSelectionTimeout(30000, TimeUnit.MILLISECONDS)
                    );
            
            return MongoClients.create(settingsBuilder.build());
        } else {
            // Fallback to manual configuration (not recommended for production)
            throw new IllegalStateException("MongoDB URI must be configured for production environment");
        }
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        String databaseName = mongoProperties.getDatabase() != null ? 
                mongoProperties.getDatabase() : 
                "journey_orchestrator_prod";
        
        if (databaseName.equals("journey_orchestrator_prod")) {
            throw new IllegalStateException("Production database name must be explicitly configured");
        }
        
        return new MongoTemplate(mongoClient, databaseName);
    }
}
