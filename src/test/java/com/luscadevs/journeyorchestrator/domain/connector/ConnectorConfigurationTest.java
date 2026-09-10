package com.luscadevs.journeyorchestrator.domain.connector;

import com.luscadevs.journeyorchestrator.domain.connector.http.HttpConnectorConfiguration;
import com.luscadevs.journeyorchestrator.domain.connector.http.HttpMethod;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class ConnectorConfigurationTest {

    @Test
    void shouldCreateConfigurationWithValidParameters() {
        ConnectorConfiguration config = new HttpConnectorConfiguration(
                HttpMethod.GET, "https://api.example.com", null, null, null, Duration.ofSeconds(30));

        assertEquals(ConnectorType.HTTP, config.getConnectorType());
        assertEquals(Duration.ofSeconds(30), config.getTimeout());
        assertFalse(config.isRetryable());
    }

    @Test
    void shouldUseDefaultTimeoutWhenNotSpecified() {
        ConnectorConfiguration config = new HttpConnectorConfiguration(
                HttpMethod.GET, "https://api.example.com", null, null, null);

        assertEquals(Duration.ofSeconds(30), config.getTimeout());
    }

    @Test
    void shouldThrowExceptionWhenConnectorTypeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HttpConnectorConfiguration(HttpMethod.GET, "https://api.example.com", 
                    null, null, null, Duration.ofSeconds(30)) {
                @Override
                public ConnectorType getConnectorType() {
                    return null;
                }
            };
        });
    }

    @Test
    void shouldThrowExceptionWhenTimeoutIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HttpConnectorConfiguration(HttpMethod.GET, "https://api.example.com", 
                    null, null, null, null);
        });
    }

    @Test
    void shouldThrowExceptionWhenTimeoutIsZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HttpConnectorConfiguration(HttpMethod.GET, "https://api.example.com", 
                    null, null, null, Duration.ZERO);
        });
    }

    @Test
    void shouldThrowExceptionWhenTimeoutIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HttpConnectorConfiguration(HttpMethod.GET, "https://api.example.com", 
                    null, null, null, Duration.ofSeconds(-1));
        });
    }

    @Test
    void shouldThrowExceptionWhenTimeoutExceedsMaximum() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HttpConnectorConfiguration(HttpMethod.GET, "https://api.example.com", 
                    null, null, null, Duration.ofMinutes(10));
        });
    }

    @Test
    void shouldAcceptTimeoutAtMaximumLimit() {
        ConnectorConfiguration config = new HttpConnectorConfiguration(
                HttpMethod.GET, "https://api.example.com", null, null, null, Duration.ofMinutes(5));

        assertEquals(Duration.ofMinutes(5), config.getTimeout());
    }

    @Test
    void shouldAcceptTimeoutJustBelowMaximum() {
        ConnectorConfiguration config = new HttpConnectorConfiguration(
                HttpMethod.GET, "https://api.example.com", null, null, null, Duration.ofMinutes(4).plusSeconds(59));

        assertEquals(Duration.ofMinutes(4).plusSeconds(59), config.getTimeout());
    }

    @Test
    void shouldAcceptTimeoutJustAboveMinimum() {
        ConnectorConfiguration config = new HttpConnectorConfiguration(
                HttpMethod.GET, "https://api.example.com", null, null, null, Duration.ofMillis(1));

        assertEquals(Duration.ofMillis(1), config.getTimeout());
    }
}
