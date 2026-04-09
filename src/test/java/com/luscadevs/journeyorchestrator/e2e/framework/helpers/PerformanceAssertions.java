package com.luscadevs.journeyorchestrator.e2e.framework.helpers;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Utility class for performance assertions in E2E tests.
 * Provides methods to validate response times, throughput, and resource usage.
 */
public class PerformanceAssertions {

    private static final Duration DEFAULT_MAX_RESPONSE_TIME = Duration.ofSeconds(5);
    private static final double DEFAULT_MAX_ERROR_RATE = 0.05; // 5%
    private static final int DEFAULT_MIN_THROUGHPUT = 10; // requests per second

    /**
     * Asserts that response time is within acceptable limits
     */
    public static void assertResponseTime(Duration actualResponseTime, Duration maxResponseTime) {
        assertTrue(actualResponseTime.compareTo(maxResponseTime) <= 0,
                   String.format("Response time %s exceeds maximum allowed %s", actualResponseTime, maxResponseTime));
    }

    /**
     * Asserts that response time is within default limits
     */
    public static void assertResponseTime(Duration actualResponseTime) {
        assertResponseTime(actualResponseTime, DEFAULT_MAX_RESPONSE_TIME);
    }

    /**
     * Asserts that response time in milliseconds is within limits
     */
    public static void assertResponseTimeMs(long actualResponseTimeMs, long maxResponseTimeMs) {
        assertTrue(actualResponseTimeMs <= maxResponseTimeMs,
                   String.format("Response time %dms exceeds maximum allowed %dms", actualResponseTimeMs, maxResponseTimeMs));
    }

    /**
     * Asserts that error rate is within acceptable limits
     */
    public static void assertErrorRate(double actualErrorRate, double maxErrorRate) {
        assertTrue(actualErrorRate <= maxErrorRate,
                   String.format("Error rate %.2f%% exceeds maximum allowed %.2f%%", actualErrorRate * 100, maxErrorRate * 100));
    }

    /**
     * Asserts that error rate is within default limits
     */
    public static void assertErrorRate(double actualErrorRate) {
        assertErrorRate(actualErrorRate, DEFAULT_MAX_ERROR_RATE);
    }

    /**
     * Asserts that throughput meets minimum requirements
     */
    public static void assertThroughput(double actualThroughput, double minThroughput) {
        assertTrue(actualThroughput >= minThroughput,
                   String.format("Throughput %.2f requests/sec is below minimum required %.2f requests/sec", 
                              actualThroughput, minThroughput));
    }

    /**
     * Asserts that throughput meets default requirements
     */
    public static void assertThroughput(double actualThroughput) {
        assertThroughput(actualThroughput, DEFAULT_MIN_THROUGHPUT);
    }

    /**
     * Asserts that performance metrics meet all criteria
     */
    public static void assertPerformanceMetrics(PerformanceMetrics.PerformanceSummary metrics) {
        assertResponseTime(metrics.getAverageResponseTime(), DEFAULT_MAX_RESPONSE_TIME);
        assertErrorRate(metrics.getErrorRate(), DEFAULT_MAX_ERROR_RATE);
        assertThroughput(metrics.getThroughput(), DEFAULT_MIN_THROUGHPUT);
    }

    /**
     * Asserts that performance metrics meet custom criteria
     */
    public static void assertPerformanceMetrics(PerformanceMetrics.PerformanceSummary metrics, 
                                              Duration maxResponseTime, 
                                              double maxErrorRate, 
                                              double minThroughput) {
        assertResponseTime(metrics.getAverageResponseTime(), maxResponseTime);
        assertErrorRate(metrics.getErrorRate(), maxErrorRate);
        assertThroughput(metrics.getThroughput(), minThroughput);
    }

    /**
     * Asserts that concurrent execution performance is acceptable
     */
    public static void assertConcurrentPerformance(List<Long> completionTimes, 
                                                 int concurrentInstances, 
                                                 Duration maxAverageTime, 
                                                 Duration maxTotalTime) {
        assertFalse(completionTimes.isEmpty(), "Completion times list cannot be empty");
        assertEquals(concurrentInstances, completionTimes.size(), 
                   "Number of completion times must match number of concurrent instances");

        double averageTime = completionTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        assertResponseTime(Duration.ofMillis((long) averageTime), maxAverageTime);
        
        long totalTime = completionTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        
        assertResponseTime(Duration.ofMillis(totalTime), maxTotalTime);
    }

    /**
     * Asserts that resource usage is within acceptable limits
     */
    public static void assertResourceUsage(Map<String, Object> resourceMetrics, 
                                          Map<String, Object> limits) {
        for (Map.Entry<String, Object> entry : limits.entrySet()) {
            String resource = entry.getKey();
            Object limitValue = entry.getValue();
            Object actualValue = resourceMetrics.get(resource);
            
            assertNotNull(actualValue, String.format("Resource metric '%s' is missing", resource));
            
            if (limitValue instanceof Number && actualValue instanceof Number) {
                double limit = ((Number) limitValue).doubleValue();
                double actual = ((Number) actualValue).doubleValue();
                
                assertTrue(actual <= limit,
                           String.format("Resource usage '%s': %.2f exceeds limit %.2f", resource, actual, limit));
            }
        }
    }

    /**
     * Asserts that performance degradation is within acceptable limits
     */
    public static void assertPerformanceDegradation(double baselinePerformance, 
                                                  double currentPerformance, 
                                                  double maxDegradationPercent) {
        double degradationPercent = ((baselinePerformance - currentPerformance) / baselinePerformance) * 100;
        
        assertTrue(degradationPercent <= maxDegradationPercent,
                   String.format("Performance degradation %.2f%% exceeds maximum allowed %.2f%%", 
                              degradationPercent, maxDegradationPercent));
    }

