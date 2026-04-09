package com.luscadevs.journeyorchestrator.e2e.framework.config;

import java.time.Duration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration for E2E testing framework. Provides test-specific settings for performance
 * thresholds, reporting, and cleanup.
 */
@Configuration
@PropertySource("classpath:e2e/application-e2e.yml")
public class E2ETestConfiguration {

    /**
     * Performance thresholds for E2E tests
     */
    public static class Performance {
        private boolean enabled = true;
        private Duration maxResponseTime = Duration.ofSeconds(2);
        private double maxErrorRate = 0.05;
        private int minThroughput = 10;

        public boolean isEnabled() {
            return enabled;
        }

        public Duration getMaxResponseTime() {
            return maxResponseTime;
        }

        public double getMaxErrorRate() {
            return maxErrorRate;
        }

        public int getMinThroughput() {
            return minThroughput;
        }
    }

    /**
     * Reporting configuration for E2E tests
     */
    public static class Reporting {
        private boolean enabled = true;
        private String[] formats = {"HTML", "JSON"};
        private String outputDir = "target/e2e-reports";

        public boolean isEnabled() {
            return enabled;
        }

        public String[] getFormats() {
            return formats;
        }

        public String getOutputDir() {
            return outputDir;
        }
    }

    /**
     * Cleanup configuration for E2E tests
     */
    public static class Cleanup {
        private boolean autoCleanup = true;
        private String isolationLevel = "METHOD";

        public boolean isAutoCleanup() {
            return autoCleanup;
        }

        public String getIsolationLevel() {
            return isolationLevel;
        }
    }

    /**
     * Testcontainers configuration for MongoDB
     */
    public static class Testcontainers {
        public MongoDB mongoDB = new MongoDB();

        public static class MongoDB {
            private String image = "mongo:7.0";
            private int port = 27017;
            private Duration startupTimeout = Duration.ofSeconds(60);

            public String getImage() {
                return image;
            }

            public int getPort() {
                return port;
            }

            public Duration getStartupTimeout() {
                return startupTimeout;
            }
        }
    }

    /**
     * RestAssured configuration for E2E tests
     */
    public static class Restassured {
        private String baseUri = "http://localhost:8080";
        private int port = 8080;

        public String getBaseUri() {
            return baseUri;
        }

        public int getPort() {
            return port;
        }
    }

    private Performance performance = new Performance();
    private Reporting reporting = new Reporting();
    private Cleanup cleanup = new Cleanup();
    private Testcontainers testcontainers = new Testcontainers();
    private Restassured restassured = new Restassured();

    public Performance getPerformance() {
        return performance;
    }

    public Reporting getReporting() {
        return reporting;
    }

    public Cleanup getCleanup() {
        return cleanup;
    }

    public Testcontainers getTestcontainers() {
        return testcontainers;
    }

    public Restassured getRestassured() {
        return restassured;
    }
}
