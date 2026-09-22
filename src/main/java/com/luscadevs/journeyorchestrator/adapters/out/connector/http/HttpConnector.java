package com.luscadevs.journeyorchestrator.adapters.out.connector.http;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luscadevs.journeyorchestrator.domain.connector.Connector;
import com.luscadevs.journeyorchestrator.domain.connector.exception.ConnectorConfigurationException;
import com.luscadevs.journeyorchestrator.domain.connector.exception.ConnectorException;
import com.luscadevs.journeyorchestrator.domain.connector.http.HttpConnectorConfiguration;
import com.luscadevs.journeyorchestrator.domain.connector.http.HttpConnectorResult;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP connector implementation using Spring WebClient.
 * 
 * Executes HTTP requests with context variable resolution, handles response
 * parsing,
 * and returns structured results for context enrichment.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpConnector implements Connector<HttpConnectorConfiguration> {

    private final HttpClientAdapter httpClientAdapter;
    private final ContextVariableResolver contextVariableResolver;
    private final ObjectMapper objectMapper;

    @Override
    public HttpConnectorResult execute(HttpConnectorConfiguration config, JourneyInstance instance) {
        if (config == null) {
            throw new ConnectorConfigurationException("HTTP connector configuration cannot be null");
        }
        if (instance == null) {
            throw new ConnectorConfigurationException("Journey instance cannot be null");
        }

        try {
            // Resolve context variables in configuration
            String resolvedUrl = contextVariableResolver.resolve(config.getUrl(), instance);
            Map<String, String> resolvedHeaders = contextVariableResolver.resolveMap(
                    config.getHeaders(), instance);
            Map<String, String> resolvedQueryParams = contextVariableResolver.resolveMap(
                    config.getQueryParams(), instance);
            String resolvedBody = contextVariableResolver.resolve(config.getBody(), instance);

            log.debug("Executing HTTP {} request to {}", config.getMethod(), resolvedUrl);

            // Execute HTTP request
            String responseBody = httpClientAdapter.execute(
                    config.getMethod(),
                    resolvedUrl,
                    resolvedHeaders,
                    resolvedQueryParams,
                    resolvedBody,
                    config.getTimeout());

            // Parse response for context enrichment
            Map<String, Object> responseData = parseResponse(responseBody);

            log.debug("HTTP request successful, response size: {} bytes", responseBody.length());

            return HttpConnectorResult.success(responseData, 200, resolvedHeaders, responseBody);

        } catch (ConnectorException e) {
            log.error("HTTP connector execution failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during HTTP connector execution", e);
            throw new ConnectorException("HTTP connector execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the HTTP response body into a Map for context enrichment.
     *
     * <ul>
     * <li>JSON object ({@code {...}}) → fields are merged directly into the
     * returned map.</li>
     * <li>JSON array ({@code [...]}) → stored as a {@code List} under the
     * {@code "response"} key.</li>
     * <li>Non-JSON / parse failure → raw string stored under
     * {@code "rawResponse"}.</li>
     * </ul>
     *
     * @param responseBody The raw response body
     * @return Parsed response data as a Map
     */
    private Map<String, Object> parseResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return new HashMap<>();
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            if (rootNode.isArray()) {
                // JSON array → wrap in a map so callers receive a consistent Map<String,
                // Object>
                java.util.List<Object> list = objectMapper.convertValue(
                        rootNode,
                        objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, Object.class));
                Map<String, Object> result = new HashMap<>();
                result.put("response", list);
                return result;
            }

            if (rootNode.isObject()) {
                return objectMapper.convertValue(rootNode, Map.class);
            }

            // Scalar JSON value (number, boolean, string at root level)
            Map<String, Object> result = new HashMap<>();
            result.put("response", objectMapper.convertValue(rootNode, Object.class));
            return result;

        } catch (Exception e) {
            log.warn("Failed to parse response as JSON, storing as raw string: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("rawResponse", responseBody);
            return result;
        }
    }
}
