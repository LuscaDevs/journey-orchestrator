package com.luscadevs.journeyorchestrator.e2e.framework.base;

import com.luscadevs.journeyorchestrator.e2e.framework.config.E2ETestConfiguration;
import com.luscadevs.journeyorchestrator.e2e.framework.helpers.TestDataManager;
import com.luscadevs.journeyorchestrator.e2e.framework.helpers.PerformanceMetrics;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * Base class for all E2E tests providing common setup and teardown. Manages test environment,
 * RestAssured configuration, and test data isolation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class E2ETestBase {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    protected E2ETestConfiguration testConfiguration;
    protected TestDataManager testDataManager;
    protected PerformanceMetrics performanceMetrics;
    protected RequestSpecification requestSpecification;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUpTestEnvironment() {
        // Clear all proxy-related system properties before RestAssured initialization
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("http.nonProxyHosts");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("https.nonProxyHosts");
        System.clearProperty("ftp.proxyHost");
        System.clearProperty("ftp.proxyPort");
        System.clearProperty("socksProxyHost");
        System.clearProperty("socksProxyPort");

        // Reset RestAssured to clean state
        RestAssured.reset();

        // Configure RestAssured static configuration with minimal settings
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.basePath = "";

        // Disable all RestAssured features that might cause proxy issues
        RestAssured.urlEncodingEnabled = false;
        RestAssured.defaultParser = io.restassured.parsing.Parser.JSON;

        // Initialize test components
        testConfiguration = new E2ETestConfiguration();
        testDataManager = new TestDataManager(testConfiguration);
        performanceMetrics = new PerformanceMetrics();

        // Create a completely self-contained request specification
        requestSpecification = RestAssured.given().baseUri("http://localhost:" + port)
                .contentType("application/json").accept("application/json")
                .urlEncodingEnabled(false);
    }

    @AfterEach
    void cleanupTestEnvironment() {
        // Clean up test data
        if (testDataManager != null) {
            testDataManager.cleanupAllTestData();
        }

        // Performance metrics cleanup (if needed)
        // performanceMetrics cleanup handled automatically

        // Reset RestAssured to clean state
        RestAssured.reset();
    }

    /**
     * Get the configured RestAssured request specification
     */
    protected RequestSpecification getRequestSpecification() {
        return requestSpecification;
    }

    /**
     * Get access to test data manager
     */
    protected TestDataManager getTestDataManager() {
        return testDataManager;
    }

    /**
     * Get access to performance metrics collector
     */
    protected PerformanceMetrics getPerformanceMetrics() {
        return performanceMetrics;
    }

    /**
     * Wait for application to be ready
     */
    protected void waitForApplicationReady() {
        // Wait for Spring Boot application to start
        try {
            Thread.sleep(Duration.ofSeconds(5).toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Configure dynamic properties for Testcontainers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "journey-e2e-test");
    }

    /**
     * Clean up Testcontainers after all tests
     */
    @AfterAll
    static void cleanupContainers() {
        if (mongo != null && mongo.isRunning()) {
            mongo.stop();
        }
    }
}
