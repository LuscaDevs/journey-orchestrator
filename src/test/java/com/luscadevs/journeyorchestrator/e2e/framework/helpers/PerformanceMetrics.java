package com.luscadevs.journeyorchestrator.e2e.framework.helpers;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Captures and analyzes performance data during test execution. Provides metrics collection for
 * response times, throughput, and resource usage.
 */
public class PerformanceMetrics {

    private final String operationName;
    private final List<Duration> responseTimes = new ArrayList<>();
    private final List<Long> errorCounts = new ArrayList<>();
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final Instant startTime;
    private final ConcurrentHashMap<String, Object> metadata = new ConcurrentHashMap<>();

    public PerformanceMetrics() {
        this.operationName = "default";
        this.startTime = Instant.now();
    }

    public PerformanceMetrics(String operationName) {
        this.operationName = operationName;
        this.startTime = Instant.now();
    }

    /**
     * Starts performance measurement for an operation
     */
    public void startMeasurement(String operationName) {
        // Implementation will record start time and operation details
        metadata.put("operation", operationName);
        metadata.put("startTime", Instant.now().toString());
    }

    /**
     * Stops performance measurement for an operation
     */
    public void stopMeasurement(String operationName) {
        // Implementation will calculate duration and store metrics
        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);
        responseTimes.add(duration);
        metadata.put("endTime", endTime.toString());
        metadata.put("duration", duration.toString());
    }

    /**
     * Stops performance measurement for the current operation
     */
    public void stopMeasurement() {
        stopMeasurement(operationName);
    }

    /**
     * Records a response time
     */
    public void recordResponseTime(Duration responseTime) {
        responseTimes.add(responseTime);
        totalRequests.incrementAndGet();
    }

    /**
     * Records an error occurrence
     */
    public void recordError(String operationName, Exception error) {
        errorCounts.add(1L);
        metadata.put("lastError", error.getMessage());
        metadata.put("lastErrorTime", Instant.now().toString());
    }

    /**
     * Gets the average response time
     */
    public Duration getAverageResponseTime() {
        if (responseTimes.isEmpty()) {
            return Duration.ZERO;
        }

        long totalMillis = responseTimes.stream().mapToLong(Duration::toMillis).sum();

        return Duration.ofMillis(totalMillis / responseTimes.size());
    }

    /**
     * Gets the maximum response time
     */
    public Duration getMaxResponseTime() {
        return responseTimes.stream().max(Duration::compareTo).orElse(Duration.ZERO);
    }

    /**
     * Gets the minimum response time
     */
    public Duration getMinResponseTime() {
        return responseTimes.stream().min(Duration::compareTo).orElse(Duration.ZERO);
    }

    /**
     * Gets the total number of requests
     */
    public long getTotalRequests() {
        return totalRequests.get();
    }

    /**
     * Gets the total number of errors
     */
    public long getTotalErrors() {
        return errorCounts.stream().mapToLong(Long::longValue).sum();
    }

    /**
     * Gets the error rate as a percentage
     */
    public double getErrorRate() {
        long total = getTotalRequests();
        if (total == 0) {
            return 0.0;
        }
        return (double) getTotalErrors() / total * 100.0;
    }

    /**
     * Gets the throughput as requests per second
     */
    public double getThroughput() {
        Duration totalTime = Duration.between(startTime, Instant.now());
        if (totalTime.isZero() || totalTime.getSeconds() == 0) {
            return 0.0;
        }
        return (double) getTotalRequests() / totalTime.getSeconds();
    }

    /**
     * Gets operation metadata
     */
    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * Sets operation metadata
     */
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    /**
     * Creates a performance summary
     */
    public PerformanceSummary getSummary() {
        return new PerformanceSummary(operationName, getTotalRequests(), getTotalErrors(),
                getErrorRate(), getAverageResponseTime(), getMaxResponseTime(),
                getMinResponseTime(), getThroughput(), new ArrayList<>(metadata.values()));
    }

    /**
     * Performance summary data holder
     */
    public static class PerformanceSummary {
        private final String operationName;
        private final long totalRequests;
        private final long totalErrors;
        private final double errorRate;
        private final Duration averageResponseTime;
        private final Duration maxResponseTime;
        private final Duration minResponseTime;
        private final double throughput;
        private final List<Object> metadata;

        public PerformanceSummary(String operationName, long totalRequests, long totalErrors,
                double errorRate, Duration averageResponseTime, Duration maxResponseTime,
                Duration minResponseTime, double throughput, List<Object> metadata) {
            this.operationName = operationName;
            this.totalRequests = totalRequests;
            this.totalErrors = totalErrors;
            this.errorRate = errorRate;
            this.averageResponseTime = averageResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.minResponseTime = minResponseTime;
            this.throughput = throughput;
            this.metadata = metadata;
        }

        // Getters
        public String getOperationName() {
            return operationName;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getTotalErrors() {
            return totalErrors;
        }

        public double getErrorRate() {
            return errorRate;
        }

        public Duration getAverageResponseTime() {
            return averageResponseTime;
        }

        public Duration getMaxResponseTime() {
            return maxResponseTime;
        }

        public Duration getMinResponseTime() {
            return minResponseTime;
        }

        public double getThroughput() {
            return throughput;
        }

        public List<Object> getMetadata() {
            return metadata;
        }
    }
}