    /**
     * Asserts that performance improves or stays within acceptable degradation
     */
    public static void assertPerformanceImprovement(double baselinePerformance, 
                                                   double currentPerformance, 
                                                   double maxDegradationPercent) {
        if (currentPerformance >= baselinePerformance) {
            // Performance improved - this is good
            return;
        }
        
        // Performance degraded - check if within acceptable limits
        assertPerformanceDegradation(baselinePerformance, currentPerformance, maxDegradationPercent);
    }

    /**
     * Asserts that response time distribution is within acceptable bounds
     */
    public static void assertResponseTimeDistribution(List<Long> responseTimes, 
                                                    Duration maxAverage, 
                                                    Duration maxP95, 
                                                    Duration maxP99) {
        assertFalse(responseTimes.isEmpty(), "Response times list cannot be empty");

        // Calculate average
        double average = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        
        assertResponseTime(Duration.ofMillis((long) average), maxAverage);

        // Calculate percentiles
        List<Long> sortedTimes = responseTimes.stream()
                .sorted()
                .toList();
        
        int p95Index = (int) Math.ceil(sortedTimes.size() * 0.95) - 1;
        int p99Index = (int) Math.ceil(sortedTimes.size() * 0.99) - 1;
        
        if (p95Index >= 0 && p95Index < sortedTimes.size()) {
            assertResponseTime(Duration.ofMillis(sortedTimes.get(p95Index)), maxP95);
        }
        
        if (p99Index >= 0 && p99Index < sortedTimes.size()) {
            assertResponseTime(Duration.ofMillis(sortedTimes.get(p99Index)), maxP99);
        }
    }

    /**
     * Asserts that performance metrics are consistent across multiple runs
     */
    public static void assertPerformanceConsistency(List<PerformanceMetrics.PerformanceSummary> metricsList,
                                                    double maxVariancePercent) {
        assertFalse(metricsList.isEmpty(), "Metrics list cannot be empty");

        if (metricsList.size() == 1) {
            return; // Single run is always consistent
        }

        // Calculate variance for each metric
        double avgResponseTime = metricsList.stream()
                .mapToDouble(m -> m.getAverageResponseTime().toMillis())
                .average()
                .orElse(0.0);

        double responseTimeVariance = metricsList.stream()
                .mapToDouble(m -> Math.abs(m.getAverageResponseTime().toMillis() - avgResponseTime))
                .average()
                .orElse(0.0) / avgResponseTime * 100;

        assertTrue(responseTimeVariance <= maxVariancePercent,
                   String.format("Response time variance %.2f%% exceeds maximum allowed %.2f%%", 
                              responseTimeVariance, maxVariancePercent));
    }

    /**
     * Asserts that performance meets SLA requirements
     */
    public static void assertSLACompliance(PerformanceMetrics.PerformanceSummary metrics, 
                                          Map<String, Object> slaRequirements) {
        // Check response time SLA
        if (slaRequirements.containsKey("maxResponseTime")) {
            Duration maxResponseTime = Duration.parse((String) slaRequirements.get("maxResponseTime"));
            assertResponseTime(metrics.getAverageResponseTime(), maxResponseTime);
        }

        // Check error rate SLA
        if (slaRequirements.containsKey("maxErrorRate")) {
            double maxErrorRate = ((Number) slaRequirements.get("maxErrorRate")).doubleValue();
            assertErrorRate(metrics.getErrorRate(), maxErrorRate);
        }

        // Check throughput SLA
        if (slaRequirements.containsKey("minThroughput")) {
            double minThroughput = ((Number) slaRequirements.get("minThroughput")).doubleValue();
            assertThroughput(metrics.getThroughput(), minThroughput);
        }
    }

    /**
     * Creates a performance assertion builder for complex assertions
     */
    public static PerformanceAssertionBuilder builder() {
        return new PerformanceAssertionBuilder();
    }

    /**
     * Builder for complex performance assertions
     */
    public static class PerformanceAssertionBuilder {
        private Duration maxResponseTime = DEFAULT_MAX_RESPONSE_TIME;
        private double maxErrorRate = DEFAULT_MAX_ERROR_RATE;
        private double minThroughput = DEFAULT_MIN_THROUGHPUT;
        private Duration maxP95 = Duration.ofSeconds(10);
        private Duration maxP99 = Duration.ofSeconds(15);

        public PerformanceAssertionBuilder withMaxResponseTime(Duration maxResponseTime) {
            this.maxResponseTime = maxResponseTime;
            return this;
        }

        public PerformanceAssertionBuilder withMaxErrorRate(double maxErrorRate) {
            this.maxErrorRate = maxErrorRate;
            return this;
        }

        public PerformanceAssertionBuilder withMinThroughput(double minThroughput) {
            this.minThroughput = minThroughput;
            return this;
        }

        public PerformanceAssertionBuilder withMaxP95(Duration maxP95) {
            this.maxP95 = maxP95;
            return this;
        }

        public PerformanceAssertionBuilder withMaxP99(Duration maxP99) {
            this.maxP99 = maxP99;
            return this;
        }

        public void assertMetrics(PerformanceMetrics.PerformanceSummary metrics) {
            assertResponseTime(metrics.getAverageResponseTime(), maxResponseTime);
            assertErrorRate(metrics.getErrorRate(), maxErrorRate);
            assertThroughput(metrics.getThroughput(), minThroughput);
        }

        public void assertResponseTimeDistribution(List<Long> responseTimes) {
            PerformanceAssertions.assertResponseTimeDistribution(responseTimes, maxResponseTime, maxP95, maxP99);
        }
    }
}
