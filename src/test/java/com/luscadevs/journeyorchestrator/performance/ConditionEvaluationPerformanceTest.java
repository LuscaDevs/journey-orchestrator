package com.luscadevs.journeyorchestrator.performance;

import com.luscadevs.journeyorchestrator.application.engine.ConditionEvaluatorService;
import com.luscadevs.journeyorchestrator.domain.journey.ContextData;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.ConditionEvaluationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for condition evaluation
 */
class ConditionEvaluationPerformanceTest {

        private ConditionEvaluatorService conditionEvaluator;
        private Random random;

        @BeforeEach
        void setUp() {
                conditionEvaluator = new ConditionEvaluatorService();
                random = new Random(42); // Fixed seed for reproducible tests
        }

        @Test
        @DisplayName("Should evaluate simple expressions under 10ms target")
        void shouldEvaluateSimpleExpressionsUnder10ms() {
                Map<String, Object> contextData = createTestContext();
                ContextData context = ContextData.builder().journeyInstanceId("perf-test")
                                .currentState("PROCESSING").eventData(contextData)
                                .journeyData(contextData).systemData(contextData).build();

                String[] simpleExpressions = {"#eventData.amount > 1000",
                                "#journeyData.approved == true", "#eventData.priority == 'HIGH'",
                                "#systemData.systemLoad < 0.8"};

                for (String expression : simpleExpressions) {
                        long totalTime = 0;
                        int iterations = 1000;

                        for (int i = 0; i < iterations; i++) {
                                long startTime = System.nanoTime();
                                ConditionEvaluationResult result =
                                                conditionEvaluator.evaluate(expression, context);
                                long endTime = System.nanoTime();

                                assertTrue(result.getSuccess(),
                                                "Expression should evaluate successfully: "
                                                                + expression);
                                totalTime += (endTime - startTime);
                        }

                        double averageTimeMs = (totalTime / iterations) / 1_000_000.0;
                        assertTrue(averageTimeMs < 10.0, String.format(
                                        "Simple expression '%s' should evaluate under 10ms average, actual: %.2fms",
                                        expression, averageTimeMs));
                }
        }

        @Test
        @DisplayName("Should evaluate complex expressions under 10ms target")
        void shouldEvaluateComplexExpressionsUnder10ms() {
                Map<String, Object> contextData = createTestContext();
                ContextData context = ContextData.builder().journeyInstanceId("perf-test")
                                .currentState("PROCESSING").eventData(contextData)
                                .journeyData(contextData).systemData(contextData).build();

                String[] complexExpressions = {
                                "(#eventData.amount > 1000 AND #eventData.priority == 'HIGH') OR (#journeyData.approved == true AND #systemData.systemLoad < 0.8)",
                                "((#eventData.category == 'FINANCE' AND #journeyData.riskScore > 50) OR (#eventData.priority == 'HIGH' AND NOT (#systemData.systemLoad > 0.9))) AND #journeyData.customerLevel == 'PREMIUM'",
                                "#eventData.amount >= 1000 AND #eventData.amount <= 5000 AND #journeyData.riskScore > 30 AND #journeyData.riskScore < 80 AND #systemData.systemLoad > 0.1 AND #systemData.systemLoad < 0.9"};

                for (String expression : complexExpressions) {
                        long totalTime = 0;
                        int iterations = 500; // Fewer iterations for complex expressions

                        for (int i = 0; i < iterations; i++) {
                                long startTime = System.nanoTime();
                                ConditionEvaluationResult result =
                                                conditionEvaluator.evaluate(expression, context);
                                long endTime = System.nanoTime();

                                assertTrue(result.getSuccess(),
                                                "Expression should evaluate successfully: "
                                                                + expression);
                                totalTime += (endTime - startTime);
                        }

                        double averageTimeMs = (totalTime / iterations) / 1_000_000.0;
                        assertTrue(averageTimeMs < 10.0, String.format(
                                        "Complex expression '%s' should evaluate under 10ms average, actual: %.2fms",
                                        expression, averageTimeMs));
                }
        }

