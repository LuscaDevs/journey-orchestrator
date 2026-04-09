package com.luscadevs.journeyorchestrator.e2e.framework.config;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * Manages Testcontainers for E2E testing.
 * Provides MongoDB container lifecycle management and configuration.
 */
public class TestContainerManager {

    private final MongoDBContainer mongoContainer;
    private boolean isStarted = false;

    public TestContainerManager() {
        this.mongoContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
                .withCopyFileToContainer(
                    org.testcontainers.utility.MountableFile.forClasspathResource("testcontainers/mongodb.conf"),
                    "/etc/mongod.conf"
                )
                .withStartupTimeout(Duration.ofSeconds(60));
    }

    /**
     * Starts MongoDB container
     */
    public void startMongoContainer() {
        if (!isStarted) {
            mongoContainer.start();
            isStarted = true;
        }
    }

    /**
     * Stops MongoDB container
     */
    public void stopMongoContainer() {
        if (isStarted) {
            mongoContainer.stop();
            isStarted = false;
        }
    }

    /**
     * Gets container status
     */
    public ContainerStatus getContainerStatus() {
        return isStarted ? ContainerStatus.RUNNING : ContainerStatus.STOPPED;
    }

    /**
     * Gets container connection details
     */
    public ConnectionDetails getConnectionDetails() {
        if (!isStarted) {
            throw new IllegalStateException("Container is not started");
        }
        
        return new ConnectionDetails(
            mongoContainer.getHost(),
            mongoContainer.getMappedPort(27017),
            mongoContainer.getReplicaSetUrl()
        );
    }

    /**
     * Executes command in container
     */
    public ExecutionResult executeCommand(String command) {
        if (!isStarted) {
            throw new IllegalStateException("Container is not started");
        }
        
        try {
            // Execute command in MongoDB container
            String result = mongoContainer.execInContainer("mongosh", "--eval", command).getStdout();
            return new ExecutionResult(0, result, "");
        } catch (Exception e) {
            return new ExecutionResult(1, "", e.getMessage());
        }
    }

    /**
     * Gets MongoDB container instance for advanced configuration
     */
    public MongoDBContainer getMongoContainer() {
        return mongoContainer;
    }

    /**
     * Container status enumeration
     */
    public enum ContainerStatus {
        RUNNING, STOPPED, STARTING, FAILED
    }

    /**
     * Connection details for container
     */
    public static class ConnectionDetails {
        private final String host;
        private final int port;
        private final String connectionString;

        public ConnectionDetails(String host, int port, String connectionString) {
            this.host = host;
            this.port = port;
            this.connectionString = connectionString;
        }

        public String getHost() { return host; }
        public int getPort() { return port; }
        public String getConnectionString() { return connectionString; }
    }

    /**
     * Command execution result
     */
    public static class ExecutionResult {
        private final int exitCode;
        private final String stdout;
        private final String stderr;

        public ExecutionResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public int getExitCode() { return exitCode; }
        public String getStdout() { return stdout; }
        public String getStderr() { return stderr; }
        public boolean isSuccess() { return exitCode == 0; }
    }
}
