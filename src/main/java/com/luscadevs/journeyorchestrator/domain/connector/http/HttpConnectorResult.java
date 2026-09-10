package com.luscadevs.journeyorchestrator.domain.connector.http;

import com.luscadevs.journeyorchestrator.domain.connector.ConnectorResult;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP-specific result value object with HTTP response details.
 * 
 * Extends ConnectorResult to include HTTP-specific information such as
 * status code, response headers, and raw response body.
 */
public class HttpConnectorResult extends ConnectorResult {
    
    private final int statusCode;
    private final Map<String, String> responseHeaders;
    private final String responseBody;
    
    private HttpConnectorResult(boolean success, Map<String, Object> data, String errorMessage, 
                                int statusCode, Map<String, String> responseHeaders, String responseBody) {
        super(success, data, errorMessage, statusCode);
        
        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException("HTTP status code must be between 100 and 599");
        }
        
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }
    
    /**
     * Creates a successful HTTP connector result.
     * 
     * @param data The result data for context enrichment
     * @param statusCode The HTTP status code
     * @param responseHeaders Optional response headers
     * @param responseBody Optional raw response body
     * @return A successful HttpConnectorResult
     */
    public static HttpConnectorResult success(Map<String, Object> data, int statusCode, 
                                             Map<String, String> responseHeaders, String responseBody) {
        return new HttpConnectorResult(true, data, null, statusCode, responseHeaders, responseBody);
    }
    
    /**
     * Creates a successful HTTP connector result without headers/body.
     * 
     * @param data The result data for context enrichment
     * @param statusCode The HTTP status code
     * @return A successful HttpConnectorResult
     */
    public static HttpConnectorResult success(Map<String, Object> data, int statusCode) {
        return success(data, statusCode, null, null);
    }
    
    /**
     * Creates a failed HTTP connector result.
     * 
     * @param errorMessage The error message describing the failure
     * @param statusCode The HTTP status code
     * @param responseHeaders Optional response headers
     * @param responseBody Optional raw response body
     * @return A failed HttpConnectorResult
     */
    public static HttpConnectorResult failure(String errorMessage, int statusCode, 
                                               Map<String, String> responseHeaders, String responseBody) {
        return new HttpConnectorResult(false, null, errorMessage, statusCode, responseHeaders, responseBody);
    }
    
    /**
     * Creates a failed HTTP connector result without headers/body.
     * 
     * @param errorMessage The error message describing the failure
     * @param statusCode The HTTP status code
     * @return A failed HttpConnectorResult
     */
    public static HttpConnectorResult failure(String errorMessage, int statusCode) {
        return failure(errorMessage, statusCode, null, null);
    }
    
    @Override
    public Integer getStatusCode() {
        return statusCode;
    }
    
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HttpConnectorResult that = (HttpConnectorResult) o;
        return statusCode == that.statusCode &&
                Objects.equals(responseHeaders, that.responseHeaders) &&
                Objects.equals(responseBody, that.responseBody);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), statusCode, responseHeaders, responseBody);
    }
    
    @Override
    public String toString() {
        return "HttpConnectorResult{" +
                "statusCode=" + statusCode +
                ", responseHeaders=" + responseHeaders +
                ", responseBody='" + responseBody + '\'' +
                "} " + super.toString();
    }
}
