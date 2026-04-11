package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

/**
 * Configuration properties for MongoDB persistence.
 */
@Data
@ConfigurationProperties(prefix = "journey-orchestrator.persistence.mongodb")
public class MongoPersistenceProperties {

    /**
     * Connection timeout in milliseconds.
     */
    private int connectionTimeout = 5000;

    /**
     * Maximum connection pool size.
     */
    private int maxPoolSize = 20;

    /**
     * Minimum connection pool size.
     */
    private int minPoolSize = 5;

    /**
     * Enable auto-index creation.
     */
    private boolean autoIndexCreation = true;

    /**
     * Connection URI for MongoDB.
     */
    private String uri;

    /**
     * Database name.
     */
    private String database;
}
