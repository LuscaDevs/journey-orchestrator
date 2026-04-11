package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MongoDB configuration for the Journey Orchestrator application. Uses Spring Boot
 * auto-configuration with custom properties.
 */
@Configuration
@EnableConfigurationProperties(MongoPersistenceProperties.class)
public class MongoConfiguration {

    // Configuration is handled by Spring Boot auto-configuration
    // Custom properties are loaded through MongoProperties class
    // MongoDB repositories are enabled in MongoAdapterConfiguration
}
