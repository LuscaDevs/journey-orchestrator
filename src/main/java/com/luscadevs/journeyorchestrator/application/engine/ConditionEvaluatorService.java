package com.luscadevs.journeyorchestrator.application.engine;

import com.luscadevs.journeyorchestrator.domain.journey.ContextData;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.ConditionEvaluationResult;
import com.luscadevs.journeyorchestrator.application.port.ConditionEvaluatorPort;

import org.springframework.stereotype.Service;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Service implementation for condition evaluation.
 * 
 * This service provides condition evaluation functionality using Spring Expression Language while
 * maintaining business-agnostic behavior and security constraints.
 */
@Service
public class ConditionEvaluatorService implements ConditionEvaluatorPort {

    private static final Logger logger = LoggerFactory.getLogger(ConditionEvaluatorService.class);
    private final ExpressionParser expressionParser;

    // Expression compilation cache for performance
    private final ConcurrentHashMap<String, Expression> compiledExpressionCache =
            new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalEvaluations = new AtomicLong(0);
    private final AtomicLong totalEvaluationTime = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    // Security patterns for validation
    private static final Pattern DANGEROUS_METHODS_PATTERN =
            Pattern.compile(".*(getClass|forName|invoke|exec|Runtime|Process|System\\.).*");
    private static final Pattern DANGEROUS_CLASSES_PATTERN =
            Pattern.compile(".*(java\\.lang\\.reflect|java\\.io|java\\.net|java\\.security).*");

    // Performance thresholds
    private static final long MAX_EVALUATION_TIME_MS = 10000; // 10 seconds
    private static final int MAX_CACHE_SIZE = 1000;

    public ConditionEvaluatorService() {
        this.expressionParser = new SpelExpressionParser();
    }

    @Override
    public ConditionEvaluationResult evaluate(String expression, ContextData context) {
        if (expression == null || expression.trim().isEmpty()) {
            return ConditionEvaluationResult.syntaxError("Expression cannot be null or empty",
                    Duration.ZERO);
        }

        totalEvaluations.incrementAndGet();

        // Add correlation ID for tracing
        MDC.put("expression", expression.hashCode() + "");
        MDC.put("journeyInstanceId", context.getJourneyInstanceId());

        try {
            // Validate expression security
            ConditionEvaluationResult validationResult = validateExpressionSecurity(expression);
            if (!validationResult.getSuccess()) {
                return validationResult;
            }

            // Get or compile expression with caching
            Expression spelExpression = getOrCompileExpression(expression);

            // Create secure evaluation context
            StandardEvaluationContext evaluationContext = createSecureEvaluationContext(context);

            long startTime = System.currentTimeMillis();
            Object result = spelExpression.getValue(evaluationContext);
            long endTime = System.currentTimeMillis();

            Duration executionTime = Duration.ofMillis(endTime - startTime);
            totalEvaluationTime.addAndGet(executionTime.toMillis());

            // Log performance metrics
            logPerformanceMetrics(expression, executionTime);

            if (result instanceof Boolean) {
                return ConditionEvaluationResult.success((Boolean) result, executionTime);
            } else {
                return ConditionEvaluationResult.runtimeError(
                        "Expression did not evaluate to boolean: " + result, executionTime);
            }

        } catch (SpelEvaluationException e) {
            return ConditionEvaluationResult
                    .syntaxError("Invalid expression syntax: " + e.getMessage(), Duration.ZERO);
        } catch (Exception e) {
            return ConditionEvaluationResult.runtimeError(
                    "Unexpected error during evaluation: " + e.getMessage(), Duration.ZERO);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public boolean validateExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }

        try {
            // Attempt to parse the expression
            expressionParser.parseExpression(expression);
            return true;

        } catch (SpelEvaluationException e) {
            // Syntax error detected
            return false;
        } catch (Exception e) {
            // Other parsing errors
            return false;
        }
    }

    /**
     * Creates a secure SpEL evaluation context with restricted access
     * 
     * @param context The runtime context data
     * @return Secure evaluation context
     */
    private StandardEvaluationContext createSecureEvaluationContext(ContextData context) {
        // Create secure evaluation context
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

        // Add journey data as a variable
        if (context.getJourneyData() != null) {
            evaluationContext.setVariable("journeyData", context.getJourneyData());
        }

        // Add event data as a variable
        if (context.getEventData() != null) {
            evaluationContext.setVariable("eventData", context.getEventData());
        }

        // Add system data as a variable
        if (context.getSystemData() != null) {
            evaluationContext.setVariable("systemData", context.getSystemData());
        }

        return evaluationContext;
    }

