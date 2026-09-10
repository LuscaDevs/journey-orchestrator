package com.luscadevs.journeyorchestrator.domain.connector.http;

import com.luscadevs.journeyorchestrator.domain.connector.ConnectorType;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class HttpConnectorConfigurationTest {

    @Test
    void shouldCreateConfigurationWithValidParameters() {
        HttpMethod method = HttpMethod.GET;
        String url = "https://api.example.com/customers";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("limit", "10");
        String body = null;
        Duration timeout = Duration.ofSeconds(30);

        HttpConnectorConfiguration config = new HttpConnectorConfiguration(
                method, url, headers, queryParams, body, timeout);

        assertEquals(HttpMethod.GET, config.getMethod());
        assertEquals(url, config.getUrl());
        assertEquals(headers, config.getHeaders());
        assertEquals(queryParams, config.getQueryParams());
        assertEquals(body, config.getBody());
        assertEquals(ConnectorType.HTTP, config.getConnectorType());
        assertEquals(Duration.ofSeconds(30), config.getTimeout());
    }

    @Test
    void shouldCreateConfigurationWithDefaultTimeout() {
        HttpMethod method = HttpMethod.POST;
        String url = "https://api.example.com/customers";
        Map<String, String> headers = new HashMap<>();
        String body = "{\"name\":\"test\"}";

        HttpConnectorConfiguration config = new HttpConnectorConfiguration(
                method, url, headers, null, body);

        assertEquals(Duration.ofSeconds(30), config.getTimeout());
    }

    @Test
    void shouldThrowExceptionWhenMethodIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HttpConnectorConfiguration(null, "https://api.example.com", 
                    null, null, null);
        });
    }

    @Test
    void shouldThrowExceptionWhenUrlIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HttpConnectorConfiguration(HttpMethod.GET, null, 
                    null, null, null);
        });
    }

    @Test
    void shouldThrowExceptionWhenUrlIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> {
            new HttpConnectorConfiguration(HttpMethod.GET, "   ", 
                    null, null, null);
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
    void shouldSupportAllHttpMethods() {
        String url = "https://api.example.com";

        for (HttpMethod method : HttpMethod.values()) {
            HttpConnectorConfiguration config = new HttpConnectorConfiguration(
                    method, url, null, null, null);
            assertEquals(method, config.getMethod());
        }
    }

    @Test
    void shouldSupportOptionalHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer token");

        HttpConnectorConfiguration config = new HttpConnectorConfiguration(
                HttpMethod.POST, "https://api.example.com", headers, null, null);

        assertEquals(2, config.getHeaders().size());
        assertEquals("application/json", config.getHeaders().get("Content-Type"));
    }

    @Test
    void shouldSupportOptionalQueryParams() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("limit", "10");
        queryParams.put("offset", "20");

        HttpConnectorConfiguration config = new HttpConnectorConfiguration(
                HttpMethod.GET, "https://api.example.com", null, queryParams, null);

        assertEquals(2, config.getQueryParams().size());
        assertEquals("10", config.getQueryParams().get("limit"));
    }

    @Test
    void shouldSupportOptionalBody() {
        String body = "{\"customerId\":\"123\",\"action\":\"update\"}";

        HttpConnectorConfiguration config = new HttpConnectorConfiguration(
                HttpMethod.POST, "https://api.example.com", null, null, body);

        assertEquals(body, config.getBody());
    }

    @Test
    void shouldSupportUrlWithPlaceholders() {
        String url = "https://api.example.com/customers/${customerId}";

        HttpConnectorConfiguration config = new HttpConnectorConfiguration(
                HttpMethod.GET, url, null, null, null);

        assertEquals(url, config.getUrl());
    }

    @Test
    void shouldSupportHeadersWithPlaceholders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer ${token}");

        HttpConnectorConfiguration config = new HttpConnectorConfiguration(
                HttpMethod.GET, "https://api.example.com", headers, null, null);

        assertEquals("Bearer ${token}", config.getHeaders().get("Authorization"));
    }

    @Test
    void shouldSupportBodyWithPlaceholders() {
        String body = "{\"customerId\":\"${customerId}\"}";

        HttpConnectorConfiguration config = new HttpConnectorConfiguration(
                HttpMethod.POST, "https://api.example.com", null, null, body);

        assertEquals(body, config.getBody());
    }
}
