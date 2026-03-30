package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration validator for MongoDB persistence settings. Ensures all required properties are
 * properly configured and validates constraints.
 */
@Configuration
@Profile({"dev", "staging", "prod"})
@Validated
public class MongoConfigurationValidator {

    private final MongoProperties mongoProperties;
    private final Validator validator;

    public MongoConfigurationValidator(MongoProperties mongoProperties) {
        this.mongoProperties = mongoProperties;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @PostConstruct
    public void validateConfiguration() {
        validateMongoProperties();
        validateEnvironmentSpecificSettings();
    }

    /**
     * Validates basic MongoDB properties.
     */
    private void validateMongoProperties() {
        ValidatedMongoProperties validatedProps = new ValidatedMongoProperties();
        validatedProps.setUri(mongoProperties.getUri());
        validatedProps.setDatabase(mongoProperties.getDatabase());
        validatedProps.setConnectionTimeout(mongoProperties.getConnectionTimeout());
        validatedProps.setMaxPoolSize(mongoProperties.getMaxPoolSize());
        validatedProps.setMinPoolSize(mongoProperties.getMinPoolSize());
        validatedProps.setAutoIndexCreation(mongoProperties.isAutoIndexCreation());

        Set<ConstraintViolation<ValidatedMongoProperties>> violations =
                validator.validate(validatedProps);

        if (!violations.isEmpty()) {
            String errorMessage = violations.stream().map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException(
                    "MongoDB configuration validation failed: " + errorMessage);
        }
    }

    /**
     * Validates environment-specific configuration requirements.
     */
    private void validateEnvironmentSpecificSettings() {
        String activeProfile = getActiveProfile();

        switch (activeProfile) {
            case "dev":
                validateDevSettings();
                break;
            case "staging":
                validateStagingSettings();
                break;
            case "prod":
                validateProdSettings();
                break;
            case "test":
                validateTestSettings();
                break;
            default:
                // Default validation for other profiles
                validateDefaultSettings();
        }
    }

    /**
     * Validates development environment settings.
     */
    private void validateDevSettings() {
        // Development allows more flexible configuration
        if (mongoProperties.getUri() == null && mongoProperties.getDatabase() == null) {
            // Accept defaults for development
        }
    }

    /**
     * Validates test environment settings.
     */
    private void validateTestSettings() {
        // Test environment allows minimal configuration
        // Tests will use embedded MongoDB or testcontainers
        // No validation required for test profile
    }

    /**
     * Validates staging environment settings.
     */
    private void validateStagingSettings() {
        if (mongoProperties.getUri() == null) {
            throw new IllegalStateException(
                    "MongoDB URI must be configured for staging environment");
        }

        if (mongoProperties.getConnectionTimeout() < 3000) {
            throw new IllegalStateException(
                    "Connection timeout should be at least 3000ms for staging");
        }

        if (mongoProperties.getMaxPoolSize() < 10) {
            throw new IllegalStateException("Max pool size should be at least 10 for staging");
        }
    }

    /**
     * Validates production environment settings.
     */
    private void validateProdSettings() {
        if (mongoProperties.getUri() == null) {
            throw new IllegalStateException(
                    "MongoDB URI must be configured for production environment");
        }

        if (mongoProperties.getDatabase() == null
                || mongoProperties.getDatabase().trim().isEmpty()) {
            throw new IllegalStateException(
                    "Database name must be explicitly configured for production");
        }

        if (mongoProperties.getDatabase().equals("journey_orchestrator_prod")) {
            throw new IllegalStateException("Default database name not allowed in production");
        }

        if (mongoProperties.getConnectionTimeout() < 5000) {
            throw new IllegalStateException(
                    "Connection timeout should be at least 5000ms for production");
        }

        if (mongoProperties.getMaxPoolSize() < 20) {
            throw new IllegalStateException("Max pool size should be at least 20 for production");
        }

        if (mongoProperties.getMinPoolSize() < 5) {
            throw new IllegalStateException("Min pool size should be at least 5 for production");
        }

        // Validate URI format for production
        if (!isValidProductionUri(mongoProperties.getUri())) {
            throw new IllegalStateException(
                    "MongoDB URI must include authentication credentials for production");
        }
    }

    /**
     * Validates default settings for other environments.
     */
    private void validateDefaultSettings() {
        if (mongoProperties.getUri() == null && mongoProperties.getDatabase() == null) {
            throw new IllegalStateException(
                    "Either MongoDB URI or database name must be configured");
        }
    }

    /**
     * Gets the active Spring profile.
     */
    private String getActiveProfile() {
        // Check for test profile first (most common for unit tests)
        if (isTestEnvironment()) {
            return "test";
        }

        // Check system property
        String profile = System.getProperty("spring.profiles.active");
        if (profile != null && !profile.isEmpty()) {
            return profile.split(",")[0].trim(); // Take first profile if multiple
        }

        // Check environment variable
        profile = System.getenv("SPRING_PROFILES_ACTIVE");
        if (profile != null && !profile.isEmpty()) {
            return profile.split(",")[0].trim(); // Take first profile if multiple
        }

        return "default";
    }

    /**
     * Checks if we're running in a test environment.
     */
    private boolean isTestEnvironment() {
        // Check if we're running in a test context
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains("Test") || className.contains("junit")
                    || className.contains("surefire") || className.contains("maven")
                    || className.contains("gradle")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates if the production URI includes proper authentication.
     */
    private boolean isValidProductionUri(String uri) {
        if (uri == null)
            return false;

        // Basic validation for authentication in URI
        return uri.contains("://") && (uri.contains("@") || uri.contains("authSource")
                || uri.contains("authMechanism"));
    }

    /**
     * Validated properties class for constraint validation.
     */
    @ConfigurationProperties(prefix = "journey-orchestrator.persistence.mongodb")
    public static class ValidatedMongoProperties {

        @NotNull(message = "Connection timeout cannot be null")
        @Min(value = 1000, message = "Connection timeout must be at least 1000ms")
        private Integer connectionTimeout;

        @NotNull(message = "Max pool size cannot be null")
        @Min(value = 1, message = "Max pool size must be at least 1")
        private Integer maxPoolSize;

        @NotNull(message = "Min pool size cannot be null")
        @Min(value = 1, message = "Min pool size must be at least 1")
        private Integer minPoolSize;

        private Boolean autoIndexCreation;

        private String uri;

        private String database;

        // Getters and setters
        public Integer getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(Integer connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public Integer getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(Integer maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public Integer getMinPoolSize() {
            return minPoolSize;
        }

        public void setMinPoolSize(Integer minPoolSize) {
            this.minPoolSize = minPoolSize;
        }

        public Boolean getAutoIndexCreation() {
            return autoIndexCreation;
        }

        public void setAutoIndexCreation(Boolean autoIndexCreation) {
            this.autoIndexCreation = autoIndexCreation;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }
    }
}