    /**
     * Validates expression security against dangerous patterns
     */
    private ConditionEvaluationResult validateExpressionSecurity(String expression) {
        // Check for dangerous method calls
        if (DANGEROUS_METHODS_PATTERN.matcher(expression).matches()) {
            return ConditionEvaluationResult.securityViolation(
                    "Expression contains potentially dangerous method calls", Duration.ZERO);
        }

        // Check for dangerous class references
        if (DANGEROUS_CLASSES_PATTERN.matcher(expression).matches()) {
            return ConditionEvaluationResult.securityViolation(
                    "Expression contains potentially dangerous class references", Duration.ZERO);
        }

        // Check for expression complexity
        if (expression.length() > 1000) {
            return ConditionEvaluationResult.syntaxError(
                    "Expression is too complex (exceeds 1000 characters)", Duration.ZERO);
        }

        return ConditionEvaluationResult.success(true, Duration.ZERO);
    }

    /**
     * Gets compiled expression from cache or compiles and caches it
     */
    private Expression getOrCompileExpression(String expression) {
        // Check cache first
        Expression cached = compiledExpressionCache.get(expression);
        if (cached != null) {
            cacheHits.incrementAndGet();
            logger.debug("Cache hit for expression: {}", expression.hashCode());
            return cached;
        }

        // Cache miss - compile and cache
        cacheMisses.incrementAndGet();
        logger.debug("Cache miss for expression: {}", expression.hashCode());

        Expression compiled = expressionParser.parseExpression(expression);

        // Manage cache size
        if (compiledExpressionCache.size() >= MAX_CACHE_SIZE) {
            // Simple eviction strategy - remove oldest entries
            compiledExpressionCache.clear();
            logger.info("Expression cache cleared due to size limit");
        }

        compiledExpressionCache.put(expression, compiled);
        return compiled;
    }

    /**
     * Logs performance metrics for condition evaluation
     */
    private void logPerformanceMetrics(String expression, Duration executionTime) {
        long evaluations = totalEvaluations.get();
        long totalTime = totalEvaluationTime.get();
        double avgTime = evaluations > 0 ? (double) totalTime / evaluations : 0.0;

        logger.info(
                "Condition evaluation completed - Expression: {}, Time: {}ms, Avg: {}ms, Total: {}, Cache hits: {}, Cache misses: {}",
                expression.hashCode(), executionTime.toMillis(), String.format("%.2f", avgTime),
                evaluations, cacheHits.get(), cacheMisses.get());

        // Log slow evaluations
        if (executionTime.toMillis() > 100) {
            logger.warn("Slow condition evaluation detected - Expression: {}, Time: {}ms",
                    expression.hashCode(), executionTime.toMillis());
        }
    }

    /**
     * Gets performance statistics for monitoring
     */
    public PerformanceStats getPerformanceStats() {
        long evaluations = totalEvaluations.get();
        long totalTime = totalEvaluationTime.get();
        double avgTime = evaluations > 0 ? (double) totalTime / evaluations : 0.0;
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        double hitRate = (hits + misses) > 0 ? (double) hits / (hits + misses) * 100 : 0.0;

        return new PerformanceStats(evaluations, avgTime, hitRate, compiledExpressionCache.size());
    }

    /**
     * Performance statistics data class
     */
    public static class PerformanceStats {
        private final long totalEvaluations;
        private final double averageEvaluationTime;
        private final double cacheHitRate;
        private final int cacheSize;

        public PerformanceStats(long totalEvaluations, double averageEvaluationTime,
                double cacheHitRate, int cacheSize) {
            this.totalEvaluations = totalEvaluations;
            this.averageEvaluationTime = averageEvaluationTime;
            this.cacheHitRate = cacheHitRate;
            this.cacheSize = cacheSize;
        }

        public long getTotalEvaluations() {
            return totalEvaluations;
        }

        public double getAverageEvaluationTime() {
            return averageEvaluationTime;
        }

        public double getCacheHitRate() {
            return cacheHitRate;
        }

        public int getCacheSize() {
            return cacheSize;
        }
    }

}
