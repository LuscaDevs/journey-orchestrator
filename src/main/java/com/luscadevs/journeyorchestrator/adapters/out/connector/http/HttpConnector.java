package com.luscadevs.journeyorchestrator.adapters.out.connector.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luscadevs.journeyorchestrator.domain.connector.Connector;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorResult;
import com.luscadevs.journeyorchestrator.domain.connector.exception.ConnectorConfigurationException;
import com.luscadevs.journeyorchestrator.domain.connector.exception.ConnectorException;
import com.luscadevs.journeyorchestrator.domain.connector.http.HttpConnectorConfiguration;
import com.luscadevs.journeyorchestrator.domain.connector.http.HttpConnectorResult;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP connector implementation using Spring WebClient.
 * 
 * Executes HTTP requests with context variable resolution, handles response parsing,
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
                    config.getTimeout()
            );
            
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
     * @param responseBody The raw response body
     * @return Parsed response data as a Map
     */
    private Map<String, Object> parseResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return new HashMap<>();
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            return objectMapper.convertValue(rootNode, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse response as JSON, returning as raw string: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("rawResponse", responseBody);
            return result;
        }
    }
}
