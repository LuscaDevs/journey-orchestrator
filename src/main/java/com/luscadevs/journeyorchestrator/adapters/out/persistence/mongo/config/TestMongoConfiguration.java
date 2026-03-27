package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * MongoDB configuration for test environment.
 * Uses embedded MongoDB or testcontainers for testing.
 */
@Configuration
@Profile("test")
public class TestMongoConfiguration {

    /**
     * Test configuration uses Spring Boot's test auto-configuration.
     * No explicit bean definitions needed - Spring Boot Test handles MongoDB setup.
     */
}