        @Test
        @DisplayName("Should handle concurrent evaluation efficiently")
        void shouldHandleConcurrentEvaluationEfficiently() throws InterruptedException {
                Map<String, Object> contextData = createTestContext();
                ContextData context = ContextData.builder().journeyInstanceId("concurrent-test")
                                .currentState("PROCESSING").eventData(contextData)
                                .journeyData(contextData).systemData(contextData).build();

                String expression =
                                "(#eventData.amount > 1000 AND #eventData.priority == 'HIGH') OR (#journeyData.approved == true)";
                int threadCount = 10;
                int evaluationsPerThread = 100;
                ExecutorService executor = Executors.newFixedThreadPool(threadCount);

                long startTime = System.currentTimeMillis();

                for (int i = 0; i < threadCount; i++) {
                        executor.submit(() -> {
                                for (int j = 0; j < evaluationsPerThread; j++) {
                                        ConditionEvaluationResult result = conditionEvaluator
                                                        .evaluate(expression, context);
                                        assertTrue(result.getSuccess(),
                                                        "Concurrent evaluation should succeed");
                                }
                        });
                }

                executor.shutdown();
                assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS),
                                "All evaluations should complete within 30 seconds");

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                int totalEvaluations = threadCount * evaluationsPerThread;
                double averageTimeMs = (double) totalTime / totalEvaluations;

                assertTrue(averageTimeMs < 10.0, String.format(
                                "Concurrent evaluations should average under 10ms, actual: %.2fms",
                                averageTimeMs));
                assertTrue(totalTime < 5000, String.format(
                                "All concurrent evaluations should complete within 5 seconds, actual: %dms",
                                totalTime));
        }

        @Test
        @DisplayName("Should maintain performance with memory efficiency")
        void shouldMaintainPerformanceWithMemoryEfficiency() {
                Runtime runtime = Runtime.getRuntime();

                // Force garbage collection before test
                System.gc();
                long initialMemory = runtime.totalMemory() - runtime.freeMemory();

                Map<String, Object> contextData = createTestContext();
                ContextData context = ContextData.builder().journeyInstanceId("memory-test")
                                .currentState("PROCESSING").eventData(contextData)
                                .journeyData(contextData).systemData(contextData).build();

                String expression =
                                "((#eventData.amount > 1000 AND #eventData.priority == 'HIGH') OR (#journeyData.approved == true)) AND #systemData.systemLoad < 0.8";

                // Perform many evaluations
                int iterations = 10000;
                for (int i = 0; i < iterations; i++) {
                        ConditionEvaluationResult result =
                                        conditionEvaluator.evaluate(expression, context);
                        assertTrue(result.getSuccess(), "Evaluation should succeed");

                        // Periodically check memory usage
                        if (i % 1000 == 0) {
                                long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                                long memoryIncrease = currentMemory - initialMemory;

                                // Memory increase should be reasonable (less than 100MB for unit
                                // test) - increased to reduce flakiness
                                assertTrue(memoryIncrease < 100 * 1024 * 1024, String.format(
                                                "Memory increase should be under 100MB at iteration %d, actual: %d bytes",
                                                i, memoryIncrease));
                        }
                }

                // Final memory check
                System.gc();
                long finalMemory = runtime.totalMemory() - runtime.freeMemory();
                long totalMemoryIncrease = finalMemory - initialMemory;

                assertTrue(totalMemoryIncrease < 50 * 1024 * 1024, String.format(
                                "Total memory increase should be under 50MB, actual: %d bytes",
                                totalMemoryIncrease));
        }

        @Test
        @DisplayName("Should handle expression caching efficiently")
        void shouldHandleExpressionCachingEfficiently() {
                Map<String, Object> contextData = createTestContext();
                ContextData context = ContextData.builder().journeyInstanceId("cache-test")
                                .currentState("PROCESSING").eventData(contextData)
                                .journeyData(contextData).systemData(contextData).build();

                String expression = "#eventData.amount > 1000 AND #eventData.priority == 'HIGH'";

                // First evaluation (should potentially cache the expression)
                long firstEvalStart = System.nanoTime();
                ConditionEvaluationResult firstResult =
                                conditionEvaluator.evaluate(expression, context);
                long firstEvalEnd = System.nanoTime();

                // Subsequent evaluations (should benefit from caching)
                long totalTime = 0;
                int subsequentEvaluations = 100;

                for (int i = 0; i < subsequentEvaluations; i++) {
                        long startTime = System.nanoTime();
                        ConditionEvaluationResult result =
                                        conditionEvaluator.evaluate(expression, context);
                        long endTime = System.nanoTime();

                        assertTrue(result.getSuccess(), "Subsequent evaluation should succeed");
                        assertEquals(firstResult.getResult(), result.getResult(),
                                        "Results should be consistent");
                        totalTime += (endTime - startTime);
                }

                double averageSubsequentTimeMs = (totalTime / subsequentEvaluations) / 1_000_000.0;
                double firstEvalTimeMs = (firstEvalEnd - firstEvalStart) / 1_000_000.0;

                // Subsequent evaluations should be faster or at least not significantly slower
                assertTrue(averageSubsequentTimeMs <= firstEvalTimeMs * 1.5, String.format(
                                "Subsequent evaluations should not be significantly slower than first evaluation. First: %.2fms, Average subsequent: %.2fms",
                                firstEvalTimeMs, averageSubsequentTimeMs));
        }

        @Test
        @DisplayName("Should scale linearly with expression complexity")
        void shouldScaleLinearlyWithExpressionComplexity() {
                Map<String, Object> contextData = createTestContext();
                ContextData context = ContextData.builder().journeyInstanceId("scaling-test")
                                .currentState("PROCESSING").eventData(contextData)
                                .journeyData(contextData).systemData(contextData).build();

                // Test expressions of increasing complexity
                String[] complexityLevels = {"#eventData.amount > 1000", // Simple: 1 condition
                                "#eventData.amount > 1000 AND #eventData.priority == 'HIGH'", // Medium:
                                                                                              // 2
                                                                                              // conditions
                                "(#eventData.amount > 1000 AND #eventData.priority == 'HIGH') OR (#journeyData.approved == true AND #systemData.systemLoad < 0.8)", // Complex:
                                                                                                                                                                    // 4
                                                                                                                                                                    // conditions
                                "((#eventData.amount > 1000 AND #eventData.priority == 'HIGH') OR (#journeyData.approved == true AND #systemData.systemLoad < 0.8)) AND (NOT (#eventData.category == 'LOW') OR #journeyData.riskScore > 50)" // Very
                                                                                                                                                                                                                                             // complex:
                                                                                                                                                                                                                                             // 6
                                                                                                                                                                                                                                             // conditions
                };

                double[] previousTimes = new double[complexityLevels.length];

                for (int i = 0; i < complexityLevels.length; i++) {
                        String expression = complexityLevels[i];
                        long totalTime = 0;
                        int iterations = 500;

                        for (int j = 0; j < iterations; j++) {
                                long startTime = System.nanoTime();
                                ConditionEvaluationResult result =
                                                conditionEvaluator.evaluate(expression, context);
                                long endTime = System.nanoTime();

                                assertTrue(result.getSuccess(),
                                                "Expression should evaluate successfully: "
                                                                + expression);
                                totalTime += (endTime - startTime);
                        }

                        double averageTimeMs = (totalTime / iterations) / 1_000_000.0;
                        previousTimes[i] = averageTimeMs;

                        // Each complexity level should still be under 10ms
                        assertTrue(averageTimeMs < 10.0, String.format(
                                        "Complexity level %d should evaluate under 10ms, actual: %.2fms",
                                        i + 1, averageTimeMs));

                        // Growth should be roughly linear (not exponential)
                        if (i > 0) {
                                double growthRatio = previousTimes[i] / previousTimes[i - 1];
                                assertTrue(growthRatio < 3.0, String.format(
                                                "Growth from complexity %d to %d should be linear, ratio: %.2f",
                                                i, i + 1, growthRatio));
                        }
                }
        }

        private Map<String, Object> createTestContext() {
                Map<String, Object> contextData = new HashMap<>();
                contextData.put("amount", 1500);
                contextData.put("priority", "HIGH");
                contextData.put("category", "FINANCE");
                contextData.put("customerLevel", "PREMIUM");
                contextData.put("riskScore", 75);
                contextData.put("approved", true);
                contextData.put("systemLoad", 0.65);
                contextData.put("currentTime", "2025-03-30T10:00:00Z");
                return contextData;
        }
}
