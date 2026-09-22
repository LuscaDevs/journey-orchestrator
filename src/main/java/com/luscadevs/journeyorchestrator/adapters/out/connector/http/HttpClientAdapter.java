package com.luscadevs.journeyorchestrator.adapters.out.connector.http;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.luscadevs.journeyorchestrator.domain.connector.exception.ConnectorNetworkException;
import com.luscadevs.journeyorchestrator.domain.connector.exception.ConnectorTimeoutException;
import com.luscadevs.journeyorchestrator.domain.connector.http.HttpMethod;

/**
 * Adapter for HTTP client operations using Spring WebClient.
 * 
 * Wraps WebClient to provide a simplified interface for HTTP connector
 * operations.
 * Handles HTTP request execution with timeout, headers, query parameters, and
 * body.
 */
@Component
public class HttpClientAdapter {

    private final WebClient webClient;

    public HttpClientAdapter(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Executes an HTTP request with the given parameters.
     * 
     * @param method      The HTTP method
     * @param url         The request URL
     * @param headers     Optional HTTP headers
     * @param queryParams Optional query parameters
     * @param body        Optional request body
     * @param timeout     Request timeout
     * @return The HTTP response as a string
     * @throws ConnectorNetworkException if network error occurs
     * @throws ConnectorTimeoutException if timeout occurs
     */
    public String execute(HttpMethod method, String url, Map<String, String> headers,
            Map<String, String> queryParams, String body, Duration timeout) {
        try {
            // Build URI — use URI.create() so that absolute URLs (https://...) are
            // handled correctly. UriBuilder.path() treats the value as a relative path
            // and strips the scheme/host, causing "Host is not specified" errors.
            URI resolvedUri;
            if (queryParams != null && !queryParams.isEmpty()) {
                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
                queryParams.forEach(uriBuilder::queryParam);
                resolvedUri = uriBuilder.build().toUri();
            } else {
                resolvedUri = URI.create(url);
            }

            WebClient.RequestBodySpec request = webClient
                    .method(convertToSpringHttpMethod(method))
                    .uri(resolvedUri);

            // Add headers
            if (headers != null && !headers.isEmpty()) {
                HttpHeaders httpHeaders = new HttpHeaders();
                headers.forEach(httpHeaders::add);
                request.headers(h -> h.addAll(httpHeaders));
            }

            // Add body if present
            if (body != null && !body.isBlank()) {
                request.contentType(MediaType.APPLICATION_JSON);
                request.bodyValue(body);
            }

            // Execute request with timeout
            return request
                    .retrieve()
                    .bodyToMono(String.class)
                    .toFuture()
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);

        } catch (WebClientResponseException e) {
            throw new ConnectorNetworkException(
                    "HTTP request failed with status " + e.getStatusCode() + ": " + e.getResponseBodyAsString(),
                    e);
        } catch (TimeoutException e) {
            throw new ConnectorTimeoutException(
                    "HTTP request timed out after " + timeout.toMillis() + "ms",
                    timeout.toMillis(),
                    timeout.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectorNetworkException(
                    "HTTP request interrupted: " + e.getMessage(),
                    e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof WebClientResponseException responseException) {
                throw new ConnectorNetworkException(
                        "HTTP request failed with status " + responseException.getStatusCode() + ": "
                                + responseException.getResponseBodyAsString(),
                        responseException);
            }
            throw new ConnectorNetworkException(
                    "HTTP request failed: " + e.getMessage(),
                    e);
        } catch (Exception e) {
            throw new ConnectorNetworkException(
                    "HTTP request failed: " + e.getMessage(),
                    e);
        }
    }

    private org.springframework.http.HttpMethod convertToSpringHttpMethod(HttpMethod method) {
        return switch (method) {
            case GET -> org.springframework.http.HttpMethod.GET;
            case POST -> org.springframework.http.HttpMethod.POST;
            case PUT -> org.springframework.http.HttpMethod.PUT;
            case DELETE -> org.springframework.http.HttpMethod.DELETE;
            case PATCH -> org.springframework.http.HttpMethod.PATCH;
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };
    }
}
