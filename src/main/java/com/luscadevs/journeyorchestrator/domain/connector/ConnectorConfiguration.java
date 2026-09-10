package com.luscadevs.journeyorchestrator.domain.connector;

import java.time.Duration;

/**
 * Abstract base class for all connector-specific configurations.
 * 
 * This entity stores the parameters needed for connector execution.
 * Subclasses provide protocol-specific configuration details.
 */
public abstract class ConnectorConfiguration {

    private final ConnectorType connectorType;
    private final Duration timeout;
    private final boolean retryable;

    /**
     * Default timeout of 30 seconds.
     */
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Maximum allowed timeout of 5 minutes.
     */
    private static final Duration MAX_TIMEOUT = Duration.ofMinutes(5);

    protected ConnectorConfiguration(ConnectorType connectorType, Duration timeout, boolean retryable) {
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        if (timeout.compareTo(MAX_TIMEOUT) > 0) {
            throw new IllegalArgumentException("Timeout cannot exceed " + MAX_TIMEOUT);
        }

        this.connectorType = connectorType;
        this.timeout = timeout;
        this.retryable = retryable;

        if (this.getConnectorType() == null) {
            throw new IllegalArgumentException("Connector type cannot be null");
        }
    }

    protected ConnectorConfiguration(ConnectorType connectorType, Duration timeout) {
        this(connectorType, timeout, false);
    }

    protected ConnectorConfiguration(ConnectorType connectorType) {
        this(connectorType, DEFAULT_TIMEOUT, false);
    }

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
