package com.luscadevs.journeyorchestrator.domain.connector.http;

import com.luscadevs.journeyorchestrator.domain.connector.ConnectorConfiguration;
import com.luscadevs.journeyorchestrator.domain.connector.ConnectorType;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP-specific connector configuration entity.
 * 
 * Stores HTTP request parameters including method, URL, headers, query parameters, and body.
 * All string fields support context variable placeholders using ${variableName} syntax.
 */
public class HttpConnectorConfiguration extends ConnectorConfiguration {
    
    private final HttpMethod method;
    private final String url;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final String body;
    
    public HttpConnectorConfiguration(HttpMethod method, String url, 
                                       Map<String, String> headers, Map<String, String> queryParams, 
                                       String body, Duration timeout) {
        super(ConnectorType.HTTP, timeout);
        
        if (method == null) {
            throw new IllegalArgumentException("HTTP method cannot be null");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or blank");
        }
        
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.queryParams = queryParams;
        this.body = body;
    }
    
    public HttpConnectorConfiguration(HttpMethod method, String url, 
                                       Map<String, String> headers, Map<String, String> queryParams, 
                                       String body) {
        this(method, url, headers, queryParams, body, Duration.ofSeconds(30));
    }
    
    public HttpMethod getMethod() {
        return method;
    }
    
    public String getUrl() {
        return url;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public Map<String, String> getQueryParams() {
        return queryParams;
    }
    
    public String getBody() {
        return body;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HttpConnectorConfiguration that = (HttpConnectorConfiguration) o;
        return method == that.method &&
                Objects.equals(url, that.url) &&
                Objects.equals(headers, that.headers) &&
                Objects.equals(queryParams, that.queryParams) &&
                Objects.equals(body, that.body);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), method, url, headers, queryParams, body);
    }
    
    @Override
    public String toString() {
        return "HttpConnectorConfiguration{" +
                "method=" + method +
                ", url='" + url + '\'' +
                ", headers=" + headers +
                ", queryParams=" + queryParams +
                ", body='" + body + '\'' +
                "} " + super.toString();
    }
}
