package com.luscadevs.journeyorchestrator.e2e.framework.config;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matchers;

import java.time.Duration;

/**
 * Configures RestAssured for E2E testing. Provides centralized configuration for API testing with
 * proper logging and validation.
 */
public class RestAssuredConfiguration {

    private final E2ETestConfiguration configuration;
    private RequestSpecification requestSpecification;
    private ResponseSpecification responseSpecification;

    public RestAssuredConfiguration(E2ETestConfiguration configuration) {
        this.configuration = configuration;
        configureRestAssured();
    }

    /**
     * Configures RestAssured with default settings
     */
    private void configureRestAssured() {
        // Configure base URI and port
        RestAssured.baseURI = configuration.getRestassured().getBaseUri();
        RestAssured.port = configuration.getRestassured().getPort();

        // Enable logging for debugging
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);

        // Create request specification
        requestSpecification = new RequestSpecBuilder().setContentType("application/json")
                .setAccept("application/json").log(LogDetail.ALL).build();

        // Create response specification
        responseSpecification = new ResponseSpecBuilder()
                .expectResponseTime(Matchers.lessThan(5000L)).log(LogDetail.ALL).build();
    }

    /**
     * Gets the configured request specification
     */
    public RequestSpecification getRequestSpecification() {
        return requestSpecification;
    }

    /**
     * Gets the configured response specification
     */
    public ResponseSpecification getResponseSpecification() {
        return responseSpecification;
    }

    /**
     * Creates a request specification with custom base path
     */
    public RequestSpecification createRequestSpecification(String basePath) {
        return new RequestSpecBuilder().setBasePath(basePath).setContentType("application/json")
                .setAccept("application/json").log(LogDetail.ALL).build();
    }

    /**
     * Creates a request specification with authentication
     */
    public RequestSpecification createAuthenticatedRequestSpecification(String token) {
        return new RequestSpecBuilder().addHeader("Authorization", "Bearer " + token)
                .setContentType("application/json").setAccept("application/json").log(LogDetail.ALL)
                .build();
    }

    /**
     * Creates a response specification with status code validation
     */
    public ResponseSpecification createResponseSpecification(int expectedStatusCode) {
        return new ResponseSpecBuilder().expectStatusCode(expectedStatusCode)
                .expectResponseTime(Matchers.lessThan(5000L)).log(LogDetail.ALL).build();
    }

    /**
     * Creates a response specification with content type validation
     */
    public ResponseSpecification createResponseSpecification(int expectedStatusCode,
            String expectedContentType) {
        return new ResponseSpecBuilder().expectStatusCode(expectedStatusCode)
                .expectContentType(expectedContentType).expectResponseTime(Matchers.lessThan(5000L))
                .log(LogDetail.ALL).build();
    }

    /**
     * Resets RestAssured configuration
     */
    public void reset() {
        RestAssured.reset();
    }

    /**
     * Updates base URI and port
     */
    public void updateBaseConfiguration(String baseUri, int port) {
        RestAssured.baseURI = baseUri;
        RestAssured.port = port;
        configureRestAssured();
    }

    /**
     * Gets current configuration
     */
    public E2ETestConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Validates that RestAssured is properly configured
     */
    public boolean isConfigured() {
        return requestSpecification != null && responseSpecification != null;
    }

    /**
     * Gets performance threshold from configuration
     */
    public Duration getPerformanceThreshold() {
        return configuration.getPerformance().getMaxResponseTime();
    }

    /**
     * Checks if performance monitoring is enabled
     */
    public boolean isPerformanceMonitoringEnabled() {
        return configuration.getPerformance().isEnabled();
    }
}
