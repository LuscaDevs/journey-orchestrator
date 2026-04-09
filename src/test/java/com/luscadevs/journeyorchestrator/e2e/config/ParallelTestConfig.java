package com.luscadevs.journeyorchestrator.e2e.config;

import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceAccessMode;

/**
 * Parallel test configuration for E2E tests. Configures resource locks and execution modes for safe
 * parallel execution.
 */
public class ParallelTestConfig {

    /**
     * Resource key for MongoDB container access
     */
    public static final String MONGODB_RESOURCE = "mongodb";

    /**
     * Resource key for shared test data
     */
    public static final String SHARED_DATA_RESOURCE = "shared-data";

    /**
     * Default parallel execution mode for E2E tests
     */
    public static final ExecutionMode DEFAULT_EXECUTION_MODE = ExecutionMode.CONCURRENT;

    /**
     * Resource access mode for database operations
     */
    public static final ResourceAccessMode DATABASE_ACCESS_MODE = ResourceAccessMode.READ_WRITE;

    /**
     * Configuration for test isolation levels
     */
    public enum TestIsolationLevel {
        /**
         * Tests can run in parallel with proper resource management
         */
        CONCURRENT,

        /**
         * Tests must run in sequence due to shared resources
         */
        SEQUENTIAL,

        /**
         * Tests must run in complete isolation
         */
        ISOLATED
    }

    /**
     * Gets the appropriate execution mode for a test class
     */
    public static ExecutionMode getExecutionMode(TestIsolationLevel isolationLevel) {
        return switch (isolationLevel) {
            case CONCURRENT -> ExecutionMode.CONCURRENT;
            case SEQUENTIAL -> ExecutionMode.SAME_THREAD;
            case ISOLATED -> ExecutionMode.SAME_THREAD;
        };
    }

    /**
     * Gets resource lock key for database-dependent tests
     */
    public static String getDatabaseResourceLock() {
        return MONGODB_RESOURCE;
    }

    /**
     * Gets resource lock key for shared data tests
     */
    public static String getSharedDataResourceLock() {
        return SHARED_DATA_RESOURCE;
    }
}
